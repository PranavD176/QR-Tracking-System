from fastapi import APIRouter

router = APIRouter(prefix="/admin", tags=["Admin"])

@router.get("/alerts")
def get_all_alerts():
    return {
        "success": True,
        "data": [
            {
                "alert_id": "uuid-v4",
                "package_description": "Physics textbook",
                "owner_name": "Rahul Sharma",
                "scanned_by_name": "Priya Patel",
                "location": "Library Room 3B",
                "created_at": "2026-04-01T11:30:00Z"
            }
        ],
        "error": None
    }