from sqlalchemy import Column, String, ForeignKey, DateTime
from app.database import Base
import uuid
from datetime import datetime, timezone

class ScanHistory(Base):
    __tablename__ = "scan_history"

    scan_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    package_id = Column(String, ForeignKey("packages.package_id"))
    scanner_id = Column(String, ForeignKey("users.user_id"))
    result = Column(String)
    location_description = Column(String)
    scanned_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))