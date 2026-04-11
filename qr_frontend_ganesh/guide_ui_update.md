# Frontend To Backend Integration Guide

This document is the handoff for the backend integration work. It explains what the frontend already expects, which screens depend on which APIs, and what member 2 needs to connect the ViewModels, repositories, and network layer.

## 1. Current Frontend Stack

- UI: Jetpack Compose
- Navigation: Navigation Compose
- State: ViewModel + StateFlow
- Networking: Retrofit + OkHttp
- Auth storage: EncryptedSharedPreferences via TokenManager
- Push notifications: Firebase Messaging
- Local storage: Room is already included for future offline/cache support

## 2. Current Network Bootstrap

- Base URL: http://10.0.2.2:8000/api/
- All protected requests use JWT from TokenManager.
- Auth header format: Authorization: Bearer <token>
- Retrofit interceptor already attaches the token automatically.
- If the backend returns 401, the app clears local auth data and the user must log in again.
- The Firebase FCM token is stored on device first, then sent to the backend after login.

## 3. What Member 2 Needs To Build

Member 2 should connect the following layers:

- API service definitions to backend endpoints
- Repository layer between Retrofit and ViewModels
- ViewModel state handling with loading, success, and error states
- Request/response mapping for each screen
- Token lifecycle handling after login, logout, and 401 responses
- Role-based behavior for user and admin screens

The frontend already has the screen structure. The missing piece is the live data wiring.

## 4. Existing Frontend Contract

The app already expects this response envelope from backend APIs:

```json
{
	"success": true,
	"data": {},
	"error": null
}
```

If the request fails, the backend should return:

```json
{
	"success": false,
	"data": null,
	"error": "Human readable message"
}
```

### Shared models already used by the frontend

- AuthResponse
- ApiResponse<T>
- RegisterRequest
- LoginRequest
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

## 5. API Endpoints The Frontend Already Calls

### Authentication

#### POST /auth/register

Request:

```json
{
	"full_name": "John Doe",
	"email": "john@example.com",
	"password": "12345678",
	"role": "user"
}
```

Response:

```json
{
	"success": true,
	"data": {
		"token": "jwt-token",
		"token_type": "Bearer",
		"expires_in": 3600,
		"user_id": "uuid",
		"role": "user"
	},
	"error": null
}
```

#### POST /auth/login

Request:

```json
{
	"email": "john@example.com",
	"password": "12345678"
}
```

Response format is the same as register.

#### POST /auth/device-token

Purpose:

- Send the device FCM token after login so the backend can target this phone for notifications.

Request:

```json
{
	"fcm_token": "device-fcm-token"
}
```

Response:

```json
{
	"success": true,
	"data": {
		"updated": true
	},
	"error": null
}
```

### Packages

#### GET /packages

Used by the home package list and admin package list.

Query params:

- status: optional
- limit: default 50
- offset: default 0

Response item shape:

```json
{
	"package_id": "uuid",
	"description": "Package description",
	"status": "active",
	"owner_id": "uuid",
	"qr_payload": "optional-qr-payload",
	"created_at": "2026-04-11T10:15:00Z"
}
```

#### POST /packages

Used by Create Package screens.

Request:

```json
{
	"description": "Sample package description"
}
```

Response item shape:

```json
{
	"package_id": "uuid",
	"description": "Sample package description",
	"status": "created",
	"owner_id": "uuid",
	"qr_payload": "QR_TRACKING:uuid-or-backend-payload",
	"created_at": "2026-04-11T10:15:00Z"
}
```

Important:

- The frontend uses qr_payload to generate or show the QR image later.
- If qr_payload is missing, the create flow cannot complete the QR handoff cleanly.

#### GET /packages/{package_id}/scans

Used by package detail scan history.

Response item shape:

```json
{
	"scan_id": "uuid",
	"scanner_name": "John Smith",
	"result": "Valid",
	"location_description": "Warehouse A",
	"scanned_at": "2026-04-11T10:15:00Z"
}
```

### Scan

