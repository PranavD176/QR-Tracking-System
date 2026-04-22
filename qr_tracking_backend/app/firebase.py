from jose import jwt, JWTError
from fastapi import HTTPException
import os
from datetime import datetime, timedelta, timezone
import uuid

JWT_SECRET_KEY = os.getenv("JWT_SECRET_KEY", "super-secret-key-for-testing")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "1440"))

def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire, "aud": "authenticated"})
    # Always cast secret to string properly
    encoded_jwt = jwt.encode(to_encode, str(JWT_SECRET_KEY), algorithm="HS256")
    return encoded_jwt

def verify_token(id_token):
    try:
        decoded = jwt.decode(
            id_token,
            JWT_SECRET_KEY,
            algorithms=["HS256"],
            audience="authenticated"
        )
        return decoded
    except JWTError as e:
        raise HTTPException(status_code=401, detail=f"Invalid token: {str(e)}")

import firebase_admin
from firebase_admin import credentials, messaging
import json

# Initialize Firebase Admin for FCM Push Notifications
# This uses the modern HTTP v1 API which is required since Legacy is dead.
try:
    # First try reading directly from a JSON string env variable (best for Railway)
    firebase_json_str = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
    
    if firebase_json_str:
        cred_dict = json.loads(firebase_json_str)
        cred = credentials.Certificate(cred_dict)
        if not firebase_admin._apps:
            firebase_admin.initialize_app(cred)
            print("Firebase initialized from JSON environment variable.")
    else:
        # Fallback to looking for the JSON file path
        cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS", "qr-based-tracking--system-6da0041c1786.json")
        if os.path.exists(cred_path):
            cred = credentials.Certificate(cred_path)
            if not firebase_admin._apps:
                firebase_admin.initialize_app(cred)
                print("Firebase initialized from credentials file.")
        else:
            print(f"Warning: Firebase credentials not found (checked env and file {cred_path}). Push notifications will not send.")
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
        event_id = str(uuid.uuid4())
        sent_at = datetime.now(timezone.utc).isoformat()

        # Send both notification and data payloads for consistent popup behavior
        # across foreground/background/device states.
        message = messaging.Message(
            token=fcm_token,
            notification=messaging.Notification(title=title, body=body),
            data={
                "title": title,
                "body": body,
                "event_id": event_id,
                "sent_at": sent_at,
            },
            android=messaging.AndroidConfig(
                priority="high",
                ttl=timedelta(hours=1),
                notification=messaging.AndroidNotification(
                    channel_id="qr_tracker_alerts",
                    sound="default",
                ),
            ),
        )
        
        # Send the message
        response = messaging.send(message)
        print(f"Push notification sent successfully! Message ID: {response}; event_id={event_id}")
        return True
        
    except Exception as e:
        print(f"FCM Request Exception (HTTP v1): {str(e)}")
        return False
