from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.dependencies import get_current_user, get_db
from app.models.package import Package
from app.models.scan import ScanHistory
from app.models.user import User
from app.schemas import PackageCreate
from app.firebase import send_push_notification
from typing import Optional

router = APIRouter()


def _serialize_package(pkg: Package) -> dict:
    """Serialize a Package ORM object to match frontend PackageResponse."""
    return {
        "package_id": pkg.package_id,
        "description": pkg.description,
        "status": pkg.status,
        "owner_id": pkg.owner_id,
        "destination_user_id": pkg.destination_user_id,
        "destination_address": pkg.destination_address,
        "route_checkpoints": pkg.route_checkpoints,
        "qr_payload": f"QR_TRACKING:{pkg.package_id}",
        "created_at": pkg.created_at.isoformat() if pkg.created_at else None,
    }


@router.post("/packages")
def create_package(data: PackageCreate, user=Depends(get_current_user), db: Session = Depends(get_db)):
    try:
        package = Package(
            owner_id=user["uid"],
            description=data.description,
            destination_user_id=data.destination_user_id,
            destination_address=data.destination_address,
            route_checkpoints=data.route_checkpoints
        )
        db.add(package)
        db.commit()
        db.refresh(package)

        if data.destination_user_id:
            destination_user = db.query(User).filter(User.user_id == data.destination_user_id).first()
            if destination_user and destination_user.fcm_token:
                send_push_notification(
                    destination_user.fcm_token,
                    "Package assigned to you",
                    f"A new package '{data.description}' has been assigned to you for delivery to {data.destination_address or 'Unknown address'}."
                )

        return {
            "success": True,
            "data": _serialize_package(package),
            "error": None
        }
    except Exception as e:
        import traceback
        return {
            "success": False,
            "data": None,
            "error": f"Internal Error: {str(e)}\n{traceback.format_exc()}"
        }


@router.get("/packages")
def get_packages(
    user=Depends(get_current_user),
    db: Session = Depends(get_db),
    status: Optional[str] = Query(None),
    limit: int = Query(50, ge=1, le=200),
    offset: int = Query(0, ge=0),
):
    from sqlalchemy import or_
    query = db.query(Package).filter(
        or_(
            Package.owner_id == user["uid"],
            Package.destination_user_id == user["uid"]
        )
    )

    if status:
        query = query.filter_by(status=status)

    packages = query.offset(offset).limit(limit).all()

    return {
        "success": True,
        "data": [_serialize_package(p) for p in packages],
        "error": None
    }


@router.get("/packages/{package_id}/scans")
def get_package_scans(package_id: str, user=Depends(get_current_user), db: Session = Depends(get_db)):
    package = db.query(Package).filter_by(package_id=package_id, owner_id=user["uid"]).first()

    if not package:
        return {"success": False, "data": None, "error": "Package not found"}

    scans = db.query(ScanHistory).filter_by(package_id=package_id).all()

    # Build enriched response matching frontend ScanHistoryResponse
    enriched_scans = []
    for scan in scans:
        scanner = db.query(User).filter_by(user_id=scan.scanner_id).first()
        enriched_scans.append({
            "scan_id": scan.scan_id,
            "scanner_name": scanner.full_name if scanner else "Unknown",
            "result": scan.result,
            "location_description": scan.location_description,
            "scanned_at": scan.scanned_at.isoformat() if scan.scanned_at else None,
        })

    return {
        "success": True,
        "data": enriched_scans,
        "error": None
    }