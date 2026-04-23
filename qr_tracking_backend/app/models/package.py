from sqlalchemy import Column, String, ForeignKey, Text, DateTime, JSON
from app.database import Base
import uuid
from datetime import datetime, timezone

class Package(Base):
    __tablename__ = "packages"

    package_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    sender_id = Column(String, ForeignKey("users.user_id"))                # who created it
    receiver_id = Column(String, ForeignKey("users.user_id"))              # final destination user
    route_checkpoints = Column(JSON, nullable=True)   # ordered list of user_ids: ["uid1", "uid2"]
    current_holder_id = Column(String, ForeignKey("users.user_id"), nullable=True)
    description = Column(Text)
    status = Column(String, default="in_transit")      # in_transit | delivered
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))