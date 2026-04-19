from sqlalchemy import Column, String, ForeignKey, Text, DateTime, JSON
from app.database import Base
import uuid
from datetime import datetime, timezone

class Package(Base):
    __tablename__ = "packages"

    package_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    owner_id = Column(String, ForeignKey("users.user_id"))
    destination_user_id = Column(String, ForeignKey("users.user_id"), nullable=True)
    destination_address = Column(String, nullable=True)
    route_checkpoints = Column(JSON, nullable=True)
    description = Column(Text)
    status = Column(String, default="active")
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))