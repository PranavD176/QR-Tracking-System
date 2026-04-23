import argparse
import os
import sys

# Allow running from qr_tracking_backend/tests without installation.
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.firebase import send_push_notification  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Send a manual popup push notification to a device token."
    )
    parser.add_argument("--token", required=True, help="Target FCM device token")
    parser.add_argument("--title", default="Popup Test", help="Notification title")
    parser.add_argument(
        "--body",
        default="Testing popup notification delivery from backend.",
        help="Notification body",
    )

    args = parser.parse_args()

    print("=== Popup Notification Test ===")
    print(f"Title: {args.title}")
    print(f"Body: {args.body}")
    print(f"Token Prefix: {args.token[:18]}...")
    print("Sending push...")

    ok = send_push_notification(args.token, args.title, args.body)

    if ok:
        print("SUCCESS: Push send call completed. Check device for popup behavior.")
        return 0

    print("FAILED: Push send did not succeed.")
    print("Actionable checks:")
    print("1) Verify FIREBASE_SERVICE_ACCOUNT_JSON or GOOGLE_APPLICATION_CREDENTIALS")
    print("2) Verify the token is from the current app build/device")
    print("3) Verify app notification permission/channel settings on device")
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
