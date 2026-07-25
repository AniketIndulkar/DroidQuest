# DroidQuest content model

DroidQuest stores its curriculum as versioned JSON that can be bundled with the Android application. Normal learning, search, progression, quizzes, challenges, recall, and glossary lookup require no API call.

## Curriculum hierarchy

The hierarchy is curriculum → level → week → lesson. `content/curriculum.json` lists all 12 levels and the weeks whose content is currently authored. A category file represents one level, not a decorative map region. Its `status` is:

- `planned`: outline metadata exists, but no lessons are published.
- `in_progress`: at least one week is published, but the level is incomplete.
- `complete`: the full level and its final checkpoint are published.

This distinction lets the app preview the 52-week expert route without treating un-authored material as learnable content.

## Lesson timing

`estimatedLearningMinutes` describes only the `learn` stage and must be between 1 and 10 minutes. It does not include Scout, Inspect, Trap Check, recall, quiz, or challenge time. Those activities vary by learner and must not be added to the learning estimate shown by the app.

## Reveal stages

Every lesson has six stages:

1. `scout` explains the purpose, real Android use, and concrete outcome. It creates context; it does not replace instruction.
2. `learn` contains the self-contained teaching material. It has at least three sections composed from portable content blocks.
3. `inspect` provides one complete code example, a walkthrough, and expected output.
4. `trap_check` explains common mistakes, why they fail, and how to fix them.
5. `challenge_intro` describes the associated practice task and a successful result.
6. `recall` contains self-paced prompts with reference answers.

Supported Learn blocks are `paragraph`, `code`, `callout`, `flow`, `table`, and `list`. These are semantic data, not HTML. The Android client can render them as Compose components and the local review page renders the same structures in a browser.

Every Learn stage ends with two to four optional `furtherReading` resources. Each resource records a learner-facing title, publisher, type, URL, and a short explanation of its value. At least one must be an official document or codelab. These links are optional enrichment and are not included in `estimatedLearningMinutes`.

## Quizzes and challenges

Each lesson links to exactly one lesson quiz and one challenge. Lesson quizzes contain at least three explained questions. Checkpoint quizzes may link to several lessons through `linkedLessonIds`; every question still identifies the specific lesson it assesses.

Challenges are separate practice activities with starter code, hints, success criteria, a solution outline, and verification steps. Their `estimatedMinutes` is a practice estimate and is distinct from lesson reading time.

## Progression

Roadmap nodes use `available` or `planned` status. Available lesson nodes link to lesson files, and available checkpoint nodes link to quizzes. `level_preview` nodes contain no teaching content and cannot be completed. Unlock eligibility is derived from `unlockPrerequisites` in `content/generated/roadmap-graph.json`.

The current graph publishes complete Levels 1–8 and Weeks 35–38 of Level 9. Lessons and weekly checkpoints form an acyclic prerequisite chain. Level 9 starts only after the Level 8 boss; its planned Week 39 preview follows the Week 38 checkpoint, and the Level 10 preview remains locked behind that unfinished week. Later level previews remain chained in curriculum order.

## Android consumption

The future Android app should:

1. Bundle a known-compatible content release in application assets.
2. Read `generated/content-index.json` to discover records and verify their SHA-256 hashes.
3. Deserialize JSON into versioned domain models behind a repository boundary.
4. Build local search from `generated/search-index.json`.
5. Use `generated/roadmap-graph.json` for progression and unlock checks.
6. Store learner progress separately from curriculum files using stable IDs.
7. Optionally download a newer signed GitHub release, validate it, and atomically replace the local content snapshot.

Learner progress must never depend on display titles, array positions, or filenames alone. Stable IDs are permanent once released.
