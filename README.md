# Disc Golf Training

Android app for tracking disc golf practice sessions across three drill types. Every throw is saved so you can review accuracy trends, compare discs, and replay past rounds.

## Practice modes

### Gap throwing
Photograph a real gap in the field (or any target), mark its bounds and width on the image, then log each throw as a hit or miss at a tapped `(x, y)` coordinate on the photo. Sessions can be replayed to see the spread of throws overlaid on the gap.

### Approach shots
GPS-based. Drop a target on a Google Map, walk to your throwing spot, and record where each disc lands. The app stores the target location, landing coordinates, and per-throw disc so you can analyze distance error and directional bias by disc.

### Putting
Pick a distance range (e.g. 10–30 ft), an interval (e.g. every 5 ft), and throws per position. The app walks you through the resulting positions and records makes/misses at each distance.

## Other features

- **History** — filter, group, and browse all past rounds across the three drill types
- **Statistics** — aggregated accuracy breakdowns, including per-disc stats and putting percentages by distance
- **Disc management** — tracks your discs (type, manufacturer, etc.) and a per-disc detail view with usage stats
- **Replay** — visualize gap-practice rounds with all throws plotted on the original image

## Tech

- Kotlin + Jetpack Compose, single `:app` module
- `minSdk = targetSdk = compileSdk = 36`, Java 11 bytecode
- Room for persistence (schema v9, migrations exported to `app/schemas/`)
- Manual DI via `DiscGolfApp` — no Hilt/Koin
- Google Maps SDK for the approach-practice map

## Build

Uses the Gradle wrapper. On Windows the repo expects Git Bash.

```bash
./gradlew assembleDebug          # debug APK
./gradlew testDebugUnitTest      # JVM unit tests
./gradlew connectedAndroidTest   # requires a device/emulator
./gradlew lint
```

### Required: Maps API key

Create `local.properties` at the repo root (gitignored) with:

```
MAPS_API_KEY=your_google_maps_sdk_key
```

The value is injected into the manifest via `manifestPlaceholders` in `app/build.gradle.kts`. Approach-practice features will not load the map without it.

## Project layout

- `app/src/main/java/com/eewill/discgolftraining/data/` — Room entities, DAOs, repositories, migrations
- `app/src/main/java/com/eewill/discgolftraining/ui/` — one Compose package per feature (`home`, `setup`, `active`, `summary`, `history`, `stats`, `replay`, `settings`, `disc`, plus `approach/` and `putting/` sub-trees)
- `app/schemas/` — exported Room schema JSON per database version
- `app/src/test/` — JVM unit tests for pure logic (coordinate math, state reducers, putting position generation)

See `CLAUDE.md` for deeper architecture notes.
