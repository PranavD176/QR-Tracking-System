# QR-Based Tracking System

## 📌 Overview
A QR-Based Package/File Tracking System designed to track file movement in real-time and detect misplacement using intelligent validation.

## 🚀 Features
- QR Code Scanning (Android)
- Real-time Tracking
- Misplacement Detection
- Alert System
- Secure API Integration

## 🛠 Tech Stack
- Android (Kotlin)
- FastAPI
- PostgreSQL
- Firebase

## 🏗 Architecture
![Architecture](diagrams/architecture.png)

## 🔄 Workflow
Scan → Validate → Update → Alert

## ⚙️ Setup Instructions

### Backend
```bash
pip install -r requirements.txt
uvicorn main:app --reload
