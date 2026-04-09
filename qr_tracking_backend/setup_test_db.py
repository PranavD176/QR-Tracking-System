#!/usr/bin/env python3
"""
Database Setup Script for Testing
Creates SQLite database with test data
"""

import os
import sys
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
from passlib.context import CryptContext

# Add the app directory to Python path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.database import Base
from app.models.user import User
from app.models.package import Package
from app.models.scan import ScanHistory
from app.models.alerts import Alert

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def setup_test_database():
    """Set up SQLite database for testing"""
    print("Setting up test database...")
    
    # Use SQLite for testing
    DATABASE_URL = "sqlite:///./test_qr_tracking.db"
    
    # Create engine
    engine = create_engine(DATABASE_URL, echo=True)
    
    # Create all tables
    Base.metadata.create_all(bind=engine)
    print("Database tables created successfully!")
    
    # Create session
    SessionLocal = sessionmaker(bind=engine)
    db = SessionLocal()
    
    try:
        # Create test users
        test_users = [
            User(
                email="user1@test.com",
                full_name="Test User One",
                hashed_password=pwd_context.hash("Password123"),
                role="user"
            ),
            User(
                email="user2@test.com",
                full_name="Test User Two",
                hashed_password=pwd_context.hash("Password123"),
                role="user"
            ),
            User(
                email="admin@test.com",
                full_name="Test Admin",
                hashed_password=pwd_context.hash("AdminPass123"),
                role="admin"
            )
        ]
        
        for user in test_users:
            db.add(user)
        db.commit()
        print("Test users created!")
        
        # Create test packages
        test_packages = [
            Package(
                owner_id=test_users[0].user_id,
                description="Important Documents",
                status="active"
            ),
            Package(
                owner_id=test_users[0].user_id,
                description="Laptop Equipment",
                status="active"
            ),
            Package(
                owner_id=test_users[1].user_id,
                description="Medical Supplies",
                status="active"
            )
        ]
        
        for package in test_packages:
            db.add(package)
        db.commit()
        print("Test packages created!")
        
        # Create test scan history
        test_scans = [
            ScanHistory(
                package_id=test_packages[0].package_id,
                scanner_id=test_users[0].user_id,
                result="valid",
                location_description="Office Desk"
            ),
            ScanHistory(
                package_id=test_packages[0].package_id,
                scanner_id=test_users[1].user_id,
                result="misplaced",
                location_description="Conference Room"
            )
        ]
        
        for scan in test_scans:
            db.add(scan)
        db.commit()
        print("Test scan history created!")
        
        # Create test alerts
        test_alerts = [
            Alert(
                package_id=test_packages[0].package_id,
                recipient_id=test_users[0].user_id,
                scanned_by_id=test_users[1].user_id,
                status="sent",
                details="Package scanned by unauthorized user at Conference Room"
            ),
            Alert(
                package_id=test_packages[1].package_id,
                recipient_id=test_users[0].user_id,
                scanned_by_id=test_users[1].user_id,
                status="acknowledged",
                details="Package scanned by unauthorized user at Reception"
            )
        ]
        
        for alert in test_alerts:
            db.add(alert)
        db.commit()
        print("Test alerts created!")
        
        # Print summary
        print("\n=== Test Database Summary ===")
        print(f"Users: {db.query(User).count()}")
        print(f"Packages: {db.query(Package).count()}")
        print(f"Scans: {db.query(ScanHistory).count()}")
        print(f"Alerts: {db.query(Alert).count()}")
        
        print("\nTest database setup completed successfully!")
        print(f"Database file: {DATABASE_URL.replace('sqlite:///', '')}")
        
    except Exception as e:
        print(f"Error setting up database: {e}")
        db.rollback()
        raise
    finally:
        db.close()

if __name__ == "__main__":
    setup_test_database()
