# DroidQuest content data

Local-first curriculum data and portable Node.js tooling for DroidQuest.

The current release defines the complete 12-level, 52-week route and publishes Weeks 1–42: complete Levels 1–9 and the first three weeks of Level 10. It contains 242 focused learning lessons, 242 lesson quizzes, 242 optional practice challenges, weekly checkpoints, nine completed level checkpoints, 254 glossary terms, progression metadata, and 49 badges. Week 43 and Levels 11–12 remain visible as planned previews without placeholder lessons.

Level 1 progresses from first Kotlin instructions through a reusable Expense Manager. Level 2 adds Android tooling and platform fundamentals. Level 3 covers declarative UI through an accessible adaptive Shopping capstone. Level 4 teaches structured coroutines, networking, persistence and offline-first synchronization. Level 5 develops production architecture and modularisation. Level 6 integrates platform capabilities. Level 7 builds release confidence through testing and quality evidence. Level 8 covers threat-based Android security engineering. Level 9 completes Gradle and build engineering with signed artifacts, R8 evidence, protected CI/CD, staged rollout, provenance, and recovery. Published Level 10 material covers evidence-led profiling, Perfetto, memory, jank, battery, networking, app size, startup, Baseline and Startup Profiles, ANRs, crashes, OOM, retry control, kill switches, and compatible migrations. Each Learn stage is capped at 10 minutes; quiz, recall, challenge, and optional reading time is separate and learner-directed.

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
