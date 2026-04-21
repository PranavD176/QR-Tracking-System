import requests
import uuid
import os
import jwt # PyJWT or python-jose
from datetime import datetime, timedelta, timezone

BASE_URL = "https://qr-tracking-system-production.up.railway.app/api"
JWT_SECRET_KEY = "super-secret-key-for-testing"

def test_scan():
    # 1. Create a fake token
    to_encode = {
        "sub": "userB@test.com",
        "uid": str(uuid.uuid4()),
        "role": "user"
    }
    expire = datetime.now(timezone.utc) + timedelta(minutes=1440)
    to_encode.update({"exp": expire, "aud": "authenticated"})
    token = jwt.encode(to_encode, JWT_SECRET_KEY, algorithm="HS256")
    
    print("Generated token:", token)
    
    # 2. Make the request
    scan_resp = requests.post(
        f"{BASE_URL}/scan",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "package_id": str(uuid.uuid4()),
            "location_description": "Test Location"
        }
    )
    
    print("Scan Response Code:", scan_resp.status_code)
    print("Scan Response Body:", scan_resp.text)

if __name__ == "__main__":
    test_scan()
