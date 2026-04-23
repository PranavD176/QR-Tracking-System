"""
scan.py — P2P Scan Router

Authorization: sender ∪ receiver ∪ route_checkpoints
Valid scan → update current_holder, notify next person
Scanner == receiver → mark delivered, notify sender
Misplaced → alert both sender AND receiver
"""
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.dependencies import get_current_user, get_db
from app.models.package import Package
from app.models.scan import ScanHistory
from app.models.alerts import Alert
from app.models.user import User
from app.firebase import send_push_notification
from app.schemas import ScanRequest

router = APIRouter()

@router.post("/scan")
def scan(data: ScanRequest, user=Depends(get_current_user), db: Session = Depends(get_db)):

    try:
        package = db.query(Package).filter_by(package_id=data.package_id).first()

        if not package:
            return {"success": False, "data": None, "error": "Package not found"}

        # Build authorized set: sender + receiver + all checkpoint user_ids
        authorized_ids = set()
        authorized_ids.add(package.sender_id)
        if package.receiver_id:
            authorized_ids.add(package.receiver_id)
        if package.route_checkpoints:
            authorized_ids.update(package.route_checkpoints)  # now just a list of user_ids

        is_authorized = user["uid"] in authorized_ids
        is_sender = package.sender_id == user["uid"]
        is_receiver = package.receiver_id == user["uid"]

        intended_result = "valid" if is_authorized else "misplaced"

        # Prevent duplicate scans
        last_scan = db.query(ScanHistory).filter(
            ScanHistory.package_id == package.package_id,
            ScanHistory.result != "duplicate"
        ).order_by(ScanHistory.scanned_at.desc()).first()

        if last_scan and last_scan.result == intended_result and last_scan.location_description == data.location_description:
            result = "duplicate"
        else:
            result = intended_result

        scan_entry = ScanHistory(
            package_id=package.package_id,
            scanner_id=user["uid"],
            result=result,
            location_description=data.location_description
        )
        db.add(scan_entry)

        alert_sent = False

        if is_authorized and result != "duplicate":
            # ── Valid scan ─────────────────────────────────────────────
            # Update current holder
            package.current_holder_id = user["uid"]

            if is_receiver:
                # Receiver scanned → mark delivered, notify sender
                package.status = "delivered"
                sender = db.query(User).filter_by(user_id=package.sender_id).first()
                if sender and sender.fcm_token:
                    try:
                        send_push_notification(
                            sender.fcm_token,
                            "Parcel Delivered ✅",
                            f"Your package '{package.description}' has been received!"
                        )
                    except Exception as e:
                        print(f"Error sending push: {e}")
            else:
                # Checkpoint or sender scan → notify next in chain
                _notify_next_in_chain(package, user["uid"], db)

        elif not is_authorized and result != "duplicate":
            # ── Misplaced scan ─────────────────────────────────────────
            # Alert sender AND receiver, but deduplicate so the same user
            # doesn't receive two identical alerts (e.g. solo testing).
            unique_recipients = set(filter(None, [package.sender_id, package.receiver_id]))
            for recipient_id in unique_recipients:
                alert = Alert(
                    package_id=package.package_id,
                    recipient_id=recipient_id,
                    scanned_by_id=user["uid"],
                    alert_type="misplaced",
                    details=data.location_description
                )
                db.add(alert)

                recipient = db.query(User).filter_by(user_id=recipient_id).first()
                if recipient and recipient.fcm_token:
                    try:
                        send_push_notification(
                            recipient.fcm_token,
                            "Package Alert 🚨",
                            f"Your package was scanned by an unauthorized person at {data.location_description}"
                        )
                    except Exception as e:
                        print(f"Error sending push notification: {e}")

            alert_sent = True


        db.commit()

        sender = db.query(User).filter_by(user_id=package.sender_id).first()
        scanner = db.query(User).filter_by(user_id=user["uid"]).first()

        return {
            "success": True,
            "data": {
                "result": result,
                "package_description": package.description or "",
                "sender_name": sender.full_name if sender else "Unknown",
                "alert_sent": alert_sent,
                "scanned_by": scanner.full_name if scanner and not is_sender else None,
                "status": package.status,
            },
            "error": None
        }
    except Exception as e:
        import traceback
        traceback.print_exc()
        db.rollback()
        return {"success": False, "data": None, "error": f"Server Error: {str(e)}"}


def _notify_next_in_chain(package: Package, scanner_uid: str, db: Session):
    """
    Notify the next person in the route chain.
    Chain order: sender → checkpoint_1 → checkpoint_2 → ... → receiver
    """
    chain = [package.sender_id]
    if package.route_checkpoints:
        chain.extend(package.route_checkpoints)
    if package.receiver_id:
        chain.append(package.receiver_id)

    try:
        current_idx = chain.index(scanner_uid)
        if current_idx + 1 < len(chain):
            next_uid = chain[current_idx + 1]
            next_user = db.query(User).filter_by(user_id=next_uid).first()
            if next_user and next_user.fcm_token:
                send_push_notification(
                    next_user.fcm_token,
                    "Parcel on its way 🚚",
                    f"Package '{package.description}' is heading to you next!"
                )
    except (ValueError, IndexError):
        pass  # scanner not in chain or no next person