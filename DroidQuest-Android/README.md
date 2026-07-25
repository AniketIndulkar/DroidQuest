# DroidQuest — Android app

Jetpack Compose (Material 3) client for the DroidQuest curriculum. It bundles a verified snapshot of the [`../data`](../data) content repository and renders the full learning experience — roadmap, staged lessons, nine quiz types, challenges, search, and glossary — **entirely offline**. AI assistance and GitHub sync are optional network extras that never block learning.

> Part of the [DroidQuest](../README.md) monorepo. See the root README for how content flows from `data/` into the app.

## Tech stack

- Kotlin + Jetpack Compose + Material 3 (single `:app` module)
- `kotlinx.serialization` for content JSON
- Jetpack DataStore (Preferences) for learner progress
- AndroidX Lifecycle ViewModel + `StateFlow`
- AGP 9 with built-in Kotlin; `minSdk 24`, `compileSdk 37`

## Architecture

Content is immutable curriculum data; learner progress is stored separately. Pure logic is isolated behind policy objects so product rules can change without touching the UI or the data.

```
content/                      Content boundary
  model/ContentDtos.kt        Versioned kotlinx.serialization DTOs (no Compose types)
  ContentSource.kt            Asset (app) / File (test) byte source
  DroidQuestContentRepository Loads index, verifies content API + SHA-256, exposes LoadedContent
  ContentLoadState.kt         Loading / Success / Error (missing, malformed, unsupported, hash mismatch)

domain/                       Pure, unit-tested — no Android, no Compose
  ProgressionPolicy.kt        Roadmap unlock evaluation over the generated graph
  QuizEvaluator.kt            All 9 question types + answer normalization
  RewardPolicy.kt             Score→stars, idempotent first-pass rewards
  SearchRouter.kt             Search result → destination

progress/                     Learner progress persistence
  ProgressRepository.kt       Interface (stable-ID keyed)
  DataStoreProgressRepository Idempotent reward awarding inside atomic edits

di/AppContainer.kt            Manual constructor injection (no DI framework)
ui/
  state/                      DroidQuestViewModel + immutable UiState + derivations
  lesson/LearnBlocks.kt       Compose renderers for every Learn block type
  screens/                    Home, QuestMap, RegionDetail, TopicDetail, Lesson,
                              Revision (quiz), Challenge, Search, Starred, Settings
  DroidQuestApp.kt            Root: async load, loading/error screens, bottom nav, AI FAB
```

### Content bundling (build-time)

The `data/content` repo stays the source of truth; nothing is duplicated into source control. At build time the `syncDroidQuestContent` Gradle task (in [`app/build.gradle.kts`](app/build.gradle.kts)) copies the JSON into generated assets at `droidquest/content/`, wired through AGP's Variant API so it runs before asset merging. Paths in `content-index.json` (`content/…`) resolve against the `droidquest` asset root.

At runtime `DroidQuestContentRepository` reads those assets, checks the content-API contract (`minimumAppContentApi` ≤ app `APP_CONTENT_API`), verifies every indexed **SHA-256**, and returns an explicit load state. There is **no silent fallback** — content problems surface a recoverable error screen with Retry.

Because the snapshot is compiled into the APK, **a `data/` change needs an app rebuild** to take effect. The sync task detects `data/content` changes automatically on the next build.

## Build, run, test

```bash
# Build
./gradlew :app:assembleDebug

# Install + launch
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n dev.novanest.droidquest/.MainActivity

# Verify
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest         # JVM unit tests (policies, repository, evaluators)
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest # instrumented (needs a device/emulator)
```

Refresh the app after editing `../data`:

```bash
./gradlew :app:assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Tests

- **Unit (`src/test`)** — parse + hash-verify all bundled content, index counts, content-API/version compatibility, stable-ID lookup, roadmap unlock/preview/first-node/completion, all nine quiz evaluators, passing score, idempotent rewards, persisted progress (via a fake repository), and search routing. Unit tests read `../data/content` directly (source of truth) via `FileContentSource`.
- **Instrumented (`src/androidTest`)** — on-device asset load + count verification, a full staged lesson render (paragraph, code, flow/table, reading link, trap, recall), and a non-MCQ (true/false) quiz render.

## Design notes

- Progression comes from the roadmap graph's `unlockPrerequisites`, never from array position or display titles. A lesson node completes when its lesson quiz passes; checkpoint/boss nodes complete when their quiz passes. Challenges are optional and never gate progression.
- Streak is intentionally **not** fabricated — it is shown as "not tracked yet" until real tracking exists.
- The score→stars mapping (`RewardPolicy`), unlock rules (`ProgressionPolicy`), grading (`QuizEvaluator`), and search routing (`SearchRouter`) are isolated policy classes, changeable without a data migration.
