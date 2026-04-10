from typing import Optional

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.dependencies import get_current_user, get_db
from app.models.alerts import Alert
from app.models.package import Package
from app.models.user import User

router = APIRouter()


def _extract_location(details: Optional[str]) -> str:
    if not details:
        return "Unknown"

    if "scanned at " in details:
        return details.split("scanned at ", 1)[1].split(" by ", 1)[0].strip()

    if "Reached checkpoint:" in details:
        return details.split("Reached checkpoint:", 1)[1].split("(", 1)[0].strip()

    return details

@router.get("/alerts")
def get_alerts(user=Depends(get_current_user), db: Session = Depends(get_db)):
    rows = (
        db.query(
            Alert,
            Package.description.label("package_description"),
            User.full_name.label("scanned_by_name"),
        )
        .outerjoin(Package, Package.package_id == Alert.package_id)
        .outerjoin(User, User.user_id == Alert.scanned_by_id)
        .filter(Alert.recipient_id == user["uid"], Alert.status == "sent")
        .all()
    )

    return {
        "success": True,
        "data": [
            {
                "alert_id": alert.alert_id,
                "package_id": alert.package_id,
                "package_description": package_description or "Unknown package",
                "scanned_by_name": scanned_by_name or "Unknown",
                "location": _extract_location(alert.details),
                "status": alert.status,
                "created_at": "",
            }
            for alert, package_description, scanned_by_name in rows
        ],
        "error": None
    }

#Acknowledgement API - Marks an alert as acknowledged
@router.put("/alerts/{alert_id}/acknowledge")
def acknowledge(alert_id: str, user=Depends(get_current_user), db: Session = Depends(get_db)):
    alert = db.query(Alert).filter_by(alert_id=alert_id, recipient_id=user["uid"]).first()

    if not alert:
        return {"success": False, "data": None, "error": "Alert not found"}

    alert.status = "acknowledged"
    db.commit()

    return {
        "success": True,
        "data": {"alert_id": alert_id, "status": "acknowledged"},
        "error": None
    }