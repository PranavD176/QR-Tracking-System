from pydantic import BaseModel
from typing import Optional, List, Dict, Any

class PackageCreate(BaseModel):
    description: str
    destination_user_id: Optional[str] = None
    destination_address: Optional[str] = None
    route_checkpoints: Optional[List[Dict[str, Any]]] = None
