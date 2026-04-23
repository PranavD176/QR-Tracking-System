import os
import sys
import unittest

from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.database import Base
from app.dependencies import get_db
from app.firebase import create_access_token
from app.models.alerts import Alert
from app.models.package import Package
from app.models.user import User
from main import app


class TestAlertsRouter(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.engine = create_engine(
            "sqlite:///:memory:",
            connect_args={"check_same_thread": False},
            poolclass=StaticPool,
        )
        cls.SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=cls.engine)
        Base.metadata.create_all(bind=cls.engine)

        def override_get_db():
            db = cls.SessionLocal()
            try:
                yield db
            finally:
                db.close()

        app.dependency_overrides[get_db] = override_get_db
        cls.client = TestClient(app)

    @classmethod
    def tearDownClass(cls):
        app.dependency_overrides.clear()
        Base.metadata.drop_all(bind=cls.engine)

    def setUp(self):
        db = self.SessionLocal()
        try:
            db.query(Alert).delete()
            db.query(Package).delete()
            db.query(User).delete()
            db.commit()
        finally:
            db.close()

    def _create_user(self, user_id: str, email: str) -> User:
        db = self.SessionLocal()
        try:
            user = User(
                user_id=user_id,
                email=email,
                full_name=email.split("@")[0],
                hashed_password="hashed",
            )
            db.add(user)
            db.commit()
            db.refresh(user)
            return user
        finally:
            db.close()

    def _create_package(self, sender_id: str, receiver_id: str) -> Package:
        db = self.SessionLocal()
        try:
            pkg = Package(
                sender_id=sender_id,
                receiver_id=receiver_id,
                current_holder_id=sender_id,
                description="test",
                status="in_transit",
            )
            db.add(pkg)
            db.commit()
            db.refresh(pkg)
            return pkg
        finally:
            db.close()

    def _create_alert(self, package_id: str, recipient_id: str, scanned_by_id: str, status: str) -> Alert:
        db = self.SessionLocal()
        try:
            alert = Alert(
                package_id=package_id,
                recipient_id=recipient_id,
                scanned_by_id=scanned_by_id,
                status=status,
                alert_type="misplaced",
                details="loc",
            )
            db.add(alert)
            db.commit()
            db.refresh(alert)
            return alert
        finally:
            db.close()

    def _auth_header(self, user_id: str, email: str) -> dict:
        token = create_access_token({"uid": user_id, "email": email})
        return {"Authorization": f"Bearer {token}"}

    def test_acknowledge_all_marks_only_current_user_unread_alerts(self):
        user_a = self._create_user("U1234567", "a@test.com")
        user_b = self._create_user("U7654321", "b@test.com")
        pkg = self._create_package(sender_id=user_a.user_id, receiver_id=user_b.user_id)

        alert_a_sent = self._create_alert(pkg.package_id, user_a.user_id, user_b.user_id, "sent")
        alert_a_ack = self._create_alert(pkg.package_id, user_a.user_id, user_b.user_id, "acknowledged")
        alert_b_sent = self._create_alert(pkg.package_id, user_b.user_id, user_a.user_id, "sent")

        response = self.client.put(
            "/api/alerts/acknowledge-all",
            headers=self._auth_header(user_a.user_id, user_a.email),
        )

        self.assertEqual(response.status_code, 200)
        payload = response.json()
        self.assertTrue(payload["success"])
        self.assertEqual(payload["data"]["updated"], 1)

        db = self.SessionLocal()
        try:
            updated_a_sent = db.query(Alert).filter_by(alert_id=alert_a_sent.alert_id).first()
            unchanged_a_ack = db.query(Alert).filter_by(alert_id=alert_a_ack.alert_id).first()
            unchanged_b_sent = db.query(Alert).filter_by(alert_id=alert_b_sent.alert_id).first()

            self.assertEqual(updated_a_sent.status, "acknowledged")
            self.assertEqual(unchanged_a_ack.status, "acknowledged")
            self.assertEqual(unchanged_b_sent.status, "sent")
        finally:
            db.close()

    def test_single_acknowledge_rejects_non_recipient(self):
        user_a = self._create_user("A1234567", "a2@test.com")
        user_b = self._create_user("B1234567", "b2@test.com")
        pkg = self._create_package(sender_id=user_a.user_id, receiver_id=user_b.user_id)

        alert = self._create_alert(pkg.package_id, user_a.user_id, user_b.user_id, "sent")

        response = self.client.put(
            f"/api/alerts/{alert.alert_id}/acknowledge",
            headers=self._auth_header(user_b.user_id, user_b.email),
        )

        self.assertEqual(response.status_code, 200)
        payload = response.json()
        self.assertFalse(payload["success"])
        self.assertEqual(payload["error"], "Alert not found")

        db = self.SessionLocal()
        try:
            still_sent = db.query(Alert).filter_by(alert_id=alert.alert_id).first()
            self.assertEqual(still_sent.status, "sent")
        finally:
            db.close()


if __name__ == "__main__":
    unittest.main()
