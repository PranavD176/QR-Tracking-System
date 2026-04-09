"""
auth_utils.py (kept as firebase.py to avoid breaking existing imports)

Replaces Firebase Admin SDK with:
  - python-jose for JWT signing/verification (Supabase-compatible)
  - httpx + google-auth for FCM HTTP v1 API push notifications
"""
import os
import json
import httpx
from jose import jwt, JWTError
from fastapi import HTTPException
from datetime import datetime, timedelta, timezone

from google.oauth2 import service_account
from google.auth.transport.requests import Request as GoogleAuthRequest

# ---------------------------------------------------------------------------
# JWT Configuration
# ---------------------------------------------------------------------------
SECRET_KEY = os.getenv("JWT_SECRET_KEY", "change-me-in-production-please")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "1440"))  # 24 hours

# ---------------------------------------------------------------------------
# FCM Configuration (HTTP v1 API)
# ---------------------------------------------------------------------------
CREDENTIALS_FILE = "firebase.json"
FCM_SCOPES = ["https://www.googleapis.com/auth/firebase.messaging"]


# ---------------------------------------------------------------------------
# Token helpers
# ---------------------------------------------------------------------------

def verify_token(token: str) -> dict:
    """
    Verify a JWT issued by this backend (python-jose, HS256).
    Returns the decoded payload dict containing 'uid', 'role', 'email'.
    Raises HTTP 401 on failure.
    """
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        if not payload.get("uid"):
            raise HTTPException(status_code=401, detail="Invalid token: missing uid claim")
        return payload
    except JWTError as e:
        raise HTTPException(status_code=401, detail=f"Invalid token: {str(e)}")


def create_access_token(data: dict) -> str:
    """
    Create a signed JWT.
    Expects 'data' to contain at minimum: {'uid': ..., 'role': ..., 'email': ...}
    """
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode["exp"] = expire
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


# ---------------------------------------------------------------------------
# Push Notifications (Modern HTTP v1)
# ---------------------------------------------------------------------------

def _get_fcm_credentials():
    """Extracts project ID and loads credentials from firebase.json"""
    if not os.path.exists(CREDENTIALS_FILE):
        return None, None
    
    with open(CREDENTIALS_FILE, "r") as f:
        data = json.load(f)
        project_id = data.get("project_id")
    
    credentials = service_account.Credentials.from_service_account_file(
        CREDENTIALS_FILE, scopes=FCM_SCOPES
    )
    return credentials, project_id

def send_push_notification(fcm_token: str, title: str, body: str) -> dict:
    """
    Send a push notification via the modern FCM HTTP v1 API.
    Requires firebase.json in the backend directory.
    If firebase.json is not found, the call is skipped.
    """
    credentials, project_id = _get_fcm_credentials()

    if not credentials or not project_id:
        print("[FCM] WARNING: firebase.json not found — skipping push notification.")
        return {"skipped": True, "reason": "firebase.json not configured"}

    # Refresh the auth token (generates a short-lived OAuth token)
    credentials.refresh(GoogleAuthRequest())

    headers = {
        "Authorization": f"Bearer {credentials.token}",
        "Content-Type": "application/json",
    }
    
    # Note the nested structure required by HTTP v1 'message' object
    payload = {
        "message": {
            "token": fcm_token,
            "notification": {
                "title": title,
                "body": body,
            }
        }
    }

    try:
        with httpx.Client(timeout=10.0) as client:
            response = client.post(
                f"https://fcm.googleapis.com/v1/projects/{project_id}/messages:send",
                headers=headers,
                json=payload,
            )
            response.raise_for_status()
            return response.json()
    except httpx.HTTPStatusError as e:
        print(f"[FCM] HTTP error sending notification: {e.response.status_code} — {e.response.text}")
        return {"error": str(e)}
    except httpx.RequestError as e:
        print(f"[FCM] Network error sending notification: {e}")
        return {"error": str(e)}