"""
dependencies.py — Shared FastAPI dependencies

get_current_user: decodes the Bearer JWT (issued by /auth/login)
                  and returns the payload dict: {uid, role, email}
require_admin:    raises 403 if the current user is not an admin
"""
from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer
from sqlalchemy.orm import Session

from app.database import SessionLocal
from app.firebase import verify_token

security = HTTPBearer()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def get_current_user(token=Depends(security)):
    """
    Validates the Bearer JWT and returns the decoded payload.
    The payload always contains: uid, role, email
    """
    try:
        decoded = verify_token(token.credentials)
        return decoded
    except HTTPException:
        raise
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid or expired token")


def require_admin(user=Depends(get_current_user)):
    """Raises 403 if the authenticated user does not have the 'admin' role."""
    if user.get("role") != "admin":
        raise HTTPException(status_code=403, detail="Admin access required")
    return user