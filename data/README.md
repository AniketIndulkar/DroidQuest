# DroidQuest content data

Local-first curriculum data and portable Node.js tooling for DroidQuest.

The current release defines the complete 12-level, 52-week route and publishes Weeks 1–22: complete Levels 1–5. It contains 122 focused learning lessons, 122 lesson quizzes, 122 optional practice challenges, weekly checkpoints, five level checkpoints, 134 glossary terms, progression metadata, and 26 badges. Levels 6–12 remain visible as planned previews without placeholder lessons.

Level 1 progresses from first Kotlin instructions through a reusable Expense Manager. Level 2 adds Android tooling, platform lifecycles and components, retained Views, Compose interoperability, and a local-first Personal Notes capstone. Level 3 covers declarative UI through an accessible adaptive Shopping capstone. Level 4 teaches structured coroutines, Flow, resilient networking, persistence, synchronization, and an Offline-First News Reader. Level 5 develops production architecture, UDF, repository and use-case boundaries, manual and Hilt dependency injection, evidence-based modularisation, architecture testing, and a Multi-Module Banking Demo. Each Learn stage is capped at 10 minutes; quiz, recall, challenge, and optional reading time is separate and learner-directed.

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
