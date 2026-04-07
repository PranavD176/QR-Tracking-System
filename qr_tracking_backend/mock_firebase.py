#!/usr/bin/env python3
"""
Mock Firebase Configuration for Testing
Creates a mock firebase.json and provides mock token verification
"""

import json
import os
from typing import Dict, Any

def create_mock_firebase_config():
    """Create a mock firebase.json file for testing"""
    
    mock_config = {
        "type": "service_account",
        "project_id": "qr-tracking-test",
        "private_key_id": "mock_key_id",
        "private_key": "-----BEGIN PRIVATE KEY-----\nMOCK_PRIVATE_KEY_FOR_TESTING\n-----END PRIVATE KEY-----\n",
        "client_email": "firebase-adminsdk-test@qr-tracking-test.iam.gserviceaccount.com",
        "client_id": "mock_client_id",
        "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        "token_uri": "https://oauth2.googleapis.com/token",
        "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-test%40qr-tracking-test.iam.gserviceaccount.com"
    }
    
    config_path = "firebase.json"
    
    with open(config_path, 'w') as f:
        json.dump(mock_config, f, indent=2)
    
    print(f"Mock Firebase config created: {config_path}")
    return config_path

class MockFirebaseVerifier:
    """Mock Firebase token verifier for testing"""
    
    def __init__(self):
        self.mock_tokens = {
            "test_token_valid": {
                "uid": "test_user_1",
                "email": "user1@test.com",
                "name": "Test User One"
            },
            "test_token_admin": {
                "uid": "test_admin_1", 
                "email": "admin@test.com",
                "name": "Test Admin"
            },
            "test_token_user2": {
                "uid": "test_user_2",
                "email": "user2@test.com", 
                "name": "Test User Two"
            }
        }
    
    def verify_token(self, token: str) -> Dict[str, Any]:
        """Mock token verification"""
        if token in self.mock_tokens:
            return self.mock_tokens[token]
        else:
            raise ValueError("Invalid token")

def patch_firebase_for_testing():
    """Patch firebase module for testing"""
    
    # Create mock firebase config
    create_mock_firebase_config()
    
    # Create a mock firebase verification function
    mock_verifier = MockFirebaseVerifier()
    
    return mock_verifier

if __name__ == "__main__":
    # Create mock firebase config
    mock_verifier = patch_firebase_for_testing()
    
    print("Mock Firebase setup complete!")
    print("\nMock tokens available:")
    for token, data in mock_verifier.mock_tokens.items():
        print(f"  {token}: {data['email']} ({data['uid']})")
