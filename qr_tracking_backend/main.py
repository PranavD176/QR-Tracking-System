from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import auth, admin, alerts, packages, scan
from app.models import checkpoint 

app = FastAPI(title="QR Tracking System API", version="1.0.0")

# CORS Configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:3001"],
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
