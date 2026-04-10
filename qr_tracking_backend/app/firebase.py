import os
from datetime import datetime, timedelta, timezone
from typing import Any, Dict, Optional

from dotenv import load_dotenv
from fastapi import HTTPException
from jose import JWTError, jwt

load_dotenv()

JWT_ALGORITHM = "HS256"
JWT_AUDIENCE = os.getenv("JWT_AUDIENCE", "authenticated")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "1440"))
JWT_SECRET = os.getenv("JWT_SECRET_KEY") or os.getenv("SUPABASE_JWT_SECRET")

if not JWT_SECRET:
    raise RuntimeError(
        "FATAL: JWT secret is not set. Configure JWT_SECRET_KEY or SUPABASE_JWT_SECRET."
    )


def create_access_token(payload: Dict[str, Any], expires_minutes: Optional[int] = None) -> str:
    """Create a signed JWT for app auth with aud/iat/exp claims."""
    ttl_minutes = expires_minutes or ACCESS_TOKEN_EXPIRE_MINUTES
    now = datetime.now(timezone.utc)
    expires_at = now + timedelta(minutes=ttl_minutes)

    claims = dict(payload)
    claims.update(
        {
            "aud": JWT_AUDIENCE,
            "iat": int(now.timestamp()),
            "exp": int(expires_at.timestamp()),
        }
    )

    return jwt.encode(claims, JWT_SECRET, algorithm=JWT_ALGORITHM)


def verify_token(id_token: str) -> Dict[str, Any]:
    try:
        decoded = jwt.decode(
            id_token,
            JWT_SECRET,
            algorithms=[JWT_ALGORITHM],
            audience=JWT_AUDIENCE,
        )

        # JWT payload is expected to identify user and role for access control.
        missing = [key for key in ("uid", "role", "email") if key not in decoded]
        if missing:
            raise HTTPException(status_code=401, detail=f"Token missing claims: {', '.join(missing)}")

        return decoded
    except HTTPException:
        raise
    except JWTError as e:
        raise HTTPException(status_code=401, detail=f"Invalid token: {str(e)}")


def send_push_notification(fcm_token: str, title: str, body: str) -> bool:
    # Placeholder until FCM server-side implementation is added.
    return False