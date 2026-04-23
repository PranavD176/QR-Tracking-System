import os
from sqlalchemy import create_engine, text
from dotenv import load_dotenv

load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL")

def alter_db():
    print(f"Connecting to {DATABASE_URL}...")
    engine = create_engine(DATABASE_URL)
    
    with engine.connect() as conn:
        # Fix the created_at bug
        try:
            conn.execute(text("ALTER TABLE packages ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;"))
            print("Successfully added created_at to packages.")
        except Exception as e:
            print(f"Error adding created_at (may already exist): {e}")

        # Add route_checkpoints for the new feature
        try:
            conn.execute(text("ALTER TABLE packages ADD COLUMN route_checkpoints JSONB;"))
            print("Successfully added route_checkpoints to packages.")
        except Exception as e:
            print(f"Error adding route_checkpoints: {e}")
            
        conn.commit()

if __name__ == "__main__":
    alter_db()
