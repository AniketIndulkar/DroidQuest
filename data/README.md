# DroidQuest content data

Local-first curriculum data and portable Node.js tooling for DroidQuest.

Version 1.0 publishes the complete 12-level, 52-week DroidQuest route. It contains 302 focused learning lessons, 302 lesson quizzes, 302 optional practice challenges, weekly checkpoints, all twelve level checkpoints, 313 glossary terms, progression metadata, and 59 badges. Every roadmap node is authored and available; the Level 12 boss is the terminal curriculum milestone.

Level 1 progresses from first Kotlin instructions through a reusable Expense Manager. Level 2 adds Android tooling and platform fundamentals. Level 3 covers declarative UI through an accessible adaptive Shopping capstone. Level 4 teaches structured coroutines, networking, persistence, and offline-first synchronization. Levels 5–10 develop architecture, platform capabilities, testing, security, build/release engineering, performance, reliability, and observability. Level 11 traces Android through processes, Binder, launch, rendering, ART, memory, power, and diagnostics. Level 12 keeps every expert track available: SDK engineering, on-device AI, native Android, form factors, AOSP/platform contribution, and regulated enterprise development, ending with a reproducible expert change defense. Each Learn stage is capped at 10 minutes; quiz, recall, challenge, and optional reading time is separate and learner-directed.

## Commands

```bash
npm install
npm run validate
npm run generate
npm run test:content
npm run review
```

Open [review.html](review.html) directly in a browser to inspect all authored and planned content. It embeds the JSON snapshot and needs no server or network connection.

Start with `docs/CONTENT_MODEL.md`, then use `docs/AUTHORING_GUIDE.md` when adding content. Research sources and curriculum decisions are recorded in `docs/ROADMAP_RESEARCH.md`.
