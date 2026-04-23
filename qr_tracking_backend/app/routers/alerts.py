from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.dependencies import get_current_user, get_db
from app.models.alerts import Alert
from app.models.package import Package
from app.models.user import User
from typing import Optional

router = APIRouter()


@router.get("/alerts")
def get_alerts(
    user=Depends(get_current_user),
    db: Session = Depends(get_db),
    status: Optional[str] = Query("sent"),
    limit: int = Query(20, ge=1, le=100),
):
    query = db.query(Alert).filter_by(recipient_id=user["uid"])

    if status:
        query = query.filter_by(status=status)

    alerts = query.limit(limit).all()

    # Build enriched response matching frontend AlertResponse
    enriched_alerts = []
    for alert in alerts:
        package = db.query(Package).filter_by(package_id=alert.package_id).first()
        scanner = db.query(User).filter_by(user_id=alert.scanned_by_id).first()
        enriched_alerts.append({
            "alert_id": alert.alert_id,
            "package_id": alert.package_id,
            "package_description": package.description if package else "Unknown",
            "scanned_by_name": scanner.full_name if scanner else "Unknown",
            "alert_type": alert.alert_type or "misplaced",
            "location": alert.details or "",
            "status": alert.status,
            "created_at": alert.created_at.isoformat() if alert.created_at else None,
        })

    return {
        "success": True,
        "data": enriched_alerts,
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


@router.put("/alerts/acknowledge-all")
def acknowledge_all(
    user=Depends(get_current_user),
    db: Session = Depends(get_db),
    status: Optional[str] = Query("sent"),
):
    query = db.query(Alert).filter_by(recipient_id=user["uid"])

    if status:
        query = query.filter_by(status=status)

    alerts = query.all()
    updated = 0

    for alert in alerts:
        if alert.status != "acknowledged":
            alert.status = "acknowledged"
            updated += 1

    db.commit()

    return {
        "success": True,
        "data": {"updated": updated},
        "error": None,
    }
