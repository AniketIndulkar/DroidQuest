# DroidQuest content data

Local-first curriculum data and portable Node.js tooling for DroidQuest.

The current release defines the complete 12-level, 52-week route and publishes Weeks 1–46: complete Levels 1–10 and the first three weeks of Level 11. It contains 266 focused learning lessons, 266 lesson quizzes, 266 optional practice challenges, weekly checkpoints, ten completed level checkpoints, 277 glossary terms, progression metadata, and 53 badges. Week 47 and Level 12 remain visible as planned previews without placeholder lessons.

Level 1 progresses from first Kotlin instructions through a reusable Expense Manager. Level 2 adds Android tooling and platform fundamentals. Level 3 covers declarative UI through an accessible adaptive Shopping capstone. Level 4 teaches structured coroutines, networking, persistence and offline-first synchronization. Level 5 develops production architecture and modularisation. Level 6 integrates platform capabilities. Level 7 builds release confidence through testing and quality evidence. Level 8 covers threat-based Android security engineering. Level 9 completes Gradle, release, and CI/CD engineering. Level 10 now completes performance, reliability, privacy-safe observability, field health, release gates, and its evidence capstone. Published Level 11 material traces Linux process identity, Zygote, system_server, Binder and framework services; application launch and threading; and the View, Compose, RenderThread, BufferQueue, SurfaceFlinger, and HWC rendering pipeline. Each Learn stage is capped at 10 minutes; quiz, recall, challenge, and optional reading time is separate and learner-directed.

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
