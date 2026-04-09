from app.database import SessionLocal
from app.models.user import User

db = SessionLocal()

users = [
    User(firebase_uid="uid1", email="usera@test.com", full_name="User A"),
    User(firebase_uid="uid2", email="userb@test.com", full_name="User B"),
    User(firebase_uid="admin1", email="admin@test.com", full_name="Admin", role="admin")
]

db.add_all(users)
db.commit()
db.close()