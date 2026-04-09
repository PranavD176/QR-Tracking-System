from pydantic import BaseModel, EmailStr


class UserCreate(BaseModel):
    """Schema for user registration — replaces firebase_uid with password."""
    email: str
    password: str
    full_name: str


class LoginRequest(BaseModel):
    """Schema for user login."""
    email: str
    password: str
