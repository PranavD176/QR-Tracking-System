from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.dependencies import get_current_user, get_db
from app.models.alerts import Alert

router = APIRouter()

@router.get("/alerts")
def get_alerts(user=Depends(get_current_user), db: Session = Depends(get_db)):

    alerts = db.query(Alert).filter_by(recipient_id=user["uid"], status="sent").all()

    return {
        "success": True,
        "data": alerts,
        "error": None
    }

#Acknowledgement API - Marks an alert as acknowledged
@router.put("/alerts/{alert_id}/acknowledge")
def acknowledge(alert_id: str, user=Depends(get_current_user), db: Session = Depends(get_db)):

    alert = db.query(Alert).filter_by(alert_id=alert_id).first()

    if not alert:
        return {"success": False, "data": None, "error": "Alert not found"}

    alert.status = "acknowledged"
    db.commit()

    return {
        "success": True,
        "data": {"alert_id": alert_id, "status": "acknowledged"},
        "error": None
    }