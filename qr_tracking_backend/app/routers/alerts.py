from fastapi import APIRouter

router = APIRouter(prefix="/alerts", tags=["Alerts"])

@router.get("")
def get_alerts():
    return {
        "success": True,
        "data": [
            {
                "alert_id": "uuid-v4",
                "package_id": "uuid-v4",
                "package_description": "Physics textbook",
                "scanned_by_name": "Priya Patel",
                "location": "Library Room 3B",
                "status": "sent",
                "created_at": "2026-04-01T11:30:00Z"
            }
        ],
        "error": None
    }

@router.put("/{alert_id}/acknowledge")
def acknowledge_alert(alert_id: str):
    return {
        "success": True,
        "data": {"alert_id": alert_id, "status": "acknowledged"},
        "error": None
    }