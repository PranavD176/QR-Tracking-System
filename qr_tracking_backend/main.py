from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import auth, admin, alerts, packages, scan

# Import all models so they are registered with Base before create_all
from app.models.user import User
from app.models.package import Package
from app.models.scan import ScanHistory
from app.models.alerts import Alert
from app.database import Base, engine

# Create all tables on startup (safe — does nothing if they already exist)
Base.metadata.create_all(bind=engine)

app = FastAPI(title="QR Tracking System API", version="1.0.0")

# CORS Configuration — allow all origins for mobile API access
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(auth.router, prefix="/api")
app.include_router(admin.router, prefix="/api")
app.include_router(alerts.router, prefix="/api")
app.include_router(packages.router, prefix="/api")
app.include_router(scan.router, prefix="/api")

@app.get("/")
def root():
    return {"message": "QR Tracking System API"}

@app.get("/health")
def health_check():
    return {"status": "healthy"}
