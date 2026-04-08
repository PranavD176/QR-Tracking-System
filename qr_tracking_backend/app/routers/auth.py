from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.dependencies import get_db
from app.models.user import User
from app.schemas import UserCreate
from app.firebase import verify_token

router = APIRouter()

# Register API - Creates a new user in the database
@router.post("/auth/register")
def register(user: UserCreate, db: Session = Depends(get_db)):
    # Check if user already exists
    existing_user = db.query(User).filter_by(firebase_uid=user.firebase_uid).first()
    if existing_user:
        return {"success": False, "data": None, "error": "User already exists"}
    
    existing_email = db.query(User).filter_by(email=user.email).first()
    if existing_email:
        return {"success": False, "data": None, "error": "Email already registered"}
    
    new_user = User(
        firebase_uid=user.firebase_uid,
        email=user.email,
        full_name=user.full_name
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return {"success": True, "data": new_user, "error": None}

# Login API - Verifies Firebase token and returns user info
@router.post("/auth/login")
def login(token: str, db: Session = Depends(get_db)):
    try:
        # Verify Firebase token directly
        decoded = verify_token(token)
        user = db.query(User).filter_by(firebase_uid=decoded["uid"]).first()
        
        if not user:
            return {"success": False, "data": None, "error": "User not found"}
        
        return {
            "success": True,
            "data": {
                "user_id": user.user_id,
                "role": user.role,
                "email": user.email,
                "full_name": user.full_name
            },
            "error": None
        }
    except Exception as e:
        return {"success": False, "data": None, "error": f"Authentication failed: {str(e)}"}