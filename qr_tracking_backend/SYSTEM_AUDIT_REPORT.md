# SYSTEM AUDIT REPORT
**Project:** QR-Based Tracking System  
**Backend:** FastAPI + SQLAlchemy  
**Audit Date:** 2026-04-09  
**Auditor:** Senior Backend / QA Automation Review  
**FCM Status:** Intentionally not configured — all FCM-related issues are excluded.

---

## Section 1: Critical Issues (Must Fix Immediately)

---

### 🔴 CRITICAL-1 — `setup_test_db.py` Crashes on Re-run (Duplicate Inserts)

**Bug:** `setup_test_db.py` has no guard against duplicate data. Running it twice against the same database raises `UNIQUE constraint failed: users.email` and crashes with an unhandled exception.

**Impact:** Dev workflow is broken. Any CI/CD pipeline that re-runs seeding will fail. The `db.rollback()` in the `except` block is called *after* the crash unwinds, but the process exits with code 1 anyway because the exception is re-raised with `raise`.

**Fix:** Use `INSERT OR IGNORE` (SQLite) or `INSERT ... ON CONFLICT DO NOTHING` (PostgreSQL) semantics — the cleanest ORM way is to check before inserting:

```python
# In setup_test_db.py — wrap each insert with an existence check
def upsert_user(db, **kwargs):
    user = db.query(User).filter_by(email=kwargs["email"]).first()
    if not user:
        user = User(**kwargs)
        db.add(user)
    return user
```

Or, simply wipe test data before re-seeding in dev:
```python
# At the top of setup_test_database():
db.query(Alert).delete()
db.query(ScanHistory).delete()
db.query(Package).delete()
db.query(User).delete()
db.commit()
```

---

### 🔴 CRITICAL-2 — Alert Query Uses Wrong Field (`uid` vs `user_id`)

**Bug:** In `app/routers/alerts.py` line 11:
```python
alerts = db.query(Alert).filter_by(recipient_id=user["uid"], status="sent").all()
```
The JWT payload sets `uid = user.user_id` (the UUID primary key), and `Alert.recipient_id` is a FK to `users.user_id` — so this is correct **now**. However, the query returns **all** alerts to the user regardless of pagination or limits. If a user has 10,000 misplacement alerts, this returns them all in a single query, blocking the event loop and crashing memory.

**Fix:** Add pagination:
```python
@router.get("/alerts")
def get_alerts(
    skip: int = 0,
    limit: int = 20,
    user=Depends(get_current_user),
    db: Session = Depends(get_db)
):
    alerts = (
        db.query(Alert)
        .filter_by(recipient_id=user["uid"], status="sent")
        .offset(skip)
        .limit(limit)
        .all()
    )
```

---

### 🔴 CRITICAL-3 — `scan.py` Has a Non-Atomic Transaction (Race Condition)

**Bug:** In `app/routers/scan.py`, the scan result logic is:
1. Query package
2. Check ownership
3. Insert `ScanHistory`
4. Insert `Alert`
5. Query owner's FCM token
6. Send push notification
7. `db.commit()`

If step 6 (FCM call) throws an exception, the code exits before `db.commit()`. This is actually safe. **But** if a second request comes in between steps 3 and 7, before the commit, both requests will successfully insert two `ScanHistory` rows for the same scan event because there is no uniqueness constraint preventing it.

**Impact:** Duplicate scan records, duplicate alerts, duplicate push notifications sent to the package owner.

**Fix:** Add a unique constraint on `(package_id, scanner_id)` to prevent duplicate scans within a time window, OR wrap the critical section in a proper DB-level lock. Minimum fix:
```python
# In models/scan.py
from sqlalchemy import Column, String, ForeignKey, DateTime, UniqueConstraint
from datetime import datetime

class ScanHistory(Base):
    __tablename__ = "scan_history"
    scan_id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    package_id = Column(String, ForeignKey("packages.package_id"), nullable=False)
    scanner_id = Column(String, ForeignKey("users.user_id"), nullable=False)
    result = Column(String, nullable=False)
    location_description = Column(String)
    scanned_at = Column(DateTime, default=datetime.utcnow, nullable=False)
```

