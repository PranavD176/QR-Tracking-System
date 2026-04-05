from pydantic import BaseModel

class UserCreate(BaseModel):
    firebase_uid: str
    email: str
    full_name: str
