import firebase_admin
from firebase_admin import credentials, auth, messaging
from functools import wraps
from fastapi import HTTPException

# Initialize Firebase Admin SDK
try:
    cred = credentials.Certificate("firebase.json")
    firebase_admin.initialize_app(cred)
    print(" Firebase Admin SDK initialized")
except Exception as e:
    print(f" Firebase initialization failed: {e}")

def verify_token(id_token):
    """Verify Firebase ID token and return decoded token"""
    try:
        return auth.verify_id_token(id_token)
    except Exception as e:
        raise HTTPException(status_code=401, detail=f"Invalid token: {str(e)}")

def send_push_notification(fcm_token, title, body):
    """Send push notification via FCM"""
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body
        ),
        token=fcm_token
    )
    response = messaging.send(message)
    return response