---

### 🔴 CRITICAL-4 — `JWT_SECRET_KEY` Default Is Hardcoded and Insecure

**Bug:** In `app/firebase.py`:
```python
SECRET_KEY = os.getenv("JWT_SECRET_KEY", "change-me-in-production-please")
```
If `.env` is missing or the var is not set, the app silently falls back to the hardcoded weak string. An attacker knowing this default could forge valid JWTs for any user.

**Impact:** Complete authentication bypass for any installation where `.env` is not properly configured.

**Fix:** Fail loudly at startup if the secret is not set:
```python
SECRET_KEY = os.getenv("JWT_SECRET_KEY")
if not SECRET_KEY:
    raise RuntimeError("FATAL: JWT_SECRET_KEY environment variable is not set!")
```

---

### 🔴 CRITICAL-5 — CORS Allows Only `localhost` — Android App Will Be Blocked

**Bug:** In `main.py`:
```python
allow_origins=["http://localhost:3000", "http://localhost:3001"],
```
The Android app communicates over HTTP/HTTPS to a deployed server URL (e.g., `https://your-api.railway.app`). The current CORS config will **block all Android requests** once deployed because the app's origin won't match either localhost URL.

**Fix:**
```python
import os
ALLOWED_ORIGINS = os.getenv("ALLOWED_ORIGINS", "http://localhost:3000").split(",")

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```
Then in `.env`:
```ini
ALLOWED_ORIGINS=http://localhost:3000,https://your-production-url.com
```
> **Note:** Android apps using Retrofit make direct HTTP calls and don't use CORS. CORS only applies to browser-based clients. However this is still best practice and required if you ever add a web frontend.

---

## Section 2: API Issues

---

| # | Endpoint | Method | Problem | Expected | Actual | Fix |
|---|---|---|---|---|---|---|
| 1 | `/api/auth/register` | POST | No password strength validation. Accepts `""` or `"a"` as valid passwords. | 422 Validation Error | 200 success with 1-char password | Add `min_length=8` to schema |
| 2 | `/api/auth/register` | POST | No email format validation. Accepts `"notanemail"` | 422 Validation Error | 200 success | Use `pydantic.EmailStr` |
| 3 | `/api/auth/login` | POST | Returns `200 OK` even on failure (`success: false`). Clients must check body, not status code. | `401 Unauthorized` | `200 OK` with `success: false` | Return proper HTTP status codes |
| 4 | `/api/auth/device-token` | POST | No validation that `fcm_token` is non-empty string. | 422 if empty | 200 success with empty token stored | Add `min_length=1` to schema |
| 5 | `/api/packages` | GET | No pagination. Returns ALL packages for a user. | Paginated response | Unbounded list | Add `skip`/`limit` query params |
| 6 | `/api/packages/{id}/scans` | GET | No authorization check — any authenticated user can fetch scans for any package by guessing the UUID. | 403 if not owner | 200 with scan data | Add ownership check |
| 7 | `/api/scan` | POST | Scanned packages return full result to the scanner, including whether the package is "misplaced". This leaks internal ownership info. | Only return "scanned successfully" | Returns `valid`/`misplaced` + `alert_sent: true` | Limit response data to scanner |
| 8 | `/api/alerts/{id}/acknowledge` | PUT | No ownership check. Any authenticated user can acknowledge any alert by guessing its UUID. | 403 if not recipient | 200 acknowledged | Add `recipient_id == user["uid"]` check |
| 9 | `/api/admin/alerts` | GET | Admin check works but there is no way to promote a user to admin via any endpoint. Admin role can only be set directly in the DB. | Admin management API | No such endpoint exists | Add `PUT /api/admin/users/{id}/role` |
| 10 | All routes | ALL | SQLAlchemy ORM exceptions (e.g., DB connection lost) bubble up as unhandled 500 errors with full Python tracebacks exposed in the response. | Generic `500` with no internals | Full traceback in response | Add global exception handler in `main.py` |

