import unittest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool
import sys
import os

# Add parent directory to path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from main import app
from app.database import Base
from app.dependencies import get_db

class TestAuthSystem(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        """Set up an in-memory SQLite database specifically for testing the Auth endpoints."""
        cls.engine = create_engine(
            'sqlite:///:memory:',
            connect_args={"check_same_thread": False},
            poolclass=StaticPool
        )
        Base.metadata.create_all(cls.engine)
        cls.TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=cls.engine)
        
        # Override the FastAPI `get_db` dependency to use our in-memory SQLite DB
        def override_get_db():
            db = cls.TestingSessionLocal()
            try:
                yield db
            finally:
                db.close()
                
        app.dependency_overrides[get_db] = override_get_db
        cls.client = TestClient(app)

    @classmethod
    def tearDownClass(cls):
        """Clean up the test dependency overrides and close engine after tests."""
        Base.metadata.drop_all(cls.engine)
        app.dependency_overrides.clear()

    def test_1_register_new_user(self):
        """Test the registration of a completely new user"""
        response = self.client.post("/api/auth/register", json={
            "email": "authtest@example.com",
            "password": "SecurePassword123!",
            "full_name": "Auth Tester"
        })
        
        self.assertEqual(response.status_code, 200)
        data = response.json()
        
        self.assertTrue(data.get("success"), "Registration should signal success")
        self.assertIn("token", data["data"], "A JWT token should be returned upon registration")
        self.assertEqual(data["data"]["email"], "authtest@example.com")
        self.assertEqual(data["data"]["full_name"], "Auth Tester")

    def test_2_register_duplicate_user(self):
        """Test that registering an existing email fails safely"""
        # Note: authtest@example.com was created in test_1
        response = self.client.post("/api/auth/register", json={
            "email": "authtest@example.com",
            "password": "DifferentPassword456",
            "full_name": "Auth Tester Copy"
        })
        
        self.assertEqual(response.status_code, 200) # Assuming the app conventionally returns 200 but formats custom error payload
        data = response.json()
        
        self.assertFalse(data.get("success"), "Duplicate registration should not succeed")
        self.assertEqual(data.get("error"), "Email already registered")

    def test_3_login_valid_credentials(self):
        """Test logging in securely and retrieving a valid JWT"""
        response = self.client.post("/api/auth/login", json={
            "email": "authtest@example.com",
            "password": "SecurePassword123!"
        })
        
        self.assertEqual(response.status_code, 200)
        data = response.json()
        
        self.assertTrue(data.get("success"))
        self.assertIn("token", data["data"], "Login must return a JWT token")
        self.assertEqual(data["data"]["email"], "authtest@example.com")

    def test_4_login_invalid_credentials(self):
        """Test logging in with the wrong password"""
        response = self.client.post("/api/auth/login", json={
            "email": "authtest@example.com",
            "password": "WrongPasswordX"
        })
        
        data = response.json()
        self.assertFalse(data.get("success"))
        self.assertEqual(data.get("error"), "Invalid email or password")
        
    def test_5_register_device_token(self):
        """Test attaching an FCM device token using the JWT obtained from login"""
        # First login to get the JWT token
        login_resp = self.client.post("/api/auth/login", json={
            "email": "authtest@example.com",
            "password": "SecurePassword123!"
        })
        token = login_resp.json()["data"]["token"]
        
        # Now hit the device token endpoint with the Bearer scheme
        headers = {"Authorization": f"Bearer {token}"}
        token_payload = {"fcm_token": "fcm_mock_device_token_xxxx_yyyy"}
        
        device_resp = self.client.post(
            "/api/auth/device-token", 
            json=token_payload, 
            headers=headers
        )
        
        self.assertEqual(device_resp.status_code, 200)
        device_data = device_resp.json()
        self.assertTrue(device_data.get("success"))
        self.assertEqual(device_data["data"]["message"], "Device token registered")

if __name__ == "__main__":
    unittest.main()
