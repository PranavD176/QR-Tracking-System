import os
import sys

# Add parent directory to path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from app.firebase import send_push_notification

def test_fcm_integration():
    print("=== Testing FCM Integration ===")
    
    # We will use a dummy token.
    # Since it is a fake token, we *expect* Firebase to reply with an Error (like InvalidArgument)
    # However, if we get an invalid token error, it proves our backend successfully
    # connected, authenticated using the JSON file, and communicated with Google's servers!
    
    fake_token = "dummy_token_to_test_connection_to_firebase_server"
    title = "Test Notification"
    body = "Checking if the backend can talk to Firebase!"
    
    print("Attempting to send push notification payload to Google...")
    result = send_push_notification(fake_token, title, body)
    
    print("\nDid the function return True? ->", result)
    print("Note: If it printed 'FCM Request Exception (HTTP v1)' above with a 400 Bad Request / Invalid argument or token error, that is actually a SUCCESS! It means Google authenticated us perfectly but rejected the fake destination token.")

if __name__ == "__main__":
    test_fcm_integration()