---

### Fix for Issue #1, #2 — Schema Validation

```python
# app/schemas/user.py
from pydantic import BaseModel, EmailStr, field_validator

class UserCreate(BaseModel):
    email: EmailStr  # validates email format
    password: str
    full_name: str

    @field_validator("password")
    @classmethod
    def password_strength(cls, v):
        if len(v) < 8:
            raise ValueError("Password must be at least 8 characters")
        return v

    @field_validator("full_name")
    @classmethod
    def full_name_not_empty(cls, v):
        if not v.strip():
            raise ValueError("Full name cannot be empty")
        return v.strip()
```

### Fix for Issue #3 — Proper HTTP Status Codes on Login

```python
# app/routers/auth.py
@router.post("/auth/login", status_code=200)
def login(credentials: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter_by(email=credentials.email).first()
    if not user or not pwd_context.verify(credentials.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Invalid email or password")
    ...
```

### Fix for Issue #6 — Package Scan Authorization

```python
# app/routers/packages.py
@router.get("/packages/{package_id}/scans")
def get_package_scans(package_id: str, user=Depends(get_current_user), db: Session = Depends(get_db)):
    # Verify the requester owns this package
    package = db.query(Package).filter_by(package_id=package_id, owner_id=user["uid"]).first()
    if not package:
        raise HTTPException(status_code=403, detail="Not authorized to view these scans")
    ...
```

### Fix for Issue #10 — Global Exception Handler

```python
# main.py
from fastapi import Request
from fastapi.responses import JSONResponse

@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=500,
        content={"success": False, "data": None, "error": "Internal server error"}
    )
```

---

## Section 3: Database Improvements

---

### DB-1 — Missing `timestamp` Columns on ALL Tables

**Problem:** None of the models have `created_at`/`updated_at` timestamps. You have no idea when a package was created, when a scan happened, or when an alert was sent.

**Fix:**
```python
# Add to ALL models
from sqlalchemy import DateTime
from datetime import datetime

created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
```

---

### DB-2 — `Package.status` Is a Plain `String` With No Constraint

**Problem:** `status = Column(String, default="active")` accepts any value. You could store `"banana"` as a status. No validation at the DB level.

**Fix:**
```python
# models/package.py
status = Column(
    Enum("active", "inactive", "lost", name="package_status"),
    default="active",
    nullable=False
)
```

---

### DB-3 — No DB-Level Foreign Key Enforcement on SQLite

**Problem:** SQLite does **not** enforce foreign key constraints by default. You can insert an `Alert` with a `recipient_id` that doesn't exist in the `users` table and SQLite won't complain.

**Fix:** Add to `database.py`:
```python
from sqlalchemy import event

if DATABASE_URL.startswith("sqlite"):
    @event.listens_for(engine, "connect")
    def set_sqlite_pragma(dbapi_connection, connection_record):
        cursor = dbapi_connection.cursor()
        cursor.execute("PRAGMA foreign_keys=ON")
        cursor.close()
```

---

### DB-4 — `ScanHistory` Missing `scanned_at` Timestamp

**Problem:** The scan history has no timestamp. You can't tell when a package was scanned, making audit trails useless.

**Fix:**
```python
# models/scan.py — add:
from sqlalchemy import DateTime
from datetime import datetime

scanned_at = Column(DateTime, default=datetime.utcnow, nullable=False)
```

---

### DB-5 — No Index on Frequently Queried Columns

**Problem:** Every API call does `filter_by(owner_id=...)` or `filter_by(recipient_id=...)` with no index. Full table scans on every request.

