"""
auth.py — Authentication router (P2P — no roles)

Endpoints:
  POST /api/auth/register       — creates a new user with hashed password
  POST /api/auth/login          — verifies password, returns a signed JWT
  POST /api/auth/device-token   — saves FCM device token for push notifications
  GET  /api/auth/users          — returns all users (for sender to pick receiver/intermediates)
"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from passlib.context import CryptContext
from pydantic import BaseModel
from typing import Optional

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
    Body: { email, password, full_name }
    """
    if db.query(User).filter_by(email=user.email).first():
        return {"success": False, "data": None, "error": "Email already registered"}

    hashed = pwd_context.hash(user.password)
    new_user = User(
        email=user.email,
        full_name=user.full_name,
        hashed_password=hashed,
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
    Returns a HS256 JWT to be sent as 'Authorization: Bearer <token>'
    """
    user = db.query(User).filter_by(email=credentials.email).first()

    if not user or not pwd_context.verify(credentials.password, user.hashed_password):
        return {"success": False, "data": None, "error": "Invalid email or password"}

    token = create_access_token({
        "uid": user.user_id,
        "email": user.email,
    })

    return {
        "success": True,
        "data": {
            "token": token,
            "token_type": "Bearer",
            "expires_in": 1440,
            "user_id": user.user_id,
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
    Called by the Android app after every login.
    Saves the device's FCM token against the logged-in user.
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


# ---------------------------------------------------------------------------
# Get All Users — GET /api/auth/users
# Any logged-in user can search for other users to pick as receiver/intermediates
# ---------------------------------------------------------------------------
@router.get("/auth/users")
def get_all_users(
    current_user=Depends(get_current_user),
    db: Session = Depends(get_db),
    search: Optional[str] = Query(None),
):
    """
    Returns all users (id, name, email) so the sender can pick
    receiver and intermediates when creating a parcel.
    Optional search query filters by name or email.
    """
    query = db.query(User)

    if search:
        search_term = f"%{search}%"
        from sqlalchemy import or_
        query = query.filter(
            or_(
                User.full_name.ilike(search_term),
                User.email.ilike(search_term),
            )
        )

    users = query.limit(50).all()

    user_list = [
        {
            "user_id": u.user_id,
            "email": u.email,
            "full_name": u.full_name,
        }
        for u in users
        if u.user_id != current_user["uid"]  # exclude self
    ]

    return {
        "success": True,
        "data": user_list,
        "error": None,
    }