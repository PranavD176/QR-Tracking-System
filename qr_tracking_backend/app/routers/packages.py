from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.dependencies import get_current_user, get_db
from app.models.package import Package
from app.models.scan import ScanHistory
from app.models.checkpoint import Checkpoint
from app.models.user import User
from app.schemas import PackageCreate
from app.schemas.package import AddCheckpointsRequest

router = APIRouter()


# ── CREATE PACKAGE ────────────────────────────────────────────────────────────
@router.post("/packages")
def create_package(data: PackageCreate, user=Depends(get_current_user), db: Session = Depends(get_db)):
    package = Package(
        owner_id=user["uid"],
        description=data.description
    )
    db.add(package)
    db.commit()
    db.refresh(package)

    return {
        "success": True,
        "data": {
            "package_id": package.package_id,
            "owner_id": package.owner_id,
            "qr_payload": f"QR_TRACKING:{package.package_id}",
            "description": package.description,
            "status": package.status,
            "created_at": "",
        },
        "error": None
    }


# ── GET ALL PACKAGES (owner only sees their own) ──────────────────────────────
@router.get("/packages")
def get_packages(user=Depends(get_current_user), db: Session = Depends(get_db)):
    packages = db.query(Package).filter_by(owner_id=user["uid"]).all()

    return {
        "success": True,
        "data": [
            {
                "package_id": pkg.package_id,
                "description": pkg.description,
                "status": pkg.status,
                "owner_id": pkg.owner_id,
                "qr_payload": f"QR_TRACKING:{pkg.package_id}",
                "created_at": "",
            }
            for pkg in packages
        ],
        "error": None
    }


# ── GET SCAN HISTORY FOR A PACKAGE ───────────────────────────────────────────
@router.get("/packages/{package_id}/scans")
def get_package_scans(package_id: str, user=Depends(get_current_user), db: Session = Depends(get_db)):
    package = db.query(Package).filter_by(package_id=package_id, owner_id=user["uid"]).first()

    if not package:
        return {"success": False, "data": None, "error": "Package not found"}

    scans = (
        db.query(ScanHistory, User.full_name.label("scanner_name"))
        .outerjoin(User, User.user_id == ScanHistory.scanner_id)
        .filter(ScanHistory.package_id == package_id)
        .all()
    )

    return {
        "success": True,
        "data": [
            {
                "scan_id": scan.scan_id,
                "scanner_name": scanner_name or "Unknown",
                "result": scan.result,
                "location_description": scan.location_description,
                "scanned_at": "",
            }
            for scan, scanner_name in scans
        ],
        "error": None
    }


# ── ADD CHECKPOINTS TO A PACKAGE (owner only) ─────────────────────────────────
@router.post("/packages/checkpoints")
def add_checkpoints(data: AddCheckpointsRequest, user=Depends(get_current_user), db: Session = Depends(get_db)):
    # Only the package owner can assign checkpoints
    package = db.query(Package).filter_by(package_id=data.package_id, owner_id=user["uid"]).first()

    if not package:
        return {"success": False, "data": None, "error": "Package not found or you are not the owner"}

    # Remove old checkpoints before saving new ones
    db.query(Checkpoint).filter_by(package_id=data.package_id).delete()

    for cp in data.checkpoints:
        checkpoint = Checkpoint(
            package_id=data.package_id,
            user_id=cp.user_id,
            label=cp.label,
            order=cp.order
        )
        db.add(checkpoint)

    db.commit()

    return {
        "success": True,
        "data": f"{len(data.checkpoints)} checkpoint(s) saved for package {data.package_id}",
        "error": None
    }


# ── GET CHECKPOINTS FOR A PACKAGE ─────────────────────────────────────────────
@router.get("/packages/{package_id}/checkpoints")
def get_checkpoints(package_id: str, user=Depends(get_current_user), db: Session = Depends(get_db)):
    package = db.query(Package).filter_by(package_id=package_id, owner_id=user["uid"]).first()

    if not package:
        return {"success": False, "data": None, "error": "Package not found or you are not the owner"}

    checkpoints = db.query(Checkpoint).filter_by(package_id=package_id).order_by(Checkpoint.order).all()

    return {
        "success": True,
        "data": [
            {
                "checkpoint_id": cp.checkpoint_id,
                "user_id": cp.user_id,
                "label": cp.label,
                "order": cp.order
            }
            for cp in checkpoints
        ],
        "error": None
    }


# ── DELETE A SINGLE CHECKPOINT ─────────────────────────────────────────────────
@router.delete("/packages/checkpoints/{checkpoint_id}")
def delete_checkpoint(checkpoint_id: str, user=Depends(get_current_user), db: Session = Depends(get_db)):
    checkpoint = db.query(Checkpoint).filter_by(checkpoint_id=checkpoint_id).first()

    if not checkpoint:
        return {"success": False, "data": None, "error": "Checkpoint not found"}

    # Make sure only the package owner can delete checkpoints
    package = db.query(Package).filter_by(package_id=checkpoint.package_id, owner_id=user["uid"]).first()

    if not package:
        return {"success": False, "data": None, "error": "Not authorized to delete this checkpoint"}

    db.delete(checkpoint)
    db.commit()

    return {
        "success": True,
        "data": f"Checkpoint {checkpoint_id} deleted",
        "error": None
    }