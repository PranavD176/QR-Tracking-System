from fastapi import APIRouter

router = APIRouter(prefix="/scan", tags=["Scan"])

@router.post("")
def record_scan():
    return {
        "success": True,
        "data": {
            "result": "valid",
            "package_description": "Physics textbook — blue cover",
            "owner_name": "Rahul Sharma",
            "alert_sent": False
        },
        "error": None
    }