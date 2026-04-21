import os
import sys
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), 'qr_tracking_backend')))

from app.models.package import Package

DATABASE_URL = "postgresql://postgres:pd%40supabase@db.pvdoarljajftytobjssd.supabase.co:5432/postgres"

def test_db():
    engine = create_engine(DATABASE_URL)
    SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    db = SessionLocal()
    
    try:
        package = Package(
            owner_id="admin@test.com", # wait, owner_id is user_id UUID, but let's see if this fails first
            description="Test DB insert",
            destination_user_id=None,
            destination_address=None
        )
        db.add(package)
        db.commit()
        print("Success")
    except Exception as e:
        print("Error:", e)
    finally:
        db.close()

if __name__ == "__main__":
    test_db()
