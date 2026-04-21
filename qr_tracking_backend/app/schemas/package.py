from pydantic import BaseModel
from typing import Optional, List

class PackageCreate(BaseModel):
    description: str
    receiver_id: str                                    # required — final destination user
    route_checkpoints: Optional[List[str]] = None       # ordered list of user_ids

class PackageUpdateCheckpoints(BaseModel):
    route_checkpoints: List[str]
