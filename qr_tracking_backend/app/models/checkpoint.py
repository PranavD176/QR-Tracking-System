from sqlalchemy import Column, String, Integer, ForeignKey
from app.database import Base
import uuid

class Checkpoint(Base):
    __tablename__ = "checkpoints"

    checkpoint_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    package_id    = Column(String, ForeignKey("packages.package_id"), nullable=False)
    user_id       = Column(String, ForeignKey("users.user_id"), nullable=False)
    label         = Column(String, nullable=False)   # e.g. "Warehouse A", "Gate 2"
    order         = Column(Integer, nullable=False)  # 1, 2, 3... expected sequence