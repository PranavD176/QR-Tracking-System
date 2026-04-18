from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.dependencies import get_current_user, get_db
from app.models.package import Package
from app.models.scan import ScanHistory
from app.models.alerts import Alert
from app.models.user import User
from app.firebase import send_push_notification
from app.schemas import ScanRequest

router = APIRouter()

@router.post("/scan")
def scan(data: ScanRequest, user=Depends(get_current_user), db: Session = Depends(get_db)):

    package = db.query(Package).filter_by(package_id=data.package_id).first()

    if not package:
        return {"success": False, "data": None, "error": "Package not found"}

    is_owner = package.owner_id == user["uid"]
    result = "valid" if is_owner else "misplaced"

    scan_entry = ScanHistory(
        package_id=package.package_id,
        scanner_id=user["uid"],
        result=result,
        location_description=data.location_description
    )
    db.add(scan_entry)

    alert_sent = False

    if not is_owner:
        alert = Alert(
            package_id=package.package_id,
            recipient_id=package.owner_id,
            scanned_by_id=user["uid"],
            details=data.location_description
        )
        db.add(alert)

        owner = db.query(User).filter_by(user_id=package.owner_id).first()

        if owner and owner.fcm_token:
            send_push_notification(
                owner.fcm_token,
                "Package Alert 🚨",
                f"Your package was scanned at {data.location_description}"
            )

        alert_sent = True

    db.commit()

    # Get scanner and owner info for enriched response
    # matching frontend ScanResponse
    owner = db.query(User).filter_by(user_id=package.owner_id).first()
    scanner = db.query(User).filter_by(user_id=user["uid"]).first()

    return {
        "success": True,
        "data": {
            "result": result,
            "package_description": package.description or "",
            "owner_name": owner.full_name if owner else "Unknown",
            "alert_sent": alert_sent,
            "scanned_by": scanner.full_name if scanner and not is_owner else None,
        },
        "error": None
    }