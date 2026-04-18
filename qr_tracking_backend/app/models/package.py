from sqlalchemy import Column, String, ForeignKey, Text, DateTime
from app.database import Base
import uuid
from datetime import datetime, timezone

class Package(Base):
    __tablename__ = "packages"

    package_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    owner_id = Column(String, ForeignKey("users.user_id"))
    description = Column(Text)
    status = Column(String, default="active")
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))