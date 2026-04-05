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
    try:
        decoded = verify_token(token.credentials)
        return decoded
    except:
        raise HTTPException(status_code=401, detail="Invalid token")

def require_admin(user=Depends(get_current_user)):
    if user.get("role") != "admin":
        raise HTTPException(status_code=403, detail="Forbidden")
    return user