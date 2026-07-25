# DroidQuest content data

Local-first curriculum data and portable Node.js tooling for DroidQuest.

The current release defines the complete 12-level, 52-week route and publishes Weeks 1–34: complete Levels 1–8. It contains 194 focused learning lessons, 194 lesson quizzes, 194 optional practice challenges, weekly checkpoints, eight level checkpoints, 206 glossary terms, progression metadata, and 41 badges. Levels 9–12 remain visible as planned previews without placeholder lessons.

Level 1 progresses from first Kotlin instructions through a reusable Expense Manager. Level 2 adds Android tooling, platform lifecycles and components, retained Views, Compose interoperability, and a local-first Personal Notes capstone. Level 3 covers declarative UI through an accessible adaptive Shopping capstone. Level 4 teaches structured coroutines, Flow, resilient networking, persistence, synchronization, and an Offline-First News Reader. Level 5 develops production architecture, dependency injection, modularisation, architecture testing, and a Multi-Module Banking Demo. Level 6 integrates platform capabilities in a local-first Travel Companion. Level 7 builds release confidence with unit, integration, UI, accessibility, static, coverage, and compatibility evidence. Level 8 protects the system through threat modelling, client/server authority, Keystore and cryptography, transport and pin rotation, secret and signing-key boundaries, OAuth, Integrity, replay/fraud controls, and a Banking Security Review. Each Learn stage is capped at 10 minutes; quiz, recall, challenge, and optional reading time is separate and learner-directed.

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
