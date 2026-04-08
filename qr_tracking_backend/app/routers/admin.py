from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.dependencies import require_admin, get_db
from app.models.alerts import Alert

router = APIRouter()

@router.get("/admin/alerts")
def get_all_alerts(user=Depends(require_admin), db: Session = Depends(get_db)):

    alerts = db.query(Alert).filter_by(status="sent").all()

    return {
        "success": True,
        "data": alerts,
        "error": None
    }