from pydantic import BaseModel


class UserCreate(BaseModel):
    """Schema for user registration — P2P, no role needed."""
    email: str
    password: str
    full_name: str


class LoginRequest(BaseModel):
    """Schema for user login."""
    email: str
    password: str
