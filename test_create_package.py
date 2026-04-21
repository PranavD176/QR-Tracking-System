import requests

BASE_URL = "https://qr-tracking-system-production.up.railway.app/api"

def test_create_package():
    # Login
    login_data = {"email": "admin@test.com", "password": "AdminPass123"}
    r = requests.post(f"{BASE_URL}/auth/login", json=login_data)
    if r.status_code != 200:
        print("Login failed:", r.text)
        return
        
    token = r.json()["data"]["token"]
    
    # Try to create a package
    headers = {"Authorization": f"Bearer {token}"}
    pkg_data = {
        "description": "Test Package from Script",
        "destination_user_id": None,
        "destination_address": None
    }
    
    r_pkg = requests.post(f"{BASE_URL}/packages", json=pkg_data, headers=headers)
    print("POST /packages status:", r_pkg.status_code)
    print("POST /packages response:", r_pkg.text)

if __name__ == "__main__":
    test_create_package()
