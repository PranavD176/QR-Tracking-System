import requests
import json
import uuid

BASE_URL = "https://qr-tracking-system-production.up.railway.app/api"

def test_scan():
    # Login as User B to get token
    login_resp = requests.post(f"{BASE_URL}/auth/login", json={
        "email": "userB@test.com",
        "password": "password123"
    })
    
    if login_resp.status_code != 200:
        print("Login failed:", login_resp.text)
        return
        
    print(login_resp.text)
    token = login_resp.json().get("token") or login_resp.json().get("access_token")
    
    # Send a dummy scan request
    # First let's get a package id, or just make up one
    # If we make up one, it should return 200 OK with success=False, error="Package not found"
    
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
