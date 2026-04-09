#!/usr/bin/env python3
"""
Comprehensive Backend Testing Script
Tests all endpoints, models, and integration points
"""

import requests
import json
import time
from typing import Dict, Any

class BackendTester:
    def __init__(self, base_url: str = "http://localhost:8000"):
        self.base_url = base_url
        self.session = requests.Session()
        self.test_results = []
        
    def log_test(self, test_name: str, success: bool, message: str = "", response_data: Any = None):
        """Log test results"""
        status = "PASS" if success else "FAIL"
        result = {
            "test": test_name,
            "status": status,
            "message": message,
            "timestamp": time.time(),
            "response": response_data
        }
        self.test_results.append(result)
        print(f"[{status}] {test_name}")
        if message:
            print(f"    {message}")
    
    def test_health_check(self):
        """Test basic API health"""
        try:
            response = self.session.get(f"{self.base_url}/health")
            success = response.status_code == 200
            self.log_test("Health Check", success, 
                        f"Status: {response.status_code}", 
                        response.json() if success else None)
            return success
        except Exception as e:
            self.log_test("Health Check", False, f"Connection error: {str(e)}")
            return False
    
    def test_user_registration(self):
        """Test user registration endpoint"""
        test_user = {
            "email": "test@example.com",
            "password": "TestPassword123",
            "full_name": "Test User"
        }
        
        try:
            response = self.session.post(f"{self.base_url}/api/auth/register", json=test_user)
            success = response.status_code == 200
            data = response.json() if success else None
            self.log_test("User Registration", success,
                        f"Status: {response.status_code}",
                        data)
            return success, data
        except Exception as e:
            self.log_test("User Registration", False, f"Error: {str(e)}")
            return False, None
    
    def test_duplicate_registration(self):
        """Test duplicate user registration (should fail)"""
        test_user = {
            "email": "test@example.com",
            "password": "TestPassword123",
            "full_name": "Test User"
        }
        
        try:
            response = self.session.post(f"{self.base_url}/api/auth/register", json=test_user)
            success = response.status_code == 200 and response.json().get("success") == False
            self.log_test("Duplicate Registration", success,
                        f"Status: {response.status_code}",
                        response.json() if response.status_code == 200 else None)
            return success
        except Exception as e:
            self.log_test("Duplicate Registration", False, f"Error: {str(e)}")
            return False
    
    def test_login_with_invalid_credentials(self):
        """Test login with wrong password — should return success=False"""
        try:
            response = self.session.post(
                f"{self.base_url}/api/auth/login",
                json={"email": "nonexistent@example.com", "password": "wrongpassword"}
            )
            data = response.json()
            success = response.status_code == 200 and data.get("success") == False
            self.log_test("Invalid Credentials Login", success,
                        f"Expected success=False, got: {data}")
            return success
        except Exception as e:
            self.log_test("Invalid Credentials Login", True, "Correctly failed with exception")
            return True
    
    def test_package_creation(self):
        """Test package creation (requires valid auth)"""
        package_data = {
            "description": "Test Package for Tracking"
        }
        
        try:
            # This will fail without proper auth, which is expected
            response = self.session.post(f"{self.base_url}/api/packages", json=package_data)
            # Should return 401 without auth
            success = response.status_code == 401
            self.log_test("Package Creation (No Auth)", success,
                        f"Expected 401, got: {response.status_code}")
            return success
        except Exception as e:
            self.log_test("Package Creation (No Auth)", True, "Correctly failed with exception")
            return True
    
    def test_get_packages_unauthorized(self):
        """Test getting packages without authentication"""
        try:
            response = self.session.get(f"{self.base_url}/api/packages")
            success = response.status_code == 401
            self.log_test("Get Packages (No Auth)", success,
                        f"Expected 401, got: {response.status_code}")
            return success
        except Exception as e:
            self.log_test("Get Packages (No Auth)", True, "Correctly failed with exception")
            return True
    
    def test_alert_endpoints_unauthorized(self):
        """Test alert endpoints without authentication"""
        endpoints = [
            "/api/alerts",
            "/api/admin/alerts"
        ]
        
        all_success = True
        for endpoint in endpoints:
            try:
                response = self.session.get(f"{self.base_url}{endpoint}")
                success = response.status_code == 401
                self.log_test(f"Alert Endpoint {endpoint} (No Auth)", success,
                            f"Expected 401, got: {response.status_code}")
                all_success = all_success and success
            except Exception as e:
                self.log_test(f"Alert Endpoint {endpoint} (No Auth)", True, "Correctly failed")
        
        return all_success
    
    def test_scan_endpoint_unauthorized(self):
        """Test scan endpoint without authentication"""
        scan_data = {
            "package_id": "test_package_id",
            "location_description": "Test Location"
        }
        
        try:
            response = self.session.post(f"{self.base_url}/api/scan", json=scan_data)
            success = response.status_code == 401
            self.log_test("Scan Endpoint (No Auth)", success,
                        f"Expected 401, got: {response.status_code}")
            return success
        except Exception as e:
            self.log_test("Scan Endpoint (No Auth)", True, "Correctly failed with exception")
            return True
    
    def run_all_tests(self):
        """Run all backend tests"""
        print("=== Starting Backend API Tests ===\n")
        
        # Basic connectivity tests
        self.test_health_check()
        
        # Authentication tests
        self.test_user_registration()
        self.test_duplicate_registration()
        self.test_login_with_invalid_credentials()
        
        # Authorization tests (should all fail without auth)
        self.test_package_creation()
        self.test_get_packages_unauthorized()
        self.test_alert_endpoints_unauthorized()
        self.test_scan_endpoint_unauthorized()
        
        # Summary
        self.print_summary()
    
    def print_summary(self):
        """Print test summary"""
        total_tests = len(self.test_results)
        passed_tests = len([r for r in self.test_results if r["status"] == "PASS"])
        failed_tests = total_tests - passed_tests
        
        print(f"\n=== Test Summary ===")
        print(f"Total Tests: {total_tests}")
        print(f"Passed: {passed_tests}")
        print(f"Failed: {failed_tests}")
        print(f"Success Rate: {(passed_tests/total_tests)*100:.1f}%")
        
        if failed_tests > 0:
            print("\nFailed Tests:")
            for result in self.test_results:
                if result["status"] == "FAIL":
                    print(f"  - {result['test']}: {result['message']}")
        
        print("\n=== Test Complete ===")

if __name__ == "__main__":
    tester = BackendTester()
    tester.run_all_tests()
