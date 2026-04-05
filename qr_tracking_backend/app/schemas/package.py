from pydantic import BaseModel

class PackageCreate(BaseModel):
    description: str
