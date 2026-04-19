from pydantic import BaseModel
from typing import Optional

class PackageCreate(BaseModel):
    description: str
    destination_user_id: Optional[str] = None
    destination_address: Optional[str] = None
