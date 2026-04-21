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

    try:
        package = db.query(Package).filter_by(package_id=data.package_id).first()

        if not package:
            return {"success": False, "data": None, "error": "Package not found"}

        is_owner = package.owner_id == user["uid"]
        
        # Check if the user is authorized to scan without triggering a misplaced alert
        is_authorized = False
        if is_owner:
            is_authorized = True
        elif package.destination_user_id == user["uid"]:
            is_authorized = True
        else:
            # Check if user is assigned to any checkpoint
            if package.route_checkpoints:
                for cp in package.route_checkpoints:
                    if cp.get("assigned_user_id") == user["uid"]:
                        is_authorized = True
                        break

        intended_result = "valid" if is_authorized else "misplaced"

        # Prevent duplicate scans
        last_scan = db.query(ScanHistory).filter(
            ScanHistory.package_id == package.package_id,
            ScanHistory.result != "duplicate"
        ).order_by(ScanHistory.scanned_at.desc()).first()

        if last_scan and last_scan.result == intended_result and last_scan.location_description == data.location_description:
            result = "duplicate"
        else:
            result = intended_result

        scan_entry = ScanHistory(
            package_id=package.package_id,
            scanner_id=user["uid"],
            result=result,
            location_description=data.location_description
        )
        db.add(scan_entry)

        alert_sent = False

        if not is_authorized and result != "duplicate":
            alert = Alert(
                package_id=package.package_id,
                recipient_id=package.owner_id,
                scanned_by_id=user["uid"],
                details=data.location_description
            )
            db.add(alert)

            owner = db.query(User).filter_by(user_id=package.owner_id).first()

            if owner and owner.fcm_token:
                try:
                    send_push_notification(
                        owner.fcm_token,
                        "Package Alert 🚨",
                        f"Your package was scanned at {data.location_description}"
                    )
                except Exception as e:
                    print(f"Error sending push notification: {e}")

            alert_sent = True

        db.commit()

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
    except Exception as e:
        import traceback
        traceback.print_exc()
        db.rollback()
        return {"success": False, "data": None, "error": f"Server Error: {str(e)}"}