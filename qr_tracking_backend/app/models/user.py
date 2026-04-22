# User model — P2P architecture: no role column, all users are equal.
# hashed_password added for self-managed bcrypt authentication.
from sqlalchemy import Column, String, Text
from app.database import Base
from app.utils.user_id import new_short_user_id


class User(Base):
    __tablename__ = "users"

    user_id = Column(String, primary_key=True, default=lambda: new_short_user_id(8))
    email = Column(String, unique=True, nullable=False)
    full_name = Column(String, nullable=False)
    hashed_password = Column(String, nullable=False)
    fcm_token = Column(Text, nullable=True)