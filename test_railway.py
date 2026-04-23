import requests

BASE_URL = "https://qr-tracking-system-production.up.railway.app/api"

def test():
    # Login as admin
    login_data = {
        "email": "admin@test.com",
        "password": "AdminPass123"
    }
    r = requests.post(f"{BASE_URL}/auth/login", json=login_data)
    if r.status_code != 200:
        print("Login failed:", r.text)
        return
        
    token = r.json()["data"]["token"]
    
    # Try getting users (new endpoint)
    headers = {"Authorization": f"Bearer {token}"}
    r_users = requests.get(f"{BASE_URL}/admin/users", headers=headers)
    print("GET /admin/users status:", r_users.status_code)
    print("GET /admin/users response:", r_users.text)

if __name__ == "__main__":
    test()
