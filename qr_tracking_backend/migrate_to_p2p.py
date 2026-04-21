import os
from sqlalchemy import create_engine, text
from dotenv import load_dotenv

load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./qr_tracking.db")

def migrate_to_p2p():
    print(f"Connecting to {DATABASE_URL}...")
    engine = create_engine(DATABASE_URL)
    
    with engine.connect() as conn:
        try:
            # For SQLite (which seems to be the default), ALTER TABLE drop/rename column is tricky,
            # but modern SQLite supports RENAME COLUMN.
            
            # 1. Users table changes
            try:
                # Remove role column (SQLite doesn't easily support DROP COLUMN in older versions, 
                # but newer ones do. We will try DROP COLUMN first).
                conn.execute(text("ALTER TABLE users DROP COLUMN role;"))
                print("Dropped 'role' column from users.")
            except Exception as e:
                print(f"Note: Could not drop 'role' from users (might be unsupported or already dropped): {e}")

            # 2. Packages table changes
            try:
                conn.execute(text("ALTER TABLE packages RENAME COLUMN owner_id TO sender_id;"))
                print("Renamed 'owner_id' to 'sender_id' in packages.")
            except Exception as e:
                print(f"Note: Could not rename owner_id (might already be renamed): {e}")

            try:
                conn.execute(text("ALTER TABLE packages RENAME COLUMN destination_user_id TO receiver_id;"))
                print("Renamed 'destination_user_id' to 'receiver_id' in packages.")
            except Exception as e:
                print(f"Note: Could not rename destination_user_id (might already be renamed): {e}")

            try:
                conn.execute(text("ALTER TABLE packages DROP COLUMN destination_address;"))
                print("Dropped 'destination_address' from packages.")
            except Exception as e:
                print(f"Note: Could not drop 'destination_address' (might already be dropped): {e}")

            try:
                conn.execute(text("ALTER TABLE packages ADD COLUMN current_holder_id VARCHAR;"))
                print("Added 'current_holder_id' to packages.")
            except Exception as e:
                print(f"Note: Could not add 'current_holder_id': {e}")
                
            # Default new status
            try:
                conn.execute(text("UPDATE packages SET status = 'in_transit' WHERE status = 'active';"))
                conn.execute(text("UPDATE packages SET status = 'delivered' WHERE status = 'completed';"))
                print("Updated package statuses.")
            except Exception as e:
                print(f"Error updating statuses: {e}")

            # 3. Alerts table changes
            try:
                conn.execute(text("ALTER TABLE alerts ADD COLUMN alert_type VARCHAR DEFAULT 'misplaced';"))
                print("Added 'alert_type' to alerts.")
            except Exception as e:
                print(f"Note: Could not add 'alert_type': {e}")

            conn.commit()
            print("Migration to P2P architecture completed successfully.")
            
        except Exception as e:
            print(f"Migration failed: {e}")

if __name__ == "__main__":
    migrate_to_p2p()
