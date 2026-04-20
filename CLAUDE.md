# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android-only disc golf training app. Single module (`:app`), Kotlin + Jetpack Compose, `minSdk = targetSdk = compileSdk = 36`, Java 11 bytecode. Gradle version catalog at `gradle/libs.versions.toml` — prefer updating versions there rather than inline in `app/build.gradle.kts`.

## Build / test commands

Use the Gradle wrapper. On this Windows host the shell is Git Bash, so `./gradlew` works; `gradlew.bat` is the native alternative.

- `./gradlew assembleDebug` — build the debug APK
- `./gradlew test` / `./gradlew testDebugUnitTest` — JVM unit tests under `app/src/test/`
- `./gradlew testDebugUnitTest --tests "com.eewill.discgolftraining.DirectionCountsTest"` — single test class
- `./gradlew connectedAndroidTest` — instrumentation tests (requires a connected device/emulator)
- `./gradlew lint` — Android lint

Do not run `installDebug` or otherwise install the APK — the user installs manually after a successful build (see memory `feedback_no_apk_install.md`).

### Secrets

`local.properties` must define `MAPS_API_KEY=...` for the Google Maps SDK used by the approach-practice map features. The value is injected into `AndroidManifest.xml` via a `manifestPlaceholders` entry in `app/build.gradle.kts`. `local.properties` is gitignored; there is no committed template.

## Architecture

### Application wiring (manual DI)

`DiscGolfApp` (registered in the manifest) is the only DI container. It lazily constructs the Room `AppDatabase` and three repositories: `RoundRepository`, `DiscRepository`, `ApproachRoundRepository`. There is no Hilt/Koin.

Screens reach these through `Context` extensions in `ui/ViewModelFactory.kt`:

```kotlin
fun Context.repository(): RoundRepository
fun Context.discRepository(): DiscRepository
fun Context.approachRoundRepository(): ApproachRoundRepository
```

ViewModels are instantiated via the `simpleFactory { ... }` helper in that same file, which wraps `viewModelFactory { initializer { ... } }`. Use this pattern for new screens — don't introduce a different DI mechanism.

### Navigation

`MainActivity` sets `DiscGolfNavHost` as content. All routes live in `ui/Nav.kt` under `object Routes`. Two parallel training flows branch off `HOME`:

- **Gap practice** — `SETUP` → `SETUP_PICKER` (reuse previous round's image/gap) → `ACTIVE` → `SUMMARY` → `REPLAY`. The picker returns its result by writing `REUSED_ROUND_ID_KEY` into the caller's `savedStateHandle` — preserve that pattern if adding similar back-propagation flows.
- **Approach practice** — `APPROACH_SETUP` → `APPROACH_ACTIVE` → `APPROACH_SUMMARY`. Uses GPS / Google Maps for target placement.

Shared destinations: `HISTORY`, `STATS`, `SETTINGS`, `DISC_DETAIL`. Nav transitions are explicitly disabled (`EnterTransition.None` everywhere).

### Data layer (Room)

`AppDatabase` (in `data/`) is at **version 8** with `exportSchema = true` — schemas are checked into `app/schemas/com.eewill.discgolftraining.data.AppDatabase/`. When you change an `@Entity`:

1. Bump the version in `AppDatabase.kt`.
2. Add a `Migration` object to `data/AppMigrations.kt` and register it in the `addMigrations(...)` chain inside `AppDatabase.build`.
3. Let KSP regenerate the schema JSON on next build and commit it alongside the migration. Never drop the old schema files — migration tests rely on them.

Destructive migration is not configured; a missing migration will crash at runtime.

Two parallel round models coexist and should not be merged:

- `RoundEntity` / `ThrowEntity` (tables `rounds`, `throws`) — gap-practice rounds with an image-backed target (`imagePath`, normalized gap rect) and hit/miss throws with `(x, y)` coordinates. `discDataMode: DiscDataMode` (`NONE` / `TYPE` / `DISC`) controls how throws are associated with discs.
- `ApproachRoundEntity` / `ApproachRoundDiscEntity` / `ApproachThrowEntity` (tables `approach_rounds`, `approach_round_discs`, `approach_throws`) — approach-practice rounds with GPS target + landing coordinates and a sortable disc lineup.

`DiscEntity` (`discs`) is shared between both flows. Throws reference discs with `ON DELETE SET NULL`; round-disc joins use `ON DELETE CASCADE`.

### UI layer

One Compose package per feature under `ui/`: `home`, `setup`, `picker`, `active`, `summary`, `history`, `stats`, `replay`, `settings`, `disc`, and the approach sub-tree `ui/approach/{setup,active,summary,map}`. Each feature typically has a `*Screen.kt` + `*ViewModel.kt` pair. Shared Compose widgets live in `ui/components/`, theming in `ui/theme/`.

Image files captured during gap setup are stored via `util/ImageFiles.kt` and referenced by path in `RoundEntity.imagePath`; a `FileProvider` (`${applicationId}.fileprovider`) is declared in the manifest for sharing them.

## Tests

JVM unit tests are the primary suite — see `app/src/test/java/...` (`CoordinateMappingTest`, `DirectionCountsTest`, `SetupStateTest`). They cover pure logic (coordinate math, state reducers) and don't touch Room. The single instrumentation test is a stub.
