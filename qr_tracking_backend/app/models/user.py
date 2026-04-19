# User model — firebase_uid removed; identity is now user_id (UUID).
# hashed_password added for self-managed bcrypt authentication.
from sqlalchemy import Column, String, Enum, Text
from app.database import Base
import uuid


class User(Base):
    __tablename__ = "users"

    user_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    email = Column(String, unique=True, nullable=False)
    full_name = Column(String, nullable=False)
    hashed_password = Column(String, nullable=False)
    role = Column(Enum('user', 'admin', name='role_enum'), default='user')
    fcm_token = Column(Text, nullable=True)