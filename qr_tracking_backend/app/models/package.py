from sqlalchemy import Column, String, ForeignKey, Text
from app.database import Base
import uuid

class Package(Base):
    __tablename__ = "packages"

    package_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    owner_id = Column(String, ForeignKey("users.user_id"))
    description = Column(Text)
    status = Column(String, default="active")