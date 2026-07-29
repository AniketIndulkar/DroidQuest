# DroidQuest Web

Local-first web client for the shared DroidQuest curriculum. It renders the same roadmap, lessons, quizzes, challenges, search index, and spaced-repetition content as the Android and iOS apps.

## Local development

Requires Node.js 22.13 or newer.

```bash
npm install
npm run dev
```

Open `http://localhost:3000`.

`predev` and `prebuild` copy the verified snapshot from `../data/content` into the generated, ignored `public/content` directory. Edit curriculum content only in `../data`; the website must not maintain a second source of truth.

## Commands

```bash
npm run sync:content  # refresh the shared curriculum snapshot
npm run build         # production build
npm test              # build and verify the app shell/content snapshot
```

## Progress

Progress is currently stored in browser local storage under `droidquest.learner-progress.v1`. The repository boundary in `lib/progress.ts` is intentionally separate from the UI so the next phase can add Supabase-backed synchronization while preserving offline-first behavior.
