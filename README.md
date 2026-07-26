# DroidQuest

A local-first, gamified Android-learning app and the versioned curriculum that powers it. DroidQuest takes a learner from zero programming experience to Android platform expertise through a roadmap of levels, lessons, quizzes, and challenges — all bundled into the app and fully usable **offline**.

This repository holds the shared curriculum and two aligned native clients:

| Path | What it is |
|------|-----------|
| [`data/`](data/) | The curriculum **content repository** — the single source of truth. Versioned JSON validated against JSON Schemas, plus generated indexes (content index, search index, roadmap graph). |
| [`DroidQuest-Android/`](DroidQuest-Android/) | The **Android app** (Jetpack Compose + Material 3). Bundles a verified snapshot of `data/content` and renders the whole curriculum. See its [README](DroidQuest-Android/README.md). |
| [`DroidQuest-iOS/`](DroidQuest-iOS/) | The **iOS app** (SwiftUI). Bundles and verifies the same `data/content` snapshot, stable IDs, progression, quizzes, rewards, reviews, search, and local-first progress. See its [README](DroidQuest-iOS/README.md). |

## How content flows into the app

```
                                      ┌─(Gradle sync)─▶ Android APK assets ─▶ Compose UI
data/content/*.json (source of truth) ┤
                                      └─(Xcode resource)─▶ iOS app bundle ──▶ SwiftUI

Both clients verify content API compatibility, release version, counts, and indexed SHA-256 hashes.
```

- Content is shipped inside each platform's app bundle. Neither installed app reads the repository at runtime, and normal learning needs no network.
- Android embeds JSON through its Gradle sync task; iOS embeds the same folder through an Xcode resource reference. Both re-verify the content index's SHA-256 hashes when loading.
- Because content is compiled in, **editing `data/` requires an app rebuild** for the change to appear.

## The content model (summary)

Hierarchy: **curriculum → level → week → lesson**. See [`data/docs/CONTENT_MODEL.md`](data/docs/CONTENT_MODEL.md) for the full contract.

- **Levels** (`content/categories/`) — 12 ordered levels. Status is `planned`, `in_progress`, or `complete` (describes authored content, not learner completion).
- **Lessons** (`content/lessons/`) — six reveal stages: Scout, Learn (portable content blocks: paragraph / code / callout / flow / table / list, plus further-reading links), Inspect, Trap Check, Challenge intro, Recall. `estimatedLearningMinutes` covers the Learn stage only (1–10 min).
- **Quizzes** (`content/quizzes/`) — nine question types: single_choice, multiple_choice, true_false, fill_blank, order_steps, match_pairs, code_output, spot_bug, short_answer.
- **Challenges** (`content/challenges/`) — optional practice with hints, starter code, solution outline, verification.
- **Roadmap** (`content/roadmap/` + `generated/roadmap-graph.json`) — nodes with `unlockPrerequisites` drive progression. Planned `level_preview` nodes cannot be started.
- Plus **badges** and a **glossary**.

## Working with the content repo

```bash
cd data
npm install
npm run generate      # regenerate content-index, search-index, roadmap-graph, review page
npm run test:content  # validate schemas + cross-references, confirm generated files are current
```

Validation rules: [`data/docs/VALIDATION_RULES.md`](data/docs/VALIDATION_RULES.md). Authoring: [`data/docs/AUTHORING_GUIDE.md`](data/docs/AUTHORING_GUIDE.md).

## Building the apps

```bash
cd DroidQuest-Android
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Full build/test/run details are in [`DroidQuest-Android/README.md`](DroidQuest-Android/README.md).

```bash
cd DroidQuest-iOS
ruby scripts/generate_project.rb
xcodebuild -project DroidQuest.xcodeproj -scheme DroidQuest \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' build test
```

Full iOS details are in [`DroidQuest-iOS/README.md`](DroidQuest-iOS/README.md).

## Current release

The content release is versioned in [`data/content/curriculum.json`](data/content/curriculum.json) (`version`). Counts live in `content/generated/content-index.json`. The app is entirely data-driven, so these numbers grow as content is authored — nothing about them is hardcoded in the app.

## License / status

Personal project. Content and app are versioned together; stable content IDs are permanent once released.
