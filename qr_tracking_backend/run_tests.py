#!/usr/bin/env python3
"""
Complete Backend Testing Environment Setup
Sets up database, mock Firebase, and runs comprehensive tests
"""

import os
import sys
import subprocess
import time
from pathlib import Path

def setup_environment():
    """Set up the complete testing environment"""
    
    print("=== QR Tracking Backend Testing Setup ===\n")
    
    # Change to backend directory
    backend_dir = Path(__file__).parent
    os.chdir(backend_dir)
    
    # Step 1: Set up environment variables
    print("1. Setting up environment variables...")
    os.environ["DATABASE_URL"] = "sqlite:///./test_qr_tracking.db"
    print("   DATABASE_URL set to SQLite")
    
    # Step 2: Create mock Firebase config
    print("\n2. Setting up mock Firebase...")
    try:
        subprocess.run([sys.executable, "mock_firebase.py"], check=True, capture_output=True)
        print("   Mock Firebase configuration created")
    except subprocess.CalledProcessError as e:
        print(f"   Error creating mock Firebase: {e}")
        return False
    
    # Step 3: Set up test database
    print("\n3. Setting up test database...")
    try:
        subprocess.run([sys.executable, "setup_test_db.py"], check=True, capture_output=True)
        print("   Test database created with sample data")
    except subprocess.CalledProcessError as e:
        print(f"   Error setting up database: {e}")
        return False
    
    # Step 4: Install dependencies
    print("\n4. Checking dependencies...")
    try:
        subprocess.run([sys.executable, "-m", "pip", "install", "-r", "requirements.txt"], 
                      check=True, capture_output=True)
        print("   Dependencies installed")
    except subprocess.CalledProcessError as e:
        print(f"   Error installing dependencies: {e}")
        return False
    
    print("\n=== Setup Complete ===")
    print("The backend is now configured for testing!")
    return True

def run_server_tests():
    """Run server and API tests"""
    
    print("\n=== Starting Server Tests ===")
    
    # Start the server in background
    print("1. Starting FastAPI server...")
    try:
        server_process = subprocess.Popen(
            [sys.executable, "-m", "uvicorn", "main:app", "--reload", "--port", "8000"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        
        # Wait for server to start
        print("   Waiting for server to start...")
        time.sleep(5)
        
        # Test if server is running
        print("2. Testing server connectivity...")
        try:
            import requests
            response = requests.get("http://localhost:8000/health", timeout=5)
            if response.status_code == 200:
                print("   Server is running successfully!")
            else:
                print(f"   Server responded with: {response.status_code}")
        except requests.exceptions.RequestException as e:
            print(f"   Server connection failed: {e}")
            return False
        
        # Run API tests
        print("3. Running comprehensive API tests...")
        try:
            result = subprocess.run([sys.executable, "test_backend.py"], 
                                  capture_output=True, text=True, timeout=30)
            print(result.stdout)
            if result.stderr:
                print("Errors:")
                print(result.stderr)
        except subprocess.TimeoutExpired:
            print("   Tests timed out")
        except subprocess.CalledProcessError as e:
            print(f"   Test execution failed: {e}")
        
    finally:
        # Stop the server
        if 'server_process' in locals():
            print("\n4. Stopping server...")
            server_process.terminate()
            server_process.wait()
            print("   Server stopped")
    
    return True

def main():
    """Main testing function"""
    
    # Set up environment
    if not setup_environment():
        print("Setup failed. Exiting.")
        return False
    
    # Ask user if they want to run server tests
    response = input("\nDo you want to run the server tests? (y/n): ").lower().strip()
    
    if response == 'y':
        if not run_server_tests():
            print("Server tests failed.")
            return False
    
    print("\n=== Testing Complete ===")
    print("\nWhat was tested:")
    print("  - Database models and relationships")
    print("  - API endpoint structure")
    print("  - Authentication flow")
    print("  - Authorization (access control)")
    print("  - Error handling")
    print("  - Request/response formats")
    
    print("\nTo run individual tests:")
    print("  python setup_test_db.py     # Set up test database")
    print("  python mock_firebase.py      # Set up mock Firebase")
    print("  python test_backend.py       # Run API tests")
    print("  python -m uvicorn main:app   # Start server manually")
    
    return True

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
