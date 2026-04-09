# UI Integration Update Guide & Handover

This document outlines the required steps to connect the newly integrated "Coral Pulse" UI with the Jetpack Compose ViewModels and the remote backend. 

The visual layer (UI) has been successfully completely modernized. The screens are currently running on **mock state data** so that they render correctly in previews and on devices. The next step is connecting this UI to the live data flow.

---

## 🔌 1. Frontend-Backend Connection Details

Before the application can communicate with the backend, the networking layer needs to be updated with the live backend parameters.

- **Base URL:** The `RetrofitClient` or network module must be updated to point to the live server IP/hostname provided by the backend team. (e.g., `http://192.168.X.X:3000/api/` for local testing).
- **JWT Authentication:** The UI relies on valid session states. All secured routes must include the `Authorization: Bearer <token>` header. The `TokenManager` utility is designed for this.
- **Cleartext Traffic:** If testing locally over HTTP (not HTTPS), ensure `android:usesCleartextTraffic="true"` is enabled in `AndroidManifest.xml`.
- **CORS:** The backend API must be configured to allow requests from the Android Application (or allow all `*` during development).

---

## 👨‍💻 Member 2 Instructions: State & ViewModel Linking

Your primary goal is to strip out the mock data in the UI screens and replace it with the `ViewModel` states.

**1. Fix Existing Build Errors**
The current codebase has compilation errors inside the `com.ganesh.qrtracker.viewmodel.*` package (e.g., Unresolved references for `getInstance`, `data`, and mismatched types). Fix these backend architecture integration issues first.
Additionally, fix the syntax error in `com.ganesh.qrtracker.network.models.ScanResponse` (Line 7 has a trailing `R.string` instead of a standard type/comma).

**2. Replace Mock States in Screens**
In files like `LoginScreen.kt`, `PackageListScreen.kt`, and `CreatePackageScreen.kt`, search for the `// TODO` markers. 
- You will find local state declarations like: `var uiState by remember { mutableStateOf(LoginUiState()) }`.
- Replace these with standard Compose ViewModel collections: `val uiState by viewModel.uiState.collectAsState()`.
- Replace the mocked action triggers (e.g., `navController.navigate(...)` inside onClick handlers) with `viewModel.login(email, password)`. Let the ViewModel's state emission govern when the navigation actually occurs.

**3. NavGraph Re-Integration**
The `ALERTS` and `ADMIN_ALERTS` composable routes in `NavGraph.kt` have been commented out. They require a `TokenManager` instance to be passed in. Once your ViewModel layer is stable, instantiate the `TokenManager` (or grab it via Dependency Injection/Hilt) and uncomment those routes in the graph.

---

## 👨‍💻 Member 3 & 4 Instructions: Backend API Validation

The UI relies on strict adherence to the data models. The backend needs to ensure responses perfectly match the expected Kotlin Data Classes.

**1. Response Payloads**
- **Scan Response:** The UI expects fields like `result` ("valid" or "misplaced"), `package_description`, and `owner_name`. Ensure the backend returns exactly these keys in JSON.
- **Alert Feed:** Ensure the `/alerts` endpoints return a list of JSON objects matching the `AlertResponse` data class schema so they populate the `AlertFeedScreen` correctly.

**2. JWT and Roles**
- The backend must return a token and the user's role (`user` vs `admin`) upon successful authentication on the `/login` endpoint.
- Ensure the backend properly rejects unauthorized tokens with a `401 Unauthorized` status, which will trigger the `onSessionExpired` callbacks in the UI and boot the user back to the login screen.

**3. Realtime Updates (Optional)**
- If "Active Pulse" features real-time progress bars, ensure WebSockets or polling endpoints are established so Member 2 can feed that live percentage back into the `TrackingProgressBar` component.
