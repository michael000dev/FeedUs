# FeedUs — Agent Instructions

FeedUs is a cross-platform, open-source RSS client built with Kotlin Multiplatform (KMP) and Compose Multiplatform, targeting Android, iOS, and Desktop (JVM).

## Build & Run Commands

```bash
./gradlew clean
./gradlew build              # compile all targets
./gradlew run                # run desktop app
./gradlew :composeApp:testDebugUnitTest --tests "com.seazon.feedus.SomeTest"  # single test
```

## Module Structure

The project has two Gradle modules:

- **`:composeApp`** — UI layer (`com.seazon.feedus`). Depends on `:lib`. Targets Android + iOS + Desktop.
- **`:lib`** — Business logic, RSS API abstractions, HTTP, parsers (`com.seazon.feedme.lib`). No Android target (Desktop + iOS only). Publishable KMP library.

Key source-set layout (same pattern in both modules):
```
src/
  commonMain/     # shared Kotlin code
  androidMain/    # Android actuals
  iosMain/        # iOS actuals
  desktopMain/    # JVM/Desktop actuals
```

## Architecture

### MVVM + Koin DI

Every feature screen splits into exactly four files:

| File | Role |
|---|---|
| `XxxScreen.kt` | `@Composable` — obtains ViewModel via `koinViewModel()`, wires events/navigation, renders `Toaster` |
| `XxxScreenComposable.kt` | Pure stateless `@Composable` — receives state and callbacks only |
| `XxxScreenState.kt` | Immutable `data class` holding all UI state |
| `XxxViewModel.kt` | `ViewModel` subclass — holds `MutableStateFlow<XxxScreenState>` and `MutableStateFlow<Event?>` |

ViewModels extend `BaseViewModel` which provides a `process {}` helper that switches to `Dispatchers.IO`.

### State & Events

- UI state: `private val _state = MutableStateFlow(XxxScreenState())` exposed as `val state: StateFlow<XxxScreenState>`.
- One-off events (errors, navigation triggers): `sealed class Event` + `private val _eventFlow = MutableStateFlow<Event?>(null)`, collected with `LaunchedEffect` in the Screen composable.

### Dependency Injection (Koin)

`appModule` in `composeApp/commonMain/di/AppModule.kt` registers shared singletons and ViewModels. Each platform provides `actual val platformModule: Module` (e.g., `DatabaseDriverFactory`).

### Navigation

Uses Jetpack Compose Navigation. Routes defined in the `Screen` enum (`composeApp/commonMain/Screen.kt`). Navigation arguments are passed via the global `object A` in `App.kt` (not type-safe nav args).

## `:lib` Layer

### RSS API Abstraction

`RssApi` (`lib/.../rss/service/RssApi.kt`) is the single interface all service implementations must satisfy. Implementations live under `rss/service/<service-name>/`:

- `feedly/`, `inoreader/`, `freshrss/`, `fever/`, `feedbin/`, `folo/`, `ttrss/`

`RssSDK` (in `:composeApp`) wraps token refresh and returns a ready `RssApi` instance.

### HTTP

Ktor client with platform engines:
- Android → OkHttp
- Desktop → OkHttp
- iOS → Darwin

### Local Database

SQLDelight `AppDatabase`. Schema defined in `.sq` files at:
```
composeApp/src/commonMain/sqldelight/com/seazon/feedus/cache/AppDatabase.sq
```
Tables: `Feed`, `Item`, `Category`, `Label`. The generated Kotlin is under the `com.seazon.feedus.cache` package.

`DatabaseDriverFactory` is an `expect` class with platform `actual` implementations.

### Persistence

User credentials/tokens → `TokenSettings` (multiplatform-settings).  
App preferences → `AppSettings` (multiplatform-settings).

## Key Conventions

- **Screen-level errors** are surfaced via Sonner toasts (`com.dokar.sonner`). Call `toaster.show(message)` in the `LaunchedEffect` that collects `eventFlow`.
- **`lib` module** contains no Android-specific code; keep it that way when adding new network or parser logic.
- **Feed ID standard**: all `feedId` values use the Feedly format `feed/{feed_url}` as the canonical form across all API implementations.
- **Unread count computation**: when the service doesn't provide per-category unread counts, they are aggregated client-side from per-feed counts (see `FeedsViewModel.fetchUnreadCount`).
- **API capability flags**: before calling optional API methods, check the relevant `supportXxx()` method on `RssApi` (e.g., `supportPagingFetchIds()`, `supportFetchByFeed()`, `supportStar()`).
