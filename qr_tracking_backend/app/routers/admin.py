from typing import Optional

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session, aliased
from app.dependencies import require_admin, get_db
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

@router.get("/admin/alerts")
def get_all_alerts(user=Depends(require_admin), db: Session = Depends(get_db)):
    owner = aliased(User)
    scanner = aliased(User)

    rows = (
        db.query(
            Alert,
            Package.description.label("package_description"),
            owner.full_name.label("owner_name"),
            scanner.full_name.label("scanned_by_name"),
        )
        .outerjoin(Package, Package.package_id == Alert.package_id)
        .outerjoin(owner, owner.user_id == Alert.recipient_id)
        .outerjoin(scanner, scanner.user_id == Alert.scanned_by_id)
        .filter(Alert.status == "sent")
        .all()
    )

    return {
        "success": True,
        "data": [
            {
                "alert_id": alert.alert_id,
                "package_description": package_description or "Unknown package",
                "owner_name": owner_name or "Unknown",
                "scanned_by_name": scanned_by_name or "Unknown",
                "location": _extract_location(alert.details),
                "created_at": "",
            }
            for alert, package_description, owner_name, scanned_by_name in rows
        ],
        "error": None
    }