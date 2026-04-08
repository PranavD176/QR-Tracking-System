---

## Screen Status

| Screen | Status | Data Source When Dynamic |
|--------|--------|--------------------------|
| LoginScreen | ❌ Mock | `POST /auth/login` |
| RegisterScreen | ❌ Mock | `POST /auth/register` |
| PackageListScreen | ❌ Mock | `GET /packages` |
| PackageDetailScreen | ❌ Mock | `GET /packages/{id}/scans` |
| CreatePackageScreen | ❌ Mock | `POST /packages` |
| ScanScreen | ❌ Mock | `POST /scan` |
| ScanResultScreen | ✅ Dynamic | Navigation arguments |
| AlertFeedScreen | ⬜ Not built | `GET /alerts` — Member 2 |
| AdminAlertsScreen | ⬜ Not built | `GET /admin/alerts` — Member 2 |

---

## Who Makes Pages Dynamic

| Task | Owner |
|------|-------|
| Build ViewModels + API call logic | Member 2 |
| Replace mock state with ViewModel in screens | Member 1 (You) |
| Build backend endpoints | Member 3 + 4 |
| Switch BASE_URL to real backend IP | Member 1 (You) |

### How You Connect a Screen (Integration Week)

**Step 1 — Add ViewModel to screen:**
```kotlin
val viewModel: AuthViewModel = viewModel()
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

**Step 2 — Replace mock navigation with event observer:**
```kotlin
LaunchedEffect(Unit) {
    viewModel.navigationEvent.collect { event ->
        navController.navigate(Routes.PACKAGE_LIST) {
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    }
}
```

**Step 3 — Replace TODO mock call with ViewModel call:**
```kotlin
// REMOVE
navController.navigate(Routes.PACKAGE_LIST)

// ADD
viewModel.login(uiState.email, uiState.password)
```

**Step 4 — Switch BASE_URL in RetrofitClient.kt:**
```kotlin
// Change this when backend shares their IP
private const val BASE_URL = "http://<backend-ip>:8000/"
```

---

## Important Rules — Do Not Forget

### Security
- `google-services.json` — never commit, always share privately
- JWT stored in `EncryptedSharedPreferences` only
- `HttpLoggingInterceptor` logs all requests — acceptable for dev
- Camera permission — all 4 states handled in ScanScreen

### Git Rules
- Branch: always push to `dev`, never directly to `main`
- `google-services.json` is in `.gitignore` ✅
- `local.properties` is in `.gitignore` ✅
- Every team member who clones the repo needs `google-services.json`
  shared privately before building

### TODO Markers
Search `// TODO` across the project to find every integration point.
There are 8 TODOs — one per screen mock call.
Each TODO = one line Member 2 replaces with a ViewModel call.

---

## Member 2 Checklist (Madhur)

- [ ] Clone repo, place `google-services.json` in `app/`, build succeeds
- [ ] AuthViewModel — login, register, logout
- [ ] ScanViewModel — POST /scan connected
- [ ] PackageViewModel — package list, detail, create
- [ ] AlertViewModel — alerts + acknowledge
- [ ] AlertFeedScreen built
- [ ] AdminAlertsScreen built
- [ ] FCM token sent to backend after login
- [ ] All TODO markers connected to ViewModels

## Member 3 Checklist

- [ ] FastAPI project running locally
- [ ] PostgreSQL + Alembic migrations done
- [ ] Firebase Admin SDK verifying tokens
- [ ] POST /auth/register, POST /auth/login working
- [ ] POST /scan core logic working
- [ ] GET /packages, POST /packages working
- [ ] CORS enabled for development
- [ ] Standard response envelope on every endpoint

## Member 4 Checklist

- [ ] GET /alerts, PUT /alerts/{id}/acknowledge working
- [ ] GET /admin/alerts with role guard (403 for non-admin)
- [ ] FCM push notification on misplacement
- [ ] Seed script — 3 test users created
- [ ] Pytest integration tests for all 10 endpoints

---

## Integration Week Plan (Week 5)

| Day | Task | Who |
|-----|------|-----|
| Day 1 | Backend running, frontend switches BASE_URL | All |
| Day 2 | Auth flow tested end to end | M1 + M3 |
| Day 3 | Scan flow + misplacement + notification tested | M1 + M4 |
| Day 4 | Alerts flow + admin tested | M2 + M4 |
| Day 5 | Full run, fix bugs, freeze for submission | All |

---

## Test Credentials (Member 4 creates via seed script)

| User | Email | Password | Role |
|------|-------|----------|------|
| Test User A | usera@test.com | Test@1234 | user |
| Test User B | userb@test.com | Test@1234 | user |
| Admin | admin@test.com | Admin@1234 | admin |

---

*QR File Tracker | Community Engineering Project | April 2026*