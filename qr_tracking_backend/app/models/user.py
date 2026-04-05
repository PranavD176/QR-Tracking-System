# Creting table for user data
from sqlalchemy import Column, String, Enum, Text
from app.database import Base
import uuid

class User(Base):
    __tablename__ = "users"

    user_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    firebase_uid = Column(String, unique=True, nullable=False)
    email = Column(String, unique=True, nullable=False)
    full_name = Column(String, nullable=False)
    role = Column(Enum('user','admin', name='role_enum'), default='user')
    fcm_token = Column(Text, nullable=True)