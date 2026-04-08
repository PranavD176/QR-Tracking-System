from pydantic import BaseModel

class ScanRequest(BaseModel):
    package_id: str
    location_description: str
