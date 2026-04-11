# Summary Guide For Frontend-Backend Integration

This is the short handoff version of [guide_ui_update.md](guide_ui_update.md). It is meant for member 2 to quickly understand what the frontend already expects and what still needs to be connected to the backend.

## 1. Purpose

The app already has the screens, navigation, UI state containers, and Retrofit client. The remaining work is to connect those screens to the backend through repositories and ViewModels.

## 2. Frontend Stack

- Jetpack Compose for UI
- Navigation Compose for routing
- ViewModel + StateFlow for screen state
- Retrofit + OkHttp for networking
- EncryptedSharedPreferences via TokenManager for JWT storage
- Firebase Messaging for push notifications
- Room is already included for future local data support

## 3. Network Setup

- Base URL: http://10.0.2.2:8000/api/
- Protected requests use JWT in `Authorization: Bearer <token>`
- Retrofit already adds the token automatically
- If the backend returns 401, the app clears local auth data and the user must log in again
- FCM token is stored on device first, then sent to the backend after login

## 4. What Member 2 Needs To Connect

Member 2 should connect:

- Retrofit API calls to backend endpoints
- Repository layer between Retrofit and ViewModels
- ViewModel loading/success/error states
- Request and response mapping for each screen
- Token handling for login, logout, and session expiry
- Role-based behavior for user and admin screens

## 5. Existing API Contract

All backend responses should follow this structure:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

Error response:

```json
{
  "success": false,
  "data": null,
  "error": "Human readable message"
}
```

## 6. Main DTOs Already Used By Frontend

- RegisterRequest
- LoginRequest
- AuthResponse
- DeviceTokenRequest
- CreatePackageRequest
- PackageResponse
- ScanRequest
- ScanResponse
- ScanHistoryResponse
- AlertResponse
- AdminAlertResponse
- AcknowledgeResponse
- UpdatedResponse

## 7. Required Backend Endpoints

### Auth

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/device-token`

### Packages

- `GET /packages`
- `POST /packages`
- `GET /packages/{package_id}/scans`

### Scan

- `POST /scan`

### Alerts

- `GET /alerts`
- `PUT /alerts/{alert_id}/acknowledge`
- `GET /admin/alerts`

## 8. Screen To ViewModel Mapping

| Screen | ViewModel | Backend Data Needed |
| --- | --- | --- |
| LoginScreen | AuthViewModel | token, role, user_id |
| RegisterScreen | AuthViewModel | token, role, user_id |
| PackageListScreen | PackageViewModel | package list |
| PackageDetailScreen | PackageViewModel | scan history |
| CreatePackageScreen | PackageViewModel | qr_payload |
| ScanScreen | ScanViewModel | scan result |
| ScanResultScreen | ScanViewModel | result, package description, owner, alert flag |
| AlertFeedScreen | AlertViewModel | user alerts |
| AdminAlertScreen | AlertViewModel | admin alerts |
| AppAlertsScreen | AlertViewModel or future notification API | notification feed |

## 9. ViewModel Responsibilities

### AuthViewModel

- Login and register users
- Save token, role, and user id
- Send FCM token after login
- Clear session on logout

### PackageViewModel

- Fetch package list
- Fetch scan history for one package
- Create new package

### ScanViewModel

- Submit scan package_id and location description
- Return valid or misplaced result state

### AlertViewModel

- Fetch user alerts
- Acknowledge alerts
- Fetch admin alerts

## 10. Important Backend Rules

- JWT must be returned on login and register
- JWT must be accepted on protected routes
- 401 means token is invalid or expired
- Create package must return qr_payload
- Scan response must include the fields needed by the result screen
- Alerts must support filtering by status
- Admin alerts must be role protected
- Device token registration must work after login

## 11. Recommended Member 2 Workflow

1. Add repository classes for auth, packages, scans, and alerts.
2. Move Retrofit calls out of ViewModels.
3. Keep ViewModels focused on UI state only.
4. Map backend responses into the existing DTOs.
5. Handle 401 centrally.
6. Use TokenManager for auth state on device.

## 12. Minimum Acceptance Checklist

- Login returns token, role, and user_id
- Register returns the same auth data
- FCM token is saved locally and sent to backend
- Package list loads on user and admin screens
- Create package returns qr_payload
- Scan returns valid or misplaced with full data
- User alerts load and can be acknowledged
- Admin alerts load correctly
- Unauthorized responses send the user back to login

## 13. Short Summary

The frontend is already prepared for backend integration. Member 2 mainly needs to add repository classes, wire the existing ViewModels to the backend contract, and keep token/session handling consistent across the app.
