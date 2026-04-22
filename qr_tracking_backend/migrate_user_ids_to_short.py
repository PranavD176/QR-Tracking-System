#!/usr/bin/env python3
import argparse
import json
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Iterable, List, Tuple

from sqlalchemy import text

from app.database import SessionLocal
from app.models.alerts import Alert
from app.models.package import Package
from app.models.scan import ScanHistory
from app.models.user import User
from app.utils.user_id import BASE62_ALPHABET, deterministic_short_user_id


SHORT_ID_LENGTH = 8


@dataclass
class MigrationStats:
    users_updated: int = 0
    package_sender_updated: int = 0
    package_receiver_updated: int = 0
    package_holder_updated: int = 0
    package_checkpoints_updated: int = 0
    scans_updated: int = 0
    alerts_recipient_updated: int = 0
    alerts_scanned_by_updated: int = 0


def _is_short_base62(value: str, length: int = SHORT_ID_LENGTH) -> bool:
    return len(value) == length and all(ch in BASE62_ALPHABET for ch in value)


def _build_mapping(user_ids: Iterable[str]) -> Tuple[Dict[str, str], int]:
    mapping: Dict[str, str] = {}
    used: set[str] = set()
    collision_resolutions = 0

    # Deterministic ordering gives deterministic mappings across reruns.
    for old_id in sorted(user_ids):
        if _is_short_base62(old_id):
            candidate = old_id
            if candidate in used:
                raise ValueError(f"Duplicate existing short user_id detected: {candidate}")
            mapping[old_id] = candidate
            used.add(candidate)
            continue

        salt = 0
        while True:
            candidate = deterministic_short_user_id(old_id, SHORT_ID_LENGTH, salt=salt)
            if candidate not in used:
                mapping[old_id] = candidate
                used.add(candidate)
                if salt > 0:
                    collision_resolutions += 1
                break
            salt += 1

    return mapping, collision_resolutions


def _remap_checkpoints(value, mapping: Dict[str, str]):
    if value is None:
        return value

    if isinstance(value, list):
        return [mapping.get(item, item) for item in value]

    if isinstance(value, str):
        try:
            parsed = json.loads(value)
        except json.JSONDecodeError:
            return value

        if isinstance(parsed, list):
            return [mapping.get(item, item) for item in parsed]

    return value


def _collect_reference_issues(db, known_user_ids: set[str]) -> List[str]:
    issues: List[str] = []

    for pkg in db.query(Package).all():
        if pkg.sender_id and pkg.sender_id not in known_user_ids:
            issues.append(f"packages.sender_id orphan: package={pkg.package_id}, user={pkg.sender_id}")
        if pkg.receiver_id and pkg.receiver_id not in known_user_ids:
            issues.append(f"packages.receiver_id orphan: package={pkg.package_id}, user={pkg.receiver_id}")
        if pkg.current_holder_id and pkg.current_holder_id not in known_user_ids:
            issues.append(
                f"packages.current_holder_id orphan: package={pkg.package_id}, user={pkg.current_holder_id}"
            )

        checkpoints = _remap_checkpoints(pkg.route_checkpoints, {})
        if isinstance(checkpoints, list):
            for checkpoint_user_id in checkpoints:
                if checkpoint_user_id not in known_user_ids:
                    issues.append(
                        "packages.route_checkpoints orphan: "
                        f"package={pkg.package_id}, user={checkpoint_user_id}"
                    )

    for scan in db.query(ScanHistory).all():
        if scan.scanner_id and scan.scanner_id not in known_user_ids:
            issues.append(f"scan_history.scanner_id orphan: scan={scan.scan_id}, user={scan.scanner_id}")

    for alert in db.query(Alert).all():
        if alert.recipient_id and alert.recipient_id not in known_user_ids:
            issues.append(
                f"alerts.recipient_id orphan: alert={alert.alert_id}, user={alert.recipient_id}"
            )
        if alert.scanned_by_id and alert.scanned_by_id not in known_user_ids:
            issues.append(
                f"alerts.scanned_by_id orphan: alert={alert.alert_id}, user={alert.scanned_by_id}"
            )

    return issues


def _apply_migration(db, mapping: Dict[str, str], dialect_name: str) -> MigrationStats:
    stats = MigrationStats()

    if dialect_name == "sqlite":
        db.execute(text("PRAGMA foreign_keys = OFF"))
    elif dialect_name == "postgresql":
        # Works when constraints are DEFERRABLE.
        db.execute(text("SET CONSTRAINTS ALL DEFERRED"))

    # Update referencing columns first.
    for pkg in db.query(Package).all():
        if pkg.sender_id in mapping and pkg.sender_id != mapping[pkg.sender_id]:
            pkg.sender_id = mapping[pkg.sender_id]
            stats.package_sender_updated += 1

        if pkg.receiver_id in mapping and pkg.receiver_id != mapping[pkg.receiver_id]:
            pkg.receiver_id = mapping[pkg.receiver_id]
            stats.package_receiver_updated += 1

        if pkg.current_holder_id in mapping and pkg.current_holder_id != mapping[pkg.current_holder_id]:
            pkg.current_holder_id = mapping[pkg.current_holder_id]
            stats.package_holder_updated += 1

        remapped = _remap_checkpoints(pkg.route_checkpoints, mapping)
        if remapped != pkg.route_checkpoints:
            pkg.route_checkpoints = remapped
            stats.package_checkpoints_updated += 1

    for scan in db.query(ScanHistory).all():
        if scan.scanner_id in mapping and scan.scanner_id != mapping[scan.scanner_id]:
            scan.scanner_id = mapping[scan.scanner_id]
            stats.scans_updated += 1

    for alert in db.query(Alert).all():
        if alert.recipient_id in mapping and alert.recipient_id != mapping[alert.recipient_id]:
            alert.recipient_id = mapping[alert.recipient_id]
            stats.alerts_recipient_updated += 1

        if alert.scanned_by_id in mapping and alert.scanned_by_id != mapping[alert.scanned_by_id]:
            alert.scanned_by_id = mapping[alert.scanned_by_id]
            stats.alerts_scanned_by_updated += 1

    # Finally update users primary keys.
    users = db.query(User).all()
    for user in users:
        old_id = user.user_id
        new_id = mapping[old_id]
        if old_id != new_id:
            user.user_id = new_id
            stats.users_updated += 1

    db.flush()

    if dialect_name == "sqlite":
        db.execute(text("PRAGMA foreign_keys = ON"))

    return stats


def _verify_post_migration_integrity(db) -> List[str]:
    known_user_ids = {user.user_id for user in db.query(User).all()}
    return _collect_reference_issues(db, known_user_ids)


def _save_mapping(path: Path, mapping: Dict[str, str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "total": len(mapping),
        "mapping": mapping,
    }
    path.write_text(json.dumps(payload, indent=2), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Migrate backend users.user_id to deterministic 8-char IDs")
    parser.add_argument(
        "--apply",
        action="store_true",
        help="Apply migration. If omitted, the script runs in dry-run mode.",
    )
    parser.add_argument(
        "--mapping-output",
        default="migration_user_id_map.json",
        help="Path to write old->new mapping JSON (written on apply).",
    )
    args = parser.parse_args()

    db = SessionLocal()

    try:
        dialect_name = db.bind.dialect.name
        users = db.query(User).all()
        old_user_ids = [user.user_id for user in users]

        if not old_user_ids:
            print("No users found. Nothing to migrate.")
            return 0

        mapping, collision_resolutions = _build_mapping(old_user_ids)
        known_user_ids = set(old_user_ids)
        pre_issues = _collect_reference_issues(db, known_user_ids)

        print("=== User ID Migration Preview ===")
        print(f"Database dialect: {dialect_name}")
        print(f"Users: {len(old_user_ids)}")
        print(f"Collision resolutions used: {collision_resolutions}")
        print(f"Pre-migration reference issues: {len(pre_issues)}")

        preview_items = list(mapping.items())[:10]
        print("Preview mappings (first 10):")
        for old_id, new_id in preview_items:
            print(f"  {old_id} -> {new_id}")

        if pre_issues:
            print("Found pre-migration data integrity issues:")
            for issue in pre_issues[:50]:
                print(f"  - {issue}")
            if len(pre_issues) > 50:
                print(f"  ... and {len(pre_issues) - 50} more")
            print("Aborting. Fix orphan references before applying migration.")
            return 1

        if not args.apply:
            print("Dry-run complete. Re-run with --apply to execute migration.")
            return 0

        stats = _apply_migration(db, mapping, dialect_name)
        post_issues = _verify_post_migration_integrity(db)
        if post_issues:
            raise RuntimeError(
                "Post-migration integrity check failed with "
                f"{len(post_issues)} issue(s): {post_issues[:5]}"
            )

        db.commit()

        mapping_path = Path(args.mapping_output)
        _save_mapping(mapping_path, mapping)

        print("=== Migration Applied Successfully ===")
        print(f"users updated: {stats.users_updated}")
        print(f"packages.sender_id updated: {stats.package_sender_updated}")
        print(f"packages.receiver_id updated: {stats.package_receiver_updated}")
        print(f"packages.current_holder_id updated: {stats.package_holder_updated}")
        print(f"packages.route_checkpoints updated: {stats.package_checkpoints_updated}")
        print(f"scan_history.scanner_id updated: {stats.scans_updated}")
        print(f"alerts.recipient_id updated: {stats.alerts_recipient_updated}")
        print(f"alerts.scanned_by_id updated: {stats.alerts_scanned_by_updated}")
        print(f"Mapping file written to: {mapping_path}")
        print("Note: Existing JWTs contain old uid values. Re-login users after migration.")
        return 0

    except Exception as exc:
        db.rollback()
        print(f"Migration failed. Rolled back transaction. Error: {exc}")
        return 1
    finally:
        # Ensure SQLite FK checks are turned back on for future sessions.
        try:
            if db.bind and db.bind.dialect.name == "sqlite":
                db.execute(text("PRAGMA foreign_keys = ON"))
        except Exception:
            pass
        db.close()


if __name__ == "__main__":
    raise SystemExit(main())