#### POST /scan

Used by the scanner flow after the QR code is read.

Request:

```json
{
	"package_id": "uuid",
	"location_description": "Section B"
}
```

Response item shape:

```json
{
	"result": "Valid",
	"package_description": "Package description",
	"owner_name": "Owner name",
	"alert_sent": false,
	"scanned_by": "User name"
}
```

The UI expects:

- Valid to show the green result screen
- Misplaced to show the red result screen and alert state

### Alerts

#### GET /alerts

User-facing alert feed.

Query params:

- status: optional, defaults to sent in the current ViewModel
- limit: default 20

Response item shape:

```json
{
	"alert_id": "uuid",
	"package_id": "uuid",
	"package_description": "Package description",
	"scanned_by_name": "Scanner name",
	"location": "Warehouse A",
	"status": "sent",
	"created_at": "2026-04-11T10:15:00Z"
}
```

#### PUT /alerts/{alert_id}/acknowledge

Used when the user dismisses an alert.

Response item shape:

```json
{
	"alert_id": "uuid",
	"status": "acknowledged"
}
```

#### GET /admin/alerts

Admin-only alert feed.

Response item shape:

```json
{
	"alert_id": "uuid",
	"package_description": "Package description",
	"owner_name": "Owner name",
	"scanned_by_name": "Scanner name",
	"location": "Warehouse A",
	"created_at": "2026-04-11T10:15:00Z"
}
```

## 6. ViewModel Responsibilities

### AuthViewModel

Current duties:

- login(email, password)
- register(email, password, fullName, role)
- logout()
- persist token, role, and user id
- send FCM token after login

Member 2 should make sure the ViewModel exposes enough state for the UI:

- idle
- loading
- success with user id and role
- error message

### PackageViewModel

Current duties:

- fetchPackages(status)
- fetchScanHistory(packageId)
- createPackage(description)

Expected UI bindings:

- Home screen package list
- Admin package list
- Package detail scan history
- Create package form

### ScanViewModel

Current duties:

- submitScan(packageId, locationDescription)

Expected UI bindings:

- scanner screen
- scan result screen

### AlertViewModel

Current duties:

- fetchAlerts(status)
- acknowledgeAlert(alertId)
- fetchAdminAlerts(status)

Expected UI bindings:

- user alert feed
- admin alert feed
- app alerts

## 7. Screen To ViewModel Mapping

| Screen | ViewModel | Required Backend Data |
| --- | --- | --- |
| LoginScreen | AuthViewModel | token, role, user_id |
| RegisterScreen | AuthViewModel | token, role, user_id |
| PackageListScreen | PackageViewModel | package list |
| PackageDetailScreen | PackageViewModel | scan history |
| CreatePackageScreen | PackageViewModel | qr_payload after create |
| ScanScreen | ScanViewModel | validation result |
| ScanResultScreen | ScanViewModel | result, package description, owner, alert flag |
| AlertFeedScreen | AlertViewModel | user alerts |
| AdminAlertScreen | AlertViewModel | admin alerts |
| AppAlertsScreen | AlertViewModel or future notification API | notification feed |

## 8. Backend Behavior Requirements

The backend should support the following behavior for the frontend to work smoothly:

1. JWT must be returned on login and register.
2. JWT must be accepted as Bearer token for protected routes.
3. 401 should mean token expired or invalid.
4. Package create must return qr_payload.
5. Scan must return both result and context fields used on the result screen.
6. Alerts must be filterable by status.
7. Admin alerts must be role-protected.
8. Device token registration must succeed after login.

## 9. Data Flow For Member 2

Recommended implementation order:

1. Create repository classes for auth, packages, scans, and alerts.
2. Move Retrofit calls out of ViewModels into repositories.
3. Keep ViewModels as state managers only.
4. Map backend DTOs to Compose state models where needed.
5. Handle 401 centrally with a shared auth/session policy.
6. Use TokenManager for token, role, and user id persistence.

## 10. Integration Checklist

Before marking backend integration complete, verify:

