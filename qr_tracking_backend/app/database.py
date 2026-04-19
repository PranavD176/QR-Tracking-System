from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
import os
from dotenv import load_dotenv

load_dotenv()

# Provide a fallback if .env is missing just in case
DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./test_qr_tracking.db")

if DATABASE_URL.startswith("sqlite"):
    engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
else:
    engine = create_engine(DATABASE_URL)
    
SessionLocal = sessionmaker(bind=engine)

Base = declarative_base()