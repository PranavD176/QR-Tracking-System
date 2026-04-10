from sqlalchemy import Column, String, ForeignKey, Text, Integer
from app.database import Base
import uuid

class Package(Base):
    __tablename__ = "packages"

    package_id           = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    owner_id             = Column(String, ForeignKey("users.user_id"))
    description          = Column(Text)
    status               = Column(String, default="active")
    current_checkpoint   = Column(Integer, default=0)   #  tracks which checkpoint it's at
    current_location     = Column(String, nullable=True) #  last known location label