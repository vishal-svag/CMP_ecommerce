# KMP Ready — E-commerce (Android + iOS)

Kotlin Multiplatform + Compose Multiplatform. Business logic and UI are shared; `androidApp` and
`iosApp` are thin shells.

## Module map

```
build-logic/convention   Gradle convention plugins (kmpready.kmp.library / .compose / .feature)
core/common              AppResult, AppError, AppConfig, DispatcherProvider, logging      (no deps)
core/domain              Models, repository interfaces, use cases                          (pure Kotlin)
core/security            SecureStorage: Android Keystore AES-GCM / iOS Keychain, TokenStore
core/network             Ktor client: bearer auth + refresh, retry, timeouts, pinning, error mapping
core/data                DTOs, mappers, repository implementations
core/designsystem        Atelier theme (Bodoni Moda + Plus Jakarta Sans, OFL), icons, stateless components
core/ui                  MVI base ViewModel, UiText, AppError→message, MoneyFormatter
feature/auth             Welcome → Sign in (step 1) → VIP Security Check (step 2: passcode / biometrics)
feature/home             Product grid w/ paging, pull-to-refresh, logout
shared                   Composition root: Koin graph, auth-gated root navigation, iOS framework
androidApp / iosApp      Platform entry points
```

Dependency rule: `feature → domain ← data`. Features never depend on each other, on `:core:data`
or on `:core:network`. Only `:shared` sees everything.

## Architecture

- **MVI** per screen: `State` (immutable data class) + `Intent` + one-shot `Effect` (Channel).
  Each screen has a stateful `XScreen` (wires the ViewModel) and a stateless, previewable `XContent`.
- **Auth gate**: `App()` observes `SessionState` and swaps between the unauthenticated and authenticated
  NavHosts. Login, logout and refresh-token rejection all flow through the session, so no screen
  navigates on auth events itself and nothing authenticated survives logout.
- **Errors**: transport exceptions are mapped to `AppError` in `safeApiCall` — the only place they
  exist. UI maps `AppError` → localized text exhaustively in `core/ui/ErrorText.kt`.
- **Guest mode**: `SessionState.Guest` (in-memory) opens the catalogue without an account; "Sign in"
  on Home ends the guest session and returns to Welcome.
- **Hero art**: `feature/auth/src/commonMain/composeResources/drawable/atelier_hero.xml` is a placeholder —
  delete it and add `atelier_hero.jpg` (4:5, ~1200×1500, rights-cleared).
- **Two-step sign-in**: `login()` returns a `LoginChallenge`; the session only starts after
  `verify(Passcode | DeviceBiometric)`. Step-1 tokens are held in memory and never reach the
  HTTP client until step 2 passes; leaving step 2 (✕, back, process death) discards them.
- **Keep me authenticated**: on → tokens encrypted on disk for 30 days (hard expiry);
  off → memory only, gone when the app process ends.
- **Before release (backend)**: replace `DevSecondFactorService` with a real API client; the server
  must not issue session tokens before 2FA, must deliver codes (SMS/email/push), enforce expiry /
  attempt limits, and verify a device-bound key for biometric approval. Apple + Passkey sign-in
  are stubbed (`signInWith` → `Unavailable`) until Apple token / WebAuthn verification exists.
- **Money** is stored in minor units (`Long`), never `Double`.

## Security checklist

| Area | Implementation |
|---|---|
| Token storage | Android Keystore (AES-256-GCM, non-exportable key); iOS Keychain `AfterFirstUnlockThisDeviceOnly` |
| Reinstall on iOS | Keychain wiped on first launch after install |
| Token refresh | Ktor `Auth` bearer; single-flight refresh; rejected refresh ⇒ wipe + `SessionEvent.Expired` |
| Token scope | Bearer header only sent to the API host |
| Transport | HTTPS enforced in release (`AppConfig`), cleartext blocked (Android NSC / iOS ATS) |
| Pinning | Supported on both engines via `AppConfig.certificatePins` — **add pins before launch** |
| Logging | Headers only, `Authorization`/`Cookie` redacted, never bodies; release logs ≥ WARN |
| Backups | `allowBackup=false` + data-extraction rules exclude secure prefs |
| Release build | R8 + resource shrinking; tooling only in `debug` |
| Signing | From git-ignored `keystore.properties` or CI env vars |
| Memory | Password cleared from state after login; `Credentials`/`AuthTokens` redact `toString()` |

## Running

- Android: `./gradlew :androidApp:installDebug`
- iOS (macOS only): open `iosApp/iosApp.xcodeproj` in Xcode and run.
- Demo login (DummyJSON mock backend): `emilys` / `emilyspass`
- Step-2 passcode (dev mock): printed to Logcat / Xcode console — filter by tag **`DevOtp`**.
  On a device with enrolled Face ID / fingerprint, **Scan** also works (dev mock accepts it).

Environment: Android `API_BASE_URL` in `androidApp/build.gradle.kts`; iOS `API_BASE_URL` in
`iosApp/Configuration/Config.xcconfig`. Endpoints live in `core/network/.../ApiEndpoints.kt`.

## Tests

- All shared tests on the JVM host: `./gradlew testAndroidHostTest`
- iOS simulator: `./gradlew iosSimulatorArm64Test`

## Adding a feature

1. `feature/<name>/build.gradle.kts` with `alias(libs.plugins.kmpready.kmp.feature)` + namespace.
2. `include(":feature:<name>")` in `settings.gradle.kts`.
3. Contract (`State`/`Intent`/`Effect`), `ViewModel : MviViewModel`, `Screen` + `Content`,
   `@Serializable` route + `NavGraphBuilder.<name>Graph()`, Koin module.
4. Register the Koin module and the graph in `:shared`.

## Release signing (`keystore.properties`, git-ignored)

```
storeFile=release.jks
storePassword=...
keyAlias=...
keyPassword=...
```
CI alternative: `KMPREADY_KEYSTORE_PATH`, `KMPREADY_KEYSTORE_PASSWORD`, `KMPREADY_KEY_ALIAS`, `KMPREADY_KEY_PASSWORD`.