- Login returns token, role, and user_id
- Register returns the same auth payload
- FCM token is saved locally and sent to backend
- Package list loads successfully on home and admin screens
- Create package returns qr_payload
- Scan returns valid or misplaced with complete payload
- User alerts load and can be acknowledged
- Admin alerts load on admin screen
- Unauthorized responses redirect to login cleanly

## 11. Notes For Member 2

- The frontend currently uses mock-safe UI, but the network layer is already in place.
- The Retrofit base URL is currently pointed at the Android emulator host. Update it for a real device or production backend.
- Package names were migrated in the source tree, but the running app identity must still match the active Gradle and Firebase configuration.
- If the backend adds new fields, keep the existing JSON field names stable or update the Kotlin DTOs with matching names.

## 12. Summary

Member 2 only needs to connect the existing screens to the backend contract above. The app already has the navigation, token storage, Retrofit client, ViewModel state containers, and DTOs needed for a full frontend/backend integration.

## 13. Formal Backend Spec

Use this section as the implementation reference for API, repository, and ViewModel wiring.

### 13.1 Endpoint Matrix

| Module | Method | Endpoint | Auth | Purpose | Frontend Consumer |
| --- | --- | --- | --- | --- | --- |
| Auth | POST | /auth/register | No | Create account and return JWT | RegisterScreen, AuthViewModel |
| Auth | POST | /auth/login | No | Login and return JWT | LoginScreen, AuthViewModel |
| Auth | POST | /auth/device-token | Yes | Register device FCM token | AuthViewModel |
| Packages | GET | /packages | Yes | Fetch user/admin package list | PackageListScreen, AdminPackagesScreen, PackageViewModel |
| Packages | POST | /packages | Yes | Create package and return QR payload | CreatePackageScreen, PackageViewModel |
| Packages | GET | /packages/{package_id}/scans | Yes | Fetch scan history for one package | PackageDetailScreen, PackageViewModel |
| Scan | POST | /scan | Yes | Validate scanned QR and return result | ScanScreen, ScanViewModel |
| Alerts | GET | /alerts | Yes | Fetch user alerts | AlertFeedScreen, AlertViewModel |
| Alerts | PUT | /alerts/{alert_id}/acknowledge | Yes | Mark alert as handled | AlertFeedScreen, AlertViewModel |
| Admin | GET | /admin/alerts | Yes | Fetch admin-wide alert feed | AdminAlertScreen, AlertViewModel |

### 13.2 Request Contracts

#### RegisterRequest

```json
{
	"full_name": "John Doe",
	"email": "john@example.com",
	"password": "12345678",
	"role": "user"
}
```

#### LoginRequest

```json
{
	"email": "john@example.com",
	"password": "12345678"
}
```

#### DeviceTokenRequest

```json
{
	"fcm_token": "device-fcm-token"
}
```

#### CreatePackageRequest

```json
{
	"description": "Sample package description"
}
```

#### ScanRequest

```json
{
	"package_id": "uuid",
	"location_description": "Section B"
}
```

### 13.3 Response Contracts

#### AuthResponse

```json
{
	"success": true,
	"data": {
		"token": "jwt-token",
		"token_type": "Bearer",
		"expires_in": 3600,
		"user_id": "uuid",
		"role": "user"
	},
	"error": null
}
```

#### ApiResponse<List<PackageResponse>>

```json
{
	"success": true,
	"data": [
		{
			"package_id": "uuid",
			"description": "Package description",
			"status": "active",
			"owner_id": "uuid",
			"qr_payload": "QR_TRACKING:uuid",
			"created_at": "2026-04-11T10:15:00Z"
		}
	],
	"error": null
}
```

#### ApiResponse<PackageResponse>

```json
{
	"success": true,
	"data": {
		"package_id": "uuid",
		"description": "Package description",
		"status": "created",
		"owner_id": "uuid",
		"qr_payload": "QR_TRACKING:uuid",
		"created_at": "2026-04-11T10:15:00Z"
	},
	"error": null
}
```

