# QR-Based Package Tracking System

## 📌 Overview
A QR-Based Package/File Tracking System designed to track file movement in real-time and detect misplacement using intelligent validation.

## 🚀 Features
- **QR Code Scanning:** Fast and reliable scanning in Android.
- **Real-time Tracking:** Real-time visibility into package status.
- **Misplacement Detection:** Alerts unauthorized package movements.
- **Alert System:** Push notifications via FCM alerts.
- **Secure API Integration:** JWT authentication and role-based access.

## 🛠 Tech Stack
- **Frontend:** Android (Kotlin, Jetpack Compose, Retrofit, StateFlow)
- **Backend:** Python (FastAPI, SQLAlchemy, Supabase PostgreSQL, Firebase Admin SDK for FCM)

## 🏗 Architecture Workflow
Scan → Validate → Update Database → Push FCM Alert

---

## ⚙️ Setup & Local Deployment

### 1. Backend Setup
The backend runs on FastAPI and requires PostgreSQL and Firebase configurations.
1. Navigate to the backend directory: `cd qr_tracking_backend`
2. Install dependencies: `pip install -r requirements.txt`
3. Configure environment variables in `.env`:
   ```ini
   DATABASE_URL="postgresql://user:pass@host/db"
   SUPABASE_JWT_SECRET="your_supabase_jwt_secret"
   GOOGLE_APPLICATION_CREDENTIALS="your_service_account.json"
   ```
4. Run the local backend server: `uvicorn main:app --reload`
5. *(Optional)* To run backend tests: `python setup_test_db.py` followed by `python test_backend.py`.

### 2. Frontend Build & Mobile Device Testing (USB Cable)
To run the Android app properly on your mobile device during development:
1. Enable **Developer Options** and **USB Debugging** on your Android device.
2. Connect your device to the PC via USB cable.
3. Open a terminal and run `adb devices` to verify your phone is detected.
4. Because the app communicates with the backend, you need to map the backend server to your phone. Use ADB Reverse port forwarding:
   ```bash
   adb reverse tcp:8000 tcp:8000
   ```
5. Navigate to the frontend directory: `cd qr_frontend_ganesh`
6. Build and install the app to your device:
   ```bash
   .\gradlew.bat installDebug
   ```

---

## 🚉 Deploying Backend to Railway

Railway is ideal for straightforward and quick deployments of FastAPI apps. Follow these steps:

### 1. Prepare your Repository
- Ensure you have a `requirements.txt` generated using `pip freeze > requirements.txt` in the backend directory.
- Add a `Procfile` to the `qr_tracking_backend` folder to tell Railway the start command:
  ```
  web: uvicorn main:app --host 0.0.0.0 --port $PORT
  ```

### 2. Railway Configuration
1. Login to [Railway.app](https://railway.app/).
2. Click **New Project** → **Deploy from GitHub repo**.
3. Select this repository. Set the **Root Directory** in Railway deployment settings to `/qr_tracking_backend`.
4. In Railway **Environment Variables**, add:
   - `DATABASE_URL` = `<your_supabase_postgres_url>`
   - `SUPABASE_JWT_SECRET` = `<your_supabase_jwt_secret>`
   - **Important**: Railway doesn't support file uploads easily via UI. You must either base64-encode your `firebase.json` service account and decode it on script startup, OR paste its JSON contents to a variable `FIREBASE_CREDENTIALS` and adjust `app/firebase.py` to parse it from the string memory instead of a file.

5. Trigger a deploy. Check logs to ensure FastAPI has started on port 0.0.0.0.

---

## 🧪 Testing the Complete App

1. Backend should be running locally or on Railway.
2. If running locally, make sure `adb reverse` is running so your Android phone hits localhost via USB. If using Railway, change the `Base URL` in your Retrofit client (in frontend) to the generated Railway URL.
3. Build the app to your phone (`installDebug`).
4. **Register** a new test account in the app.
5. Create a new package online/DB.
6. Print or open its QR Payload on your PC screen.
7. Use the **Scan Package** feature on your phone to scan the QR payload.
8. Wait for the app to notify you if it is marked as `Valid` or `Misplaced`. Verify that the device properly prompts for FCM token generation when applicable.
