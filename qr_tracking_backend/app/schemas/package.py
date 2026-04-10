from pydantic import BaseModel
from typing import List

class PackageCreate(BaseModel):
    description: str

class CheckpointItem(BaseModel):
    user_id: str
    label: str
    order: int

class AddCheckpointsRequest(BaseModel):
    package_id: str
    checkpoints: List[CheckpointItem]