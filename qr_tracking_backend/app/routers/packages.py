from fastapi import APIRouter

router = APIRouter(prefix="/packages", tags=["Packages"])

@router.post("")
def create_package():
    return {
        "success": True,
        "data": {
            "package_id": "uuid-v4",
            "description": "Physics textbook — blue cover",
            "status": "active",
            "owner_id": "uuid-v4",
            "qr_payload": "QR_TRACKING:uuid-v4",
            "created_at": "2026-04-01T10:05:00Z"
        },
        "error": None
    }

@router.get("")
def list_packages():
    return {
        "success": True,
        "data": [
            {
                "package_id": "uuid-v4",
                "description": "Physics textbook",
                "status": "active",
                "created_at": "2026-04-01T10:05:00Z"
            }
        ],
        "error": None
    }

@router.get("/{package_id}/scans")
def get_package_scans(package_id: str):
    return {
        "success": True,
        "data": [
            {
                "scan_id": "uuid-v4",
                "scanner_name": "Priya Patel",
                "result": "misplaced",
                "location_description": "Library Room 3B",
                "scanned_at": "2026-04-01T11:30:00Z"
            }
        ],
        "error": None
    }