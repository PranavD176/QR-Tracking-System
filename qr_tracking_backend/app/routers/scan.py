from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.dependencies import get_current_user, get_db
from app.models.package import Package
from app.models.scan import ScanHistory
from app.models.alerts import Alert
from app.models.user import User
from app.models.checkpoint import Checkpoint
from app.firebase import send_push_notification
from app.schemas import ScanRequest

router = APIRouter()

@router.post("/scan")
def scan(data: ScanRequest, user=Depends(get_current_user), db: Session = Depends(get_db)):

    package = db.query(Package).filter_by(package_id=data.package_id).first()
    if not package:
        return {"success": False, "data": None, "error": "Package not found"}

    scanner_uid = user["uid"]
    scanner     = db.query(User).filter_by(user_id=scanner_uid).first()
    owner       = db.query(User).filter_by(user_id=package.owner_id).first()
    alert_sent  = False

    # Check if scanner is a registered checkpoint for this package
    checkpoint = db.query(Checkpoint).filter_by(
        package_id = data.package_id,
        user_id    = scanner_uid
    ).first()

    is_owner      = package.owner_id == scanner_uid
    is_checkpoint = checkpoint is not None

    # ── CASE 1: Owner scans their own package ──────────────────────────
    if is_owner:
        result = "valid"
        scan_type = "owner_scan"
        message = "You scanned your own package."

    # ── CASE 2: Valid checkpoint user scans ────────────────────────────
    elif is_checkpoint:
        result = "valid"
        scan_type = "checkpoint_reached"

        # Update package's current location
        package.current_checkpoint = checkpoint.order
        package.current_location   = checkpoint.label

        message = f"Package reached checkpoint: {checkpoint.label}"

        # Notify owner about checkpoint update
        if owner and owner.fcm_token:
            send_push_notification(
                owner.fcm_token,
                "📦 Package Update",
                f"Your package reached '{checkpoint.label}' (checkpoint {checkpoint.order})"
            )
            alert_sent = True

        # Save an informational alert for the owner to see in app
        alert = Alert(
            package_id    = package.package_id,
            recipient_id  = package.owner_id,
            scanned_by_id = scanner_uid,
            details       = f"Reached checkpoint: {checkpoint.label} (step {checkpoint.order})"
        )
        db.add(alert)

    # ── CASE 3: Unknown user scans — MISPLACED ─────────────────────────
    else:
        result  = "misplaced"
        scan_type = "misplaced"
        message = "Package scanned by unauthorized user — possible misplacement!"

        # Notify owner with urgent alert
        if owner and owner.fcm_token:
            send_push_notification(
                owner.fcm_token,
                "🚨 Package Alert",
                f"Your package was scanned by an unknown user at {data.location_description}"
            )
            alert_sent = True

        alert = Alert(
            package_id    = package.package_id,
            recipient_id  = package.owner_id,
            scanned_by_id = scanner_uid,
            details       = f"MISPLACED — scanned at {data.location_description} by unknown user"
        )
        db.add(alert)

    # Always save scan history
    scan_entry = ScanHistory(
        package_id           = package.package_id,
        scanner_id           = scanner_uid,
        result               = result,
        location_description = data.location_description
    )
    db.add(scan_entry)
    db.commit()

    return {
        "success": True,
        "data": {
            "result":           result,
            "scan_type":        scan_type,
            "package_description": package.description or "",
            "owner_name":       owner.full_name if owner else "",
            "alert_sent":       alert_sent,
            "scanned_by":       scanner.full_name if scanner else scanner_uid,
            "message":          message,
            "current_location": package.current_location
        },
        "error": None
    }