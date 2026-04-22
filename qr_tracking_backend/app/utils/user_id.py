import hashlib
import uuid

BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"


def _base62_encode(number: int) -> str:
    if number == 0:
        return BASE62_ALPHABET[0]

    chars = []
    base = len(BASE62_ALPHABET)
    value = number

    while value > 0:
        value, remainder = divmod(value, base)
        chars.append(BASE62_ALPHABET[remainder])

    return "".join(reversed(chars))


def deterministic_short_user_id(seed: str, length: int = 8, salt: int = 0) -> str:
    if length <= 0:
        raise ValueError("length must be positive")

    source = seed if salt == 0 else f"{seed}:{salt}"
    digest = hashlib.sha256(source.encode("utf-8")).digest()
    number = int.from_bytes(digest[:8], "big")

    encoded = _base62_encode(number)
    if len(encoded) < length:
        encoded = encoded.rjust(length, BASE62_ALPHABET[0])

    return encoded[:length]


def new_short_user_id(length: int = 8) -> str:
    # Random seed ensures IDs for new users are not guessable.
    seed = uuid.uuid4().hex
    return deterministic_short_user_id(seed=seed, length=length)
