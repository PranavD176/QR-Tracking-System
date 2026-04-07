from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.dependencies import get_current_user, get_db
from app.models.package import Package
from app.models.scan import ScanHistory
from app.schemas import PackageCreate

router = APIRouter()

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
            "qr_payload": f"QR_TRACKING:{package.package_id}",
            "description": package.description,
            "status": package.status
        },
        "error": None
    }

@router.get("/packages")
def get_packages(user=Depends(get_current_user), db: Session = Depends(get_db)):
    packages = db.query(Package).filter_by(owner_id=user["uid"]).all()
    
    return {
        "success": True,
        "data": packages,
        "error": None
    }

@router.get("/packages/{package_id}/scans")
def get_package_scans(package_id: str, user=Depends(get_current_user), db: Session = Depends(get_db)):
    package = db.query(Package).filter_by(package_id=package_id, owner_id=user["uid"]).first()
    
    if not package:
        return {"success": False, "data": None, "error": "Package not found"}
    
    scans = db.query(ScanHistory).filter_by(package_id=package_id).all()
    
    return {
        "success": True,
        "data": scans,
        "error": None
    }