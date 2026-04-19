"""
auth.py — Authentication router

Replaces Firebase token verification with:
  - bcrypt password hashing via passlib
  - JWT issuance via python-jose (see app/firebase.py for token helpers)

Endpoints:
  POST /api/auth/register       — creates a new user with hashed password
  POST /api/auth/login          — verifies password, returns a signed JWT
  POST /api/auth/device-token   — saves FCM device token for push notifications
"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from passlib.context import CryptContext
from pydantic import BaseModel

from app.dependencies import get_db, get_current_user
from app.models.user import User
from app.schemas import UserCreate, LoginRequest
from app.firebase import create_access_token

router = APIRouter()

# bcrypt context — handles hashing and verification
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


# ---------------------------------------------------------------------------
# Register — POST /api/auth/register
# ---------------------------------------------------------------------------
@router.post("/auth/register")
def register(user: UserCreate, db: Session = Depends(get_db)):
    """
    Register a new user.
    Body: { email, password, full_name, role? }
    """
    if db.query(User).filter_by(email=user.email).first():
        return {"success": False, "data": None, "error": "Email already registered"}

    hashed = pwd_context.hash(user.password)
    # Accept the role from the frontend (default "user" from schema)
    role = user.role if user.role in ("user", "admin", "checkpoint") else "user"
    new_user = User(
        email=user.email,
        full_name=user.full_name,
        hashed_password=hashed,
        role=role,
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return {
        "success": True,
        "data": {
            "user_id": new_user.user_id,
            "email": new_user.email,
            "full_name": new_user.full_name,
        },
        "error": None,
    }


# ---------------------------------------------------------------------------
# Login — POST /api/auth/login
# ---------------------------------------------------------------------------
@router.post("/auth/login")
def login(credentials: LoginRequest, db: Session = Depends(get_db)):
    """
    Login with email + password.
    Body: { email, password }
    Returns a HS256 JWT to be sent as 'Authorization: Bearer <token>'
    on all subsequent requests.

    Response matches Android AuthData:
      { token, token_type, expires_in, user_id, role, email, full_name }
    """
    user = db.query(User).filter_by(email=credentials.email).first()

    if not user or not pwd_context.verify(credentials.password, user.hashed_password):
        return {"success": False, "data": None, "error": "Invalid email or password"}

    token = create_access_token({
        "uid": user.user_id,
        "role": user.role,
        "email": user.email,
    })

    return {
        "success": True,
        "data": {
            "token": token,
            "token_type": "Bearer",
            "expires_in": 1440,
            "user_id": user.user_id,
            "role": user.role,
            "email": user.email,
            "full_name": user.full_name,
        },
        "error": None,
    }


# ---------------------------------------------------------------------------
# Register Device Token — POST /api/auth/device-token
# ---------------------------------------------------------------------------
class DeviceTokenRequest(BaseModel):
    fcm_token: str


@router.post("/auth/device-token")
def register_device_token(
    body: DeviceTokenRequest,
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """
    Called by the Android app (AuthViewModel.sendFcmToken) after every login.
    Saves the device's FCM token against the logged-in user.
    The token is later used in scan.py to push a notification when a package
    belonging to this user is scanned by someone else (misplacement alert).

    Body: { fcm_token: "<device-token-from-firebase-messaging>" }

    Response matches Android UpdatedResponse: { updated: Boolean }
    """
    user = db.query(User).filter_by(user_id=current_user["uid"]).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    user.fcm_token = body.fcm_token
    db.commit()

    return {
        "success": True,
        "data": {"updated": True},
        "error": None,
    }