"""
add_contact_no.py — Add contact_no column to users table in Supabase.

Run this once to migrate the database:
    python add_contact_no.py
"""
import os
from dotenv import load_dotenv
from sqlalchemy import create_engine, text

load_dotenv()

DATABASE_URL = os.getenv("DATABASE_URL")
if not DATABASE_URL:
    raise RuntimeError("DATABASE_URL not set in .env")

engine = create_engine(DATABASE_URL)

with engine.connect() as conn:
    # Check if column already exists
    result = conn.execute(text("""
        SELECT column_name FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'contact_no'
    """))
    if result.fetchone():
        print("✅ Column 'contact_no' already exists in 'users' table. Nothing to do.")
    else:
        conn.execute(text("ALTER TABLE users ADD COLUMN contact_no VARCHAR"))
        conn.commit()
        print("✅ Added 'contact_no' column to 'users' table successfully.")
