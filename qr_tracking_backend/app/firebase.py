from jose import jwt, JWTError
from fastapi import HTTPException
import os

SUPABASE_JWT_SECRET = os.getenv("SUPABASE_JWT_SECRET")

def verify_token(id_token):
    try:
        decoded = jwt.decode(
            id_token,
            SUPABASE_JWT_SECRET,
            algorithms=["HS256"],
            audience="authenticated"
        )
        return decoded
    except JWTError as e:
        raise HTTPException(status_code=401, detail=f"Invalid token: {str(e)}")

def send_push_notification(fcm_token, title, body):
    # Keep FCM or replace with another provider later
    pass