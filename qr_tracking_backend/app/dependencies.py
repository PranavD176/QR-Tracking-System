"""
dependencies.py — Shared FastAPI dependencies

get_current_user: decodes the Bearer JWT (issued by /auth/login)
                  and returns the payload dict: {uid, email}
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
    The payload always contains: uid, email
    """
    try:
        decoded = verify_token(token.credentials)
        return decoded
    except HTTPException:
        raise
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid or expired token")