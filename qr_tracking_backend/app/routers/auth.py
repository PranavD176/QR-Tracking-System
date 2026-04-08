from fastapi import APIRouter

router = APIRouter(prefix="/auth", tags=["Auth"])

@router.post("/register")
def register():
    return {
        "success": True,
        "data": {
            "user_id": "uuid-v4",
            "email": "user@example.com",
            "full_name": "Rahul Sharma",
            "role": "user",
            "created_at": "2026-04-01T10:00:00Z"
        },
        "error": None
    }

@router.post("/login")
def login():
    return {
        "success": True,
        "data": {
            "token": "eyJhbGciOiJSUzI1...",
            "token_type": "bearer",
            "expires_in": 3600,
            "user_id": "uuid-v4",
            "role": "user"
        },
        "error": None
    }

@router.post("/device-token")
def register_device_token():
    return {
        "success": True,
        "data": {"updated": True},
        "error": None
    }