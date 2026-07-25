# DroidQuest content data

Local-first curriculum data and portable Node.js tooling for DroidQuest.

The current release defines the complete 12-level, 52-week route and publishes Weeks 1–38: complete Levels 1–8 and the first four weeks of Level 9. It contains 218 focused learning lessons, 218 lesson quizzes, 218 optional practice challenges, weekly checkpoints, eight completed level checkpoints, 230 glossary terms, progression metadata, and 45 badges. Week 39 and Levels 10–12 remain visible as planned previews without placeholder lessons.

Level 1 progresses from first Kotlin instructions through a reusable Expense Manager. Level 2 adds Android tooling and platform fundamentals. Level 3 covers declarative UI through an accessible adaptive Shopping capstone. Level 4 teaches structured coroutines, networking, persistence and offline-first synchronization. Level 5 develops production architecture and modularisation. Level 6 integrates platform capabilities. Level 7 builds release confidence through testing and quality evidence. Level 8 covers threat-based Android security engineering. Published Level 9 material now covers Gradle’s execution model, Android variants and dependency governance, tested convention plugins, custom generation, TestKit, configuration/build caches, KSP and ABI-aware performance. Each Learn stage is capped at 10 minutes; quiz, recall, challenge, and optional reading time is separate and learner-directed.

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
