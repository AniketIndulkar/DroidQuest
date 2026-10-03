# DroidQuest Desktop

Tauri 2 desktop client for macOS and Windows. It reuses the shared DroidQuest
React presentation and framework-independent domain packages while keeping
desktop storage and native integration adapters local to this application.

## Development

```bash
npm ci --prefix ../packages/droidquest-domain
npm ci --prefix ../packages/droidquest-react
npm install
npm run desktop:dev
```

## Validation

```bash
npm test
npm run desktop:build:app
```

`desktop:build:app` produces the native macOS application bundle without also
creating a disk image. Use `npm run desktop:build` when preparing all configured
installers for distribution.

Curriculum content is copied from `../data/content` before development and
production builds. Do not edit the generated `public/content` snapshot.