#### ApiResponse<ScanResponse>

```json
{
	"success": true,
	"data": {
		"result": "Valid",
		"package_description": "Package description",
		"owner_name": "Owner name",
		"alert_sent": false,
		"scanned_by": "User name"
	},
	"error": null
}
```

#### ApiResponse<List<AlertResponse>>

```json
{
	"success": true,
	"data": [
		{
			"alert_id": "uuid",
			"package_id": "uuid",
			"package_description": "Package description",
			"scanned_by_name": "Scanner name",
			"location": "Warehouse A",
			"status": "sent",
			"created_at": "2026-04-11T10:15:00Z"
		}
	],
	"error": null
}
```

### 13.4 Response Code Rules

Backend responses should follow these rules so the ViewModels can stay simple:

| Code | Meaning | Frontend Behavior |
| --- | --- | --- |
| 200 | Success | Update state to Success |
| 201 | Created | Treat as Success for create flows |
| 400 | Validation error | Show backend error message |
| 401 | Unauthorized | Clear session and redirect to login |
| 403 | Forbidden | Show access denied message |
| 404 | Not found | Show not found message |
| 500 | Server error | Show generic backend failure message |

### 13.5 Suggested Repository Structure For Member 2

Create repositories so ViewModels no longer call Retrofit directly:

- AuthRepository
- PackageRepository
- ScanRepository
- AlertRepository

Recommended responsibilities:

- Repository should own Retrofit calls and response parsing.
- ViewModel should own screen state only.
- TokenManager should stay the source of truth for auth state on device.

### 13.6 Suggested ViewModel State Contract

Use the same pattern across all ViewModels:

```kotlin
sealed class UiState<out T> {
		object Idle : UiState<Nothing>()
		object Loading : UiState<Nothing>()
		data class Success<T>(val data: T) : UiState<T>()
		data class Error(val message: String) : UiState<Nothing>()
}
```

If member 2 prefers the existing per-feature sealed classes, that is also fine, but the structure should remain consistent.

### 13.7 Sequence Flows

#### Login Flow

1. User enters email and password.
2. LoginScreen calls AuthViewModel.login().
3. AuthViewModel calls AuthRepository.login().
4. Backend returns token, role, and user_id.
5. TokenManager saves token, role, and user_id.
6. App retrieves FCM token and sends it to /auth/device-token.
7. Navigation moves to the correct post-login screen.

#### Create Package Flow

1. User opens CreatePackageScreen.
2. User enters description.
3. PackageViewModel calls PackageRepository.createPackage().
4. Backend returns qr_payload.
5. UI either displays or forwards qr_payload to the QR generator flow.

#### Scan Flow

1. Scanner reads QR payload.
2. App extracts package_id from the QR value.
3. ScanViewModel calls ScanRepository.submitScan().
4. Backend returns result and scan metadata.
5. UI navigates to valid or misplaced result screen.

#### Alerts Flow

1. Screen opens alert feed.
2. AlertViewModel loads alerts from backend.
3. User acknowledges one alert.
4. Backend marks the alert acknowledged.
5. UI refreshes list.

### 13.8 Backend Fields The UI Depends On

Do not remove or rename these fields without updating the frontend DTOs:

- token
- token_type
- expires_in
- user_id
- role
- qr_payload
- package_id
- package_description
- scanned_by
- scanned_by_name
- owner_name
- alert_sent
- created_at
- status

### 13.9 Minimum Backend Deliverables

Member 2 should confirm the backend provides the following before final integration:

1. Auth endpoints with JWT.
2. Package creation with QR payload return.
3. Scan validation endpoint with full scan result data.
4. Alert list and acknowledge endpoints.
5. Admin alert endpoint with role protection.
6. Device token registration endpoint for FCM.

### 13.10 Handoff Summary For Member 2

The frontend is already prepared for live backend integration. The required next step is to place repository classes between Retrofit and ViewModels, keep token/session handling centralized, and ensure all backend responses match the DTOs listed in this document.
