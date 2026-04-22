import os
from sqlalchemy import create_engine, text
from dotenv import load_dotenv
from passlib.context import CryptContext

load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL")

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def reset_passwords():
    print(f"Connecting to {DATABASE_URL}...")
    engine = create_engine(DATABASE_URL)
    
    # Hash the new password "Pass@123"
    new_hashed_password = pwd_context.hash("Pass@123")
    
    with engine.connect() as conn:
        try:
            # Note: the hashed_password column was recently added, but let's make sure it's being populated
            # for all users in the db
            conn.execute(text(f"UPDATE users SET hashed_password = '{new_hashed_password}';"))
            conn.commit()
            print("Successfully updated all users to password 'Pass@123'")
            
        except Exception as e:
            print(f"Error updating passwords: {e}")

if __name__ == "__main__":
    reset_passwords()
