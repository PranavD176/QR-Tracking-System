from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.dependencies import get_db, get_current_user
from app.models.user import User
from app.models.package import Package
from app.models.scan import ScanHistory
from app.models.alerts import Alert
from app.schemas import UserCreate, PackageCreate, ScanRequest

router = APIRouter()

# Authenticate  APIs

# Register API - Creates a new user in the database
@router.post("/auth/register")
def register(user: UserCreate, db: Session = Depends(get_db)):
    new_user = User(
        firebase_uid=user.firebase_uid,
        email=user.email,
        full_name=user.full_name
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return {"success": True, "data": new_user, "error": None}

# Login API - Verifies Firebase token and returns user info
@router.post("/auth/login")
def login(token: str, db: Session = Depends(get_db)):
    decoded = get_current_user(token)  # Wait, token is str, but get_current_user expects token from security
    # Actually, login should probably use the token directly
    from app.firebase import verify_token
    decoded = verify_token(token)
    user = db.query(User).filter_by(firebase_uid=decoded["uid"]).first()

    return {
        "success": True,
        "data": {
            "user_id": user.user_id,
            "role": user.role
        },
        "error": None
    }

# Create Package API - Creates a new package and returns its QR payload
@router.post("/packages")
def create_package(data: PackageCreate, user=Depends(get_current_user), db: Session = Depends(get_db)):
    package = Package(
        owner_id=user["uid"],
        description=data.description
    )
    db.add(package)
    db.commit()

    return {
        "success": True,
        "data": {
            "package_id": package.package_id,
            "qr_payload": f"QR_TRACKING:{package.package_id}"
        },
        "error": None
    }

# Scan API - Records a scan event and sends alert if package is scanned by non-owner
@router.post("/scan")
def scan(data: ScanRequest, user=Depends(get_current_user), db: Session = Depends(get_db)):
    
    package = db.query(Package).filter_by(package_id=data.package_id).first()

    if not package:
        return {"success": False, "data": None, "error": "Package not found"}

    is_owner = package.owner_id == user["uid"]

    result = "valid" if is_owner else "misplaced"

    scan = ScanHistory(
        package_id=package.package_id,
        scanner_id=user["uid"],
        result=result,
        location_description=data.location_description
    )

    db.add(scan)

    alert_sent = False

    if not is_owner:
        alert = Alert(
            package_id=package.package_id,
            recipient_id=package.owner_id,
            scanned_by_id=user["uid"]
        )
        db.add(alert)
        alert_sent = True

    db.commit()

    return {
        "success": True,
        "data": {
            "result": result,
            "alert_sent": alert_sent
        },
        "error": None
    }