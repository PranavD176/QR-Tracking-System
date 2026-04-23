"""
packages.py — P2P Package Router

Any logged-in user can create parcels and assign a receiver + intermediates.
"""
from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import or_
from app.dependencies import get_current_user, get_db
from app.models.package import Package
from app.models.scan import ScanHistory
from app.models.user import User
from app.schemas import PackageCreate, PackageUpdateCheckpoints
from app.firebase import send_push_notification
from typing import Optional

router = APIRouter()


def _serialize_package(pkg: Package, db: Session = None) -> dict:
    """Serialize a Package ORM object to match frontend PackageResponse."""
    sender_name = None
    receiver_name = None
    current_holder_name = None
    route_checkpoint_names = []
    scanned_checkpoint_count = 0

    if db:
        sender = db.query(User).filter_by(user_id=pkg.sender_id).first()
        sender_name = sender.full_name if sender else None
        receiver = db.query(User).filter_by(user_id=pkg.receiver_id).first()
        receiver_name = receiver.full_name if receiver else None
        if pkg.current_holder_id:
            holder = db.query(User).filter_by(user_id=pkg.current_holder_id).first()
            current_holder_name = holder.full_name if holder else None

        # Resolve checkpoint user IDs to names
        if pkg.route_checkpoints:
            for cp_uid in pkg.route_checkpoints:
                cp_user = db.query(User).filter_by(user_id=cp_uid).first()
                route_checkpoint_names.append(cp_user.full_name if cp_user else cp_uid)

        # Count how many checkpoints have been scanned (valid result)
        if pkg.route_checkpoints:
            scanned_ids = set()
            scans = db.query(ScanHistory).filter_by(
                package_id=pkg.package_id
            ).filter(ScanHistory.result == "valid").all()
            for scan in scans:
                if scan.scanner_id in pkg.route_checkpoints:
                    scanned_ids.add(scan.scanner_id)
            scanned_checkpoint_count = len(scanned_ids)

    return {
        "package_id": pkg.package_id,
        "description": pkg.description,
        "status": pkg.status,
        "sender_id": pkg.sender_id,
        "sender_name": sender_name,
        "receiver_id": pkg.receiver_id,
        "receiver_name": receiver_name,
        "current_holder_id": pkg.current_holder_id,
        "current_holder_name": current_holder_name,
        "route_checkpoints": pkg.route_checkpoints,
        "route_checkpoint_names": route_checkpoint_names,
        "scanned_checkpoint_count": scanned_checkpoint_count,
        "qr_payload": f"QR_TRACKING:{pkg.package_id}",
        "created_at": pkg.created_at.isoformat() if pkg.created_at else None,
    }


@router.post("/packages")
def create_package(data: PackageCreate, user=Depends(get_current_user), db: Session = Depends(get_db)):
    """
    Any user can create a parcel.
    sender_id = current user, receiver_id and route_checkpoints from body.
    """
    try:
        # Verify receiver exists
        receiver = db.query(User).filter_by(user_id=data.receiver_id).first()
        if not receiver:
            return {"success": False, "data": None, "error": "Receiver user not found"}

        package = Package(
            sender_id=user["uid"],
            receiver_id=data.receiver_id,
            description=data.description,
            route_checkpoints=data.route_checkpoints,
            current_holder_id=user["uid"],
            status="pending_acceptance"
        )
        db.add(package)
        db.commit()
        db.refresh(package)

        # Send alert to receiver to accept
        if receiver.fcm_token:
            sender_user = db.query(User).filter_by(user_id=user["uid"]).first()
            sender_name = sender_user.full_name if sender_user else "Someone"
            send_push_notification(
                receiver.fcm_token,
                "New Parcel Request 📦",
                f"{sender_name} wants to send you '{data.description}'. Please accept it."
            )
        
        from app.models.alerts import Alert
        alert = Alert(
            package_id=package.package_id,
            recipient_id=receiver.user_id,
            scanned_by_id=user["uid"], # Using scanned_by to store the requester for now
            alert_type="acceptance_request",
            details="Acceptance pending"
        )
        db.add(alert)
        db.commit()

        # Notify all checkpoint users
        if data.route_checkpoints:
            for uid in data.route_checkpoints:
                cp_user = db.query(User).filter_by(user_id=uid).first()
                if cp_user and cp_user.fcm_token:
                    send_push_notification(
                        cp_user.fcm_token,
                        "You're a Checkpoint 🚚",
                        f"A parcel '{data.description}' will pass through you."
                    )

        return {
            "success": True,
            "data": _serialize_package(package, db),
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
    """
    Returns parcels where user is sender, receiver, OR in route_checkpoints.
    """
    uid = user["uid"]

    # Start with parcels where user is sender or receiver
    query = db.query(Package).filter(
        or_(
            Package.sender_id == uid,
            Package.receiver_id == uid,
        )
    )

    if status:
        query = query.filter_by(status=status)

    packages = query.order_by(Package.created_at.desc()).offset(offset).limit(limit).all()

    # Also include packages where user is in route_checkpoints (JSON field)
    # SQLAlchemy JSON filtering varies by DB — fetch all and filter in Python for SQLite compat
    all_packages = db.query(Package).all()
    checkpoint_packages = []
    for pkg in all_packages:
        if pkg.route_checkpoints and uid in pkg.route_checkpoints:
            if pkg not in packages:
                if status is None or pkg.status == status:
                    checkpoint_packages.append(pkg)

    combined = list(packages) + checkpoint_packages

    return {
        "success": True,
        "data": [_serialize_package(p, db) for p in combined],
        "error": None
    }


@router.get("/packages/{package_id}/scans")
def get_package_scans(package_id: str, user=Depends(get_current_user), db: Session = Depends(get_db)):
    """Get scan history — available to sender, receiver, and checkpoint users."""
    package = db.query(Package).filter_by(package_id=package_id).first()

    if not package:
        return {"success": False, "data": None, "error": "Package not found"}

    # Check user is authorized to view scans
    uid = user["uid"]
    authorized = uid in [package.sender_id, package.receiver_id]
    if not authorized and package.route_checkpoints:
        authorized = uid in package.route_checkpoints

    if not authorized:
        return {"success": False, "data": None, "error": "Not authorized to view this package"}

    scans = db.query(ScanHistory).filter_by(package_id=package_id).order_by(ScanHistory.scanned_at.desc()).all()

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


@router.put("/packages/{package_id}/checkpoints")
def update_checkpoints(
    package_id: str,
    data: PackageUpdateCheckpoints,
    user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """
    Sender can modify route checkpoints after creation.
    """
    package = db.query(Package).filter_by(package_id=package_id).first()

    if not package:
        return {"success": False, "data": None, "error": "Package not found"}

    if package.sender_id != user["uid"]:
        return {"success": False, "data": None, "error": "Only the sender can modify checkpoints"}

    if package.status == "delivered":
        return {"success": False, "data": None, "error": "Cannot modify delivered package"}

    package.route_checkpoints = data.route_checkpoints
    db.commit()

    return {
        "success": True,
        "data": _serialize_package(package, db),
        "error": None
    }


@router.put("/packages/{package_id}/accept")
def accept_package(
    package_id: str,
    user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    package = db.query(Package).filter_by(package_id=package_id).first()
    if not package:
        return {"success": False, "data": None, "error": "Package not found"}
    if package.receiver_id != user["uid"]:
        return {"success": False, "data": None, "error": "Only receiver can accept"}
    if package.status != "pending_acceptance":
        return {"success": False, "data": None, "error": "Package not pending acceptance"}
        
    package.status = "in_transit"
    
    # Resolve alerts
    from app.models.alerts import Alert
    alerts = db.query(Alert).filter_by(package_id=package_id, alert_type="acceptance_request").all()
    for a in alerts:
        a.status = "acknowledged"
        
    db.commit()
    
    # Notify sender
    sender = db.query(User).filter_by(user_id=package.sender_id).first()
    if sender and sender.fcm_token:
        receiver_user = db.query(User).filter_by(user_id=user["uid"]).first()
        send_push_notification(
            sender.fcm_token,
            "Parcel Accepted ✅",
            f"{receiver_user.full_name} accepted your parcel."
        )

    # Notify all checkpoint users
    if package.route_checkpoints:
        for uid in package.route_checkpoints:
            cp_user = db.query(User).filter_by(user_id=uid).first()
            if cp_user and cp_user.fcm_token:
                send_push_notification(
                    cp_user.fcm_token,
                    "You're a Checkpoint 🚚",
                    f"A parcel '{package.description}' will pass through you."
                )
                
    return {"success": True, "data": _serialize_package(package, db), "error": None}

@router.put("/packages/{package_id}/reject")
def reject_package(
    package_id: str,
    user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    package = db.query(Package).filter_by(package_id=package_id).first()
    if not package:
        return {"success": False, "data": None, "error": "Package not found"}
    if package.receiver_id != user["uid"]:
        return {"success": False, "data": None, "error": "Only receiver can reject"}
    if package.status != "pending_acceptance":
        return {"success": False, "data": None, "error": "Package not pending acceptance"}
        
    package.status = "rejected"
    
    # Resolve alerts
    from app.models.alerts import Alert
    alerts = db.query(Alert).filter_by(package_id=package_id, alert_type="acceptance_request").all()
    for a in alerts:
        a.status = "acknowledged"
        
    db.commit()
    
    # Notify sender
    sender = db.query(User).filter_by(user_id=package.sender_id).first()
    if sender and sender.fcm_token:
        receiver_user = db.query(User).filter_by(user_id=user["uid"]).first()
        send_push_notification(
            sender.fcm_token,
            "Parcel Rejected ❌",
            f"{receiver_user.full_name} rejected your parcel."
        )
                
    return {"success": True, "data": _serialize_package(package, db), "error": None}


@router.get("/dashboard")
def get_personal_dashboard(
    user=Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """Personal dashboard — stats scoped to the current user."""
    uid = user["uid"]

    sent_count = db.query(Package).filter_by(sender_id=uid).count()
    received_count = db.query(Package).filter_by(receiver_id=uid, status="delivered").count()
    in_transit = db.query(Package).filter(
        or_(Package.sender_id == uid, Package.receiver_id == uid),
        Package.status == "in_transit"
    ).count()
    delivered = db.query(Package).filter(
        or_(Package.sender_id == uid, Package.receiver_id == uid),
        Package.status == "delivered"
    ).count()

    # Recent activity
    recent_scans = db.query(ScanHistory).join(
        Package, Package.package_id == ScanHistory.package_id
    ).filter(
        or_(Package.sender_id == uid, Package.receiver_id == uid)
    ).order_by(ScanHistory.scanned_at.desc()).limit(5).all()

    recent = []
    for s in recent_scans:
        status_label = "RECEIVED" if s.result == "valid" else "MISPLACED"
        if s.result == "duplicate":
            status_label = "DUPLICATE"
        recent.append({
            "parcel_id": s.package_id,
            "timestamp": s.scanned_at.isoformat() if s.scanned_at else "Unknown",
            "status": status_label,
        })

    return {
        "success": True,
        "data": {
            "stats": {
                "sent": sent_count,
                "received": received_count,
                "in_transit": in_transit,
                "delivered": delivered,
            },
            "recent_scans": recent,
        },
        "error": None,
    }