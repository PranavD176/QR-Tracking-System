from sqlalchemy import Column, String, ForeignKey, Text, Enum, DateTime
from app.database import Base
import uuid
from datetime import datetime, timezone

class Alert(Base):
    __tablename__ = "alerts"

    alert_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    package_id = Column(String, ForeignKey("packages.package_id"))
    recipient_id = Column(String, ForeignKey("users.user_id"))
    scanned_by_id = Column(String, ForeignKey("users.user_id"))
    status = Column(Enum("sent", "acknowledged", name="alert_status"), default="sent")
    details = Column(Text, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))