**Fix:**
```python
# models/package.py
owner_id = Column(String, ForeignKey("users.user_id"), index=True, nullable=False)

# models/alerts.py
recipient_id = Column(String, ForeignKey("users.user_id"), index=True, nullable=False)

# models/scan.py
package_id = Column(String, ForeignKey("packages.package_id"), index=True, nullable=False)
```

---

### DB-6 — `Alert` Missing `created_at` Timestamp

**Problem:** You can't tell when an alert was created. The admin dashboard would have no chronological ordering.

---

### Production-Grade Schema (Summary)

```sql
-- Minimum required indexes for production performance
CREATE INDEX idx_packages_owner_id ON packages(owner_id);
CREATE INDEX idx_alerts_recipient_id ON alerts(recipient_id);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_scan_history_package_id ON scan_history(package_id);
CREATE INDEX idx_scan_history_scanner_id ON scan_history(scanner_id);
```

---

## Section 4: Security Risks

---

| # | Risk | Severity | Fix |
|---|---|---|---|
| 1 | **Hardcoded JWT fallback secret** | 🔴 CRITICAL | Raise `RuntimeError` at startup if `JWT_SECRET_KEY` is not set |
| 2 | **No rate limiting on `/auth/login`** | 🔴 HIGH | Add `slowapi` rate limiting (5 requests/minute per IP) |
| 3 | **No rate limiting on `/auth/register`** | 🔴 HIGH | Same as above — bot registration attack vector |
| 4 | **Password returned in 422 errors** | 🟠 MEDIUM | Pydantic validation errors may echo back the submitted body. Use `response_model` to control output |
| 5 | **Admin role only settable via direct DB access** | 🟠 MEDIUM | Acceptable for MVP but add an admin-only `PUT /users/{id}/role` endpoint |
| 6 | **JWT has no `jti` (token ID) — no logout/revocation** | 🟠 MEDIUM | JWTs cannot be invalidated before expiry. Add a token blacklist in Redis or in DB |
| 7 | **Alert acknowledgement has no ownership check** | 🟠 MEDIUM | Any user can acknowledge any other user's alert — see API Issue #8 |
| 8 | **Package scan history visible to non-owners** | 🟠 MEDIUM | Any user can enumerate scans for any package — see API Issue #6 |
| 9 | **CORS locked to `localhost` only** | 🟡 LOW | Won't block Android but will block any future web frontend |
| 10 | **`firebase.json` in working directory** | 🟡 LOW | Service account keys should never be in the project root. Add to `.gitignore` and load via env var path |

---

### Fix for Issue #2/#3 — Rate Limiting

```bash
pip install slowapi
```
```python
# main.py
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# In auth.py
from main import limiter

@router.post("/auth/login")
@limiter.limit("5/minute")
def login(request: Request, credentials: LoginRequest, ...):
    ...
```

---

### Fix for Issue #10 — Move `firebase.json` Path to Env Var

```python
# app/firebase.py
CREDENTIALS_FILE = os.getenv("GOOGLE_APPLICATION_CREDENTIALS", "firebase.json")
```
```ini
# .env
GOOGLE_APPLICATION_CREDENTIALS=/secure/path/to/firebase.json
```

---

## Section 5: Performance Bottlenecks

---

### PERF-1 — Synchronous Database in Async Framework

**Issue:** FastAPI is an async framework but the entire ORM stack is synchronous (`sqlalchemy` with `SessionLocal`, not `AsyncSession`). Every DB call blocks the event loop thread.

**Why it matters:** Under concurrent load (e.g., 50 simultaneous scans), all requests queue behind each other. Throughput is essentially single-threaded.

**Optimization:** For a student/MVP project this is acceptable. For production, migrate to `sqlalchemy.ext.asyncio`:
```python
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
DATABASE_URL = "postgresql+asyncpg://..."
engine = create_async_engine(DATABASE_URL)
```

---

### PERF-2 — N+1 Query Risk in `/api/admin/alerts`

**Issue:** `get_all_alerts` returns all `Alert` objects but the response doesn't include package description or user names. If you later add those to the response, you'll trigger an N+1 query (one query per alert to fetch related package/user).

**Optimization:** Use `joinedload` proactively:
```python
from sqlalchemy.orm import joinedload

alerts = (
    db.query(Alert)
    .options(joinedload(Alert.package), joinedload(Alert.recipient))
    .filter_by(status="sent")
    .all()
)
```
This requires adding `relationship()` to the models.

---

### PERF-3 — `send_push_notification()` Blocks the Request

**Issue:** In `scan.py`, the FCM HTTP call is made synchronously inside the route handler. If the FCM server is slow (>3s), the scan API call blocks for that entire time.

**Optimization:** Use `BackgroundTasks` to send the notification after the response:
```python
from fastapi import BackgroundTasks

@router.post("/scan")
def scan(data: ScanRequest, background_tasks: BackgroundTasks, ...):
    ...
    if owner and owner.fcm_token:
        background_tasks.add_task(
            send_push_notification,
            owner.fcm_token,
            "Package Alert 🚨",
            f"Your package was scanned at {data.location_description}"
        )
    db.commit()
    return {...}  # Returns immediately, notification sent in background
```

---

### PERF-4 — Service Account Credentials Refreshed on Every Notification

**Issue:** In `app/firebase.py`, `_get_fcm_credentials()` re-reads the `firebase.json` file from disk and creates a new `Credentials` object on **every single push notification**. If you send 100 notifications per minute, that's 100 file reads and 100 new OAuth token requests.

**Optimization:** Cache the credentials globally:
```python
_cached_credentials = None
_cached_project_id = None

def _get_fcm_credentials():
    global _cached_credentials, _cached_project_id
    if _cached_credentials is None:
        if not os.path.exists(CREDENTIALS_FILE):
            return None, None
        with open(CREDENTIALS_FILE) as f:
            _cached_project_id = json.load(f).get("project_id")
        _cached_credentials = service_account.Credentials.from_service_account_file(
            CREDENTIALS_FILE, scopes=FCM_SCOPES
        )
    return _cached_credentials, _cached_project_id
```
The `google-auth` library handles token refresh automatically when the token expires.

---

## Section 6: Step-by-Step Improvement Roadmap

---

### Step 1 — Fix Duplicate Seed Data Crash (Do This Today)

**What:** Add a "clear before seed" block to `setup_test_db.py`  
**Why:** Every dev environment reset will crash otherwise  
**How:**
```python
# Add at the start of setup_test_database(), after db = SessionLocal():
print("Clearing existing data...")
db.query(Alert).delete()
db.query(ScanHistory).delete()
db.query(Package).delete()
db.query(User).delete()
db.commit()
```

---

### Step 2 — Make JWT Secret Mandatory at Startup

**What:** Remove the default fallback secret in `app/firebase.py`  
**Why:** A forgotten `.env` file silently creates a massive security hole  
**How:**
```python
SECRET_KEY = os.getenv("JWT_SECRET_KEY")
if not SECRET_KEY:
    raise RuntimeError("FATAL: JWT_SECRET_KEY is not set in environment!")
```

---

### Step 3 — Add Timestamps to All Models

**What:** Add `created_at` to `User`, `Package`, `ScanHistory`, `Alert`  
**Why:** Audit trails, chronological ordering in UI, debugging  
**How:** Add to each model:
```python
from sqlalchemy import DateTime
from datetime import datetime
created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
```

---

### Step 4 — Fix API Response Codes (Login, Register)

**What:** Replace `return {"success": False, ...}` with proper `raise HTTPException`  
**Why:** REST clients (Android Retrofit) check HTTP status codes, not body fields. Android's `response.isSuccessful` returns `true` for any 2xx — your login failures return 200, so the Android app thinks login succeeded  
**How:** In `auth.py`:
```python
if not user or not pwd_context.verify(...):
    raise HTTPException(status_code=401, detail="Invalid email or password")
```

---

### Step 5 — Add Input Validation to Schemas

**What:** Use `EmailStr`, `min_length`, field validators in `UserCreate`  
**Why:** Prevents garbage data, empty passwords, malformed emails from being stored  
**How:** See Section 2, Fix for Issue #1/#2 above

---

### Step 6 — Add Authorization Checks to Package Scans & Alert Acknowledgement

**What:** Verify ownership in `GET /packages/{id}/scans` and `PUT /alerts/{id}/acknowledge`  
**Why:** Currently any authenticated user can read anyone's scan history  
**How:** See Section 2, Fix for Issue #6 above

---

### Step 7 — Apply DB Indexes

**What:** Add `index=True` to all FK columns used in `filter_by()` queries  
**Why:** Without indexes, every query is a full table scan — O(n) instead of O(log n)  
**How:** See Section 3, DB-5 above

---

### Step 8 — Move Push Notifications to Background Tasks

**What:** Use FastAPI `BackgroundTasks` for FCM calls in `scan.py`  
**Why:** Scan response time is currently coupled to FCM network latency  
**How:** See Section 5, PERF-3 above

---

### Step 9 — Add Rate Limiting to Auth Endpoints

**What:** Install `slowapi`, add `@limiter.limit("5/minute")` to `/auth/login` and `/auth/register`  
**Why:** Without it, a bot can brute-force passwords or spam registrations  
**How:** See Section 4, Fix for Issue #2/#3 above

---

### Step 10 — Migrate DATABASE_URL to Supabase PostgreSQL

**What:** Change `DATABASE_URL` in `.env` to your Supabase connection string  
**Why:** SQLite is single-writer — concurrent scan requests will cause database lock errors in production  
**How:**
```ini
DATABASE_URL=postgresql://postgres:yourpassword%40symbol@db.xxxx.supabase.co:5432/postgres
```
Then run:
```bash
..\venv\Scripts\python.exe setup_test_db.py
```

---

### Step 11 — Enable SQLite Foreign Key Enforcement (Dev Only)

**What:** Add `PRAGMA foreign_keys=ON` on engine connect  
**Why:** SQLite silently ignores FK violations during development, hiding bugs  
**How:** See Section 3, DB-3 above

---

### Step 12 — Add Global Exception Handler to `main.py`

**What:** Add a catch-all `@app.exception_handler(Exception)` that returns a clean JSON error  
**Why:** Python tracebacks exposed in API responses leak internal code structure to attackers  
**How:** See Section 2, Fix for Issue #10 above

---

## Summary Table

| Priority | Issue | Effort | Impact |
|---|---|---|---|
| 🔴 P0 | JWT secret has insecure default | 2 min | Auth bypass risk |
| 🔴 P0 | Login returns 200 on failure | 5 min | Android auth broken |
| 🔴 P0 | setup_test_db crashes on re-run | 5 min | Dev workflow broken |
| 🔴 P1 | No ownership check on scans/alerts | 15 min | Data exposure |
| 🔴 P1 | Scan not atomic (race condition) | 30 min | Duplicate data |
| 🟠 P2 | No input validation (email, password) | 20 min | Data quality |
| 🟠 P2 | No timestamps on models | 30 min | No audit trail |
| 🟠 P2 | Push notification blocks response | 15 min | API latency |
| 🟡 P3 | No rate limiting | 45 min | Brute force risk |
| 🟡 P3 | No DB indexes | 20 min | Query performance |
| 🟡 P3 | Credentials loaded every notification | 10 min | FCM performance |
| 🟢 P4 | Async SQLAlchemy migration | 4 hrs | Scalability |
