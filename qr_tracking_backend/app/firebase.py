import os
from datetime import datetime, timedelta, timezone
from typing import Any, Dict, Optional

from dotenv import load_dotenv
from fastapi import HTTPException
from jose import JWTError, jwt

load_dotenv()

JWT_ALGORITHM = "HS256"
JWT_AUDIENCE = os.getenv("JWT_AUDIENCE", "authenticated")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "1440"))
JWT_SECRET = os.getenv("JWT_SECRET_KEY") or os.getenv("SUPABASE_JWT_SECRET")

if not JWT_SECRET:
    raise RuntimeError(
        "FATAL: JWT secret is not set. Configure JWT_SECRET_KEY or SUPABASE_JWT_SECRET."
    )


def create_access_token(payload: Dict[str, Any], expires_minutes: Optional[int] = None) -> str:
    """Create a signed JWT for app auth with aud/iat/exp claims."""
    ttl_minutes = expires_minutes or ACCESS_TOKEN_EXPIRE_MINUTES
    now = datetime.now(timezone.utc)
    expires_at = now + timedelta(minutes=ttl_minutes)

    claims = dict(payload)
    claims.update(
        {
            "aud": JWT_AUDIENCE,
            "iat": int(now.timestamp()),
            "exp": int(expires_at.timestamp()),
        }
    )

    return jwt.encode(claims, JWT_SECRET, algorithm=JWT_ALGORITHM)


def verify_token(id_token: str) -> Dict[str, Any]:
    try:
        decoded = jwt.decode(
            id_token,
            JWT_SECRET,
            algorithms=[JWT_ALGORITHM],
            audience=JWT_AUDIENCE,
        )

        # JWT payload is expected to identify user and role for access control.
        missing = [key for key in ("uid", "role", "email") if key not in decoded]
        if missing:
            raise HTTPException(status_code=401, detail=f"Token missing claims: {', '.join(missing)}")

        return decoded
    except HTTPException:
        raise
    except JWTError as e:
        raise HTTPException(status_code=401, detail=f"Invalid token: {str(e)}")

import firebase_admin
from firebase_admin import credentials, messaging

# Initialize Firebase Admin for FCM Push Notifications
# This uses the modern HTTP v1 API which is required since Legacy is dead.
try:
    # Look for the JSON file in the same directory or project root
    cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS", "qr-based-tracking--system-6da0041c1786.json")
    if os.path.exists(cred_path):
        cred = credentials.Certificate(cred_path)
        # Avoid initializing the app multiple times in FastAPI reloads
        if not firebase_admin._apps:
            firebase_admin.initialize_app(cred)
    else:
        print(f"Warning: Firebase credentials file not found at {cred_path}. Push notifications will not send.")
except Exception as e:
    print(f"Failed to initialize Firebase Admin: {e}")

def send_push_notification(fcm_token: str, title: str, body: str) -> bool:
    """
    Sends a push notification to an Android device using the modern Firebase Admin SDK.
    """
    if not firebase_admin._apps:
        print("Warning: Push notification skipped. Firebase not initialized.")
        return False
        
    try:
        # Create the modern FCM v1 message payload
        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body,
            ),
            token=fcm_token,
        )
        
        # Send the message
        response = messaging.send(message)
        print(f"Push notification sent successfully! Message ID: {response}")
        return True
        
    except Exception as e:
        print(f"FCM Request Exception (HTTP v1): {str(e)}")
        return False