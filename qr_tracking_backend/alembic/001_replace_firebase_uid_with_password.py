"""
Alembic migration: Replace firebase_uid with hashed_password
Generated for QR Tracking System — Firebase → Supabase migration

Changes:
  - Drop column: users.firebase_uid
  - Add column:  users.hashed_password (String, NOT NULL with a temp default)

NOTE: Because existing rows (if any) will have no password, we set a temporary
      placeholder hash and users will need to re-register or have their passwords
      reset. In a fresh dev environment with no real users this is a non-issue.
"""
from alembic import op
import sqlalchemy as sa


# Alembic revision identifiers
revision = "001_replace_firebase_uid_with_password"
down_revision = None   # first migration — adjust if you have existing ones
branch_labels = None
depends_on = None

# Temporary hash used for existing rows during migration
# (bcrypt hash of the string "RESET_REQUIRED")
_TEMP_HASH = "$2b$12$placeholderHashForExistingRowsOnly.XXXXXXXXXXXXXXXXXXXXXX"


def upgrade():
    with op.batch_alter_table("users") as batch_op:
        # Remove the Firebase UID column
        batch_op.drop_column("firebase_uid")

        # Add hashed_password — use server_default so existing rows get a value
        batch_op.add_column(
            sa.Column(
                "hashed_password",
                sa.String(),
                nullable=False,
                server_default=_TEMP_HASH,
            )
        )

    # Remove the server_default after backfill so future insertions
    # must always supply a real hashed password
    with op.batch_alter_table("users") as batch_op:
        batch_op.alter_column("hashed_password", server_default=None)


def downgrade():
    with op.batch_alter_table("users") as batch_op:
        batch_op.drop_column("hashed_password")
        batch_op.add_column(
            sa.Column(
                "firebase_uid",
                sa.String(),
                nullable=False,
                server_default="MIGRATED",
            )
        )
