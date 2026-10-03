# Desktop architecture

DroidQuest Desktop is a thin Tauri shell around the shared React application.
The Rust layer owns only operating-system capabilities; learning rules remain
in `@droidquest/domain`, and platform-neutral presentation remains in
`@droidquest/react`.

Native capabilities are exposed through narrow adapters. For example, lesson
resources use Tauri's opener plugin to launch the system browser, while the
shared React package only receives an `openExternalUrl` function.

Progress is local-first. Each platform supplies a `ProgressStore` adapter. A
future Supabase adapter will synchronize the same domain records without
coupling screens to a network client.

Curriculum content continues to use `data/content` as its single source of
truth. Each client packages a generated, read-only snapshot for offline use.
