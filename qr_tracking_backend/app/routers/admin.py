from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.dependencies import require_admin, get_db
from app.models.alerts import Alert
from app.models.package import Package
from app.models.user import User
from typing import Optional

router = APIRouter()


@router.get("/admin/alerts")
def get_all_alerts(
    user=Depends(require_admin),
    db: Session = Depends(get_db),
    status: Optional[str] = Query("sent"),
):
    query = db.query(Alert)

    if status:
        query = query.filter_by(status=status)

    alerts = query.all()

    # Build enriched response matching frontend AdminAlertResponse
    enriched_alerts = []
    for alert in alerts:
        package = db.query(Package).filter_by(package_id=alert.package_id).first()
        owner = db.query(User).filter_by(user_id=package.owner_id).first() if package else None
        scanner = db.query(User).filter_by(user_id=alert.scanned_by_id).first()
        enriched_alerts.append({
            "alert_id": alert.alert_id,
            "package_description": package.description if package else "Unknown",
            "owner_name": owner.full_name if owner else "Unknown",
            "scanned_by_name": scanner.full_name if scanner else "Unknown",
            "location": alert.details or "",
            "created_at": alert.created_at.isoformat() if alert.created_at else None,
        })

    return {
        "success": True,
        "data": enriched_alerts,
        "error": None
    }


@router.get("/admin/users")
def get_all_users(
    user=Depends(require_admin),
    db: Session = Depends(get_db)
):
    users = db.query(User).all()
    user_list = []
    for u in users:
        user_list.append({
            "user_id": u.user_id,
            "email": u.email,
            "full_name": u.full_name,
            "role": u.role
        })
    
    return {
        "success": True,
        "data": user_list,
        "error": None
    }


@router.get("/admin/dashboard")
def get_admin_dashboard(
    user=Depends(require_admin),
    db: Session = Depends(get_db)
):
    from sqlalchemy import func
    from app.models.scan import ScanHistory

    # Calculate stats
    total_scans = db.query(func.count(ScanHistory.scan_id)).scalar() or 0
    received = db.query(func.count(ScanHistory.scan_id)).filter(ScanHistory.result == "valid").scalar() or 0
    misplaced = db.query(func.count(ScanHistory.scan_id)).filter(ScanHistory.result == "misplaced").scalar() or 0
    duplicate = db.query(func.count(ScanHistory.scan_id)).filter(ScanHistory.result == "duplicate").scalar() or 0

    stats = {
        "total": total_scans,
        "received": received,
        "misplaced": misplaced,
        "duplicate": duplicate
    }

    # Get recent scans (latest 5)
    recent_scans_query = db.query(ScanHistory).order_by(ScanHistory.scanned_at.desc()).limit(5).all()
    recent_scans = []
    for s in recent_scans_query:
        # Map "valid" -> "RECEIVED", "misplaced" -> "MISPLACED"
        status_enum = "RECEIVED" if s.result == "valid" else "MISPLACED"
        if s.result == "duplicate":
            status_enum = "DUPLICATE"
            
        # Format time for UI (e.g. "14:45 | Today" or "14:45 | Oct 24")
        # Just send ISO format and let UI handle it, or format here. Let's format here for simplicity or send iso.
        # It's better to send iso and let UI format.
        time_str = s.scanned_at.isoformat() if s.scanned_at else "Unknown"

        recent_scans.append({
            "parcel_id": s.package_id,
            "timestamp": time_str,
            "status": status_enum
        })

    return {
        "success": True,
        "data": {
            "stats": stats,
            "recent_scans": recent_scans
        },
        "error": None
    }