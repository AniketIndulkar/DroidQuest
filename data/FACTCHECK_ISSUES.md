# DroidQuest Curriculum Fact-Check Issues

Review of factual/technical accuracy of learning text in `content/lessons/` (302 lessons, 52 weeks, 12 levels).
No content was modified. Each issue lists lesson id, location, the claim, why it is wrong, and suggested fix.

Severity: **[HIGH]** factually wrong / misleading · **[MED]** imprecise or debatable · **[LOW]** nitpick/style-of-fact.

---

## Executive summary

All 302 lessons across the 52-week / 12-level curriculum were reviewed. **The factual/technical accuracy is excellent — no [HIGH] errors were found.** Kotlin, Compose, coroutines/Flow, the data layer, architecture/DI, platform capabilities, testing, security, Gradle, performance, Android internals, and the specialization tracks are all described correctly and with current API names (e.g. `Icons.AutoMirrored`, `enableEdgeToEdge`, Navigation 3, `LiteRT`, `ApplicationExitInfo`, stable-AIDL/VINTF). Notably strong: Levels 8 (security) and 11 (internals) are genuinely expert-level and correct, including subtle points like "`ActivityThread` is not a `Thread`" and JNI global refs acting as GC roots.

**Nothing here requires an urgent content fix for correctness.** The issues worth your attention are:

1. **[MED] Content-generation / templating defect across Levels 5–12 (≈250 lessons).** These levels were clearly produced from a per-level template, which introduced two mechanical problems that are *not* factual errors but do hurt quality:
   - **Broken warning-callout sentences** — every lesson's main callout ends with a dangling sentence fragment (the lesson's trap-mistake text pasted onto a generic sentence), producing grammatically broken run-ons. Example (L5): *"…adds value only when it protects a real contract. Putting Retrofit, Room, navigation, and formatting directly inside one ViewModel because it is convenient."*
   - **Generic, duplicated SCOUT / boilerplate paragraph / FLOW / table / INSPECT walkthrough / expectedOutput / one trap / two recall answers**, repeated verbatim in every lesson of a level. Consequence: the recall questions don't test the specific lesson (e.g. "What problem should hilt components… solve?" is answered with a generic non-answer), and INSPECT sections just re-show the section code with generic steps. Teaching value drops noticeably versus Levels 1–4.
2. **[MED — verify] One claim I could not confirm:** `level-06-week-23-06` says *"Android 15 dynamic rules can refine allowed [App Link] paths."* I'm not aware of such a feature (App Link path patterns are declared in the manifest; verification is via Digital Asset Links). Please check official docs; remove if it doesn't exist.
3. **[LOW] Two code-block language-tag mislabels** in Level 1 (shell vs kotlin) — affects syntax highlighting only.

Per-level detail follows.

---

## Level 01 — Programming Foundations (22 lessons)

Technical content is accurate throughout (Kotlin syntax, null safety, generics/variance, collections, exceptions, Big-O, HTTP/REST/JSON, git). Only issues found are code-block language-tag mislabels — they affect syntax highlighting, not correctness of the learning text.

- **[LOW]** `level-01-week-04-01-terminal-files-processes` — INSPECT code block is tagged `language: shell` but the code is Kotlin (`fun main(args: Array<String>) {...}`). Should be `kotlin`.
- **[LOW]** `level-01-week-04-02-git-collaboration-docs` — INSPECT code block is tagged `language: kotlin` but the code is shell/git (`git status`, `git diff --staged`, `git log`). Should be `shell`.

---

## Level 02 — Android Platform Fundamentals (21 lessons)

No factual issues found. Toolchain, Gradle/AGP, manifest, SDK levels (min/compile/target), packaging (APK/AAB), Application/Context, activity lifecycle, configuration changes, saved state / process death, resource qualifiers & plurals, intents & ActivityResult contracts, runtime permissions, services & WorkManager, broadcasts/ContentProvider/PendingIntent (FLAG_IMMUTABLE), tasks/back stack/App Links (assetlinks.json), XML layouts, ConstraintLayout & ViewBinding, fragments & Navigation, RecyclerView/DiffUtil, and Compose interop are all described accurately and with correct API constants.

---

## Level 03 — Jetpack Compose UI (27 lessons)

No factual issues found. Compose mental model, modifiers/constraint order, layout primitives, Material 3 & Scaffold slots, text/input/resources, state (`remember`/`rememberSaveable`/ViewModel), state hoisting/UDF, recomposition & snapshots, lazy lists, Navigation Compose (typed `@Serializable` routes, `toRoute`), effects (`LaunchedEffect`/`DisposableEffect`/`SideEffect`/`rememberUpdatedState`/`rememberCoroutineScope`), `derivedStateOf`/`snapshotFlow`, `collectAsStateWithLifecycle`, stability/skipping, custom `Layout` (single measurement pass), Canvas, gestures/pointerInput, animation & nested scroll, design systems/CompositionLocal, accessibility semantics, focus/keyboard, localisation/RTL/theming, edge-to-edge insets, adaptive layouts & Navigation 3, and Compose testing are all accurate and use current APIs (e.g. `Icons.AutoMirrored`, `enableEdgeToEdge`, `NavigableListDetailPaneScaffold`).

---

## Level 04 — Coroutines, Flow & Data Layer (31 lessons)

No factual issues found. Coroutines (suspend vs blocking, dispatchers/main-safety, structured concurrency, cancellation/`CancellationException`, timeouts, supervision, thread-safety, virtual-time testing with `runTest`/`StandardTestDispatcher`), Flow (cold/context-preservation/`flowOn`, operators, `catch` positioning, `retryWhen`, combine/zip/flatMap*, buffer/conflate, StateFlow/SharedFlow, `stateIn`/`shareIn`/`SharingStarted`), networking (HTTP semantics, kotlinx.serialization DTOs, Retrofit, OkHttp interceptors, MockWebServer, retry/idempotency, OAuth PKCE for public clients, single-flight token refresh, Paging 3, multipart, WebSocket/SSE, connectivity), and persistence (Room entities/DAOs/transactions/migrations/FTS, DataStore, source-of-truth/TTL, offline writes/outbox/conflict resolution, WorkManager `CoroutineWorker` `Result` semantics) are all accurate. The virtual-time retry example correctly pairs `advanceTimeBy` with `runCurrent` for tasks scheduled exactly at the boundary.

---

## Level 05 — App Architecture & DI (21 lessons)

**Technically accurate** (layers/dependency direction, immutable UI state/UDF, state holders, repositories/mappers, use cases/error models, MVVM/MVI/Clean trade-offs, manual DI/composition root, Hilt graphs/components/scopes — `SingletonComponent`/`ActivityRetainedComponent`/`ViewModelComponent`/`ActivityComponent`, `@Binds`/`@Provides`, qualifiers/multibindings/`@AssistedInject`, `@TestInstallIn`, Koin trade-offs, modularisation, api/impl split, dynamic features). No incorrect claims about the technologies.

But this level has a **content-generation / templating defect** that affects nearly all 21 lessons — worth raising even though it is not a factual error:

- **[MED]** **Broken "Architecture is not file naming" callouts.** In every lesson this callout ends with a dangling sentence fragment created by appending the lesson's trap-mistake text onto a generic sentence. Example (`level-05-week-19-01`): *"…adds value only when it protects a real contract. Putting Retrofit, Room, navigation, and formatting directly inside one ViewModel because it is convenient."* — the trailing clause has no predicate and reads as a broken run-on. Same pattern in `week-19-02` ("Representing loading, content, empty… that can contradict each other."), `week-20-01`, `week-21-01`, etc. Should be rewritten as a complete sentence (e.g. "Avoid putting Retrofit, Room… inside one ViewModel just because it is convenient.").
- **[MED]** **Generic, non-specific INSPECT sections and recall answers across the whole level.** Every lesson's INSPECT block reuses the same section code sample plus three identical generic walkthrough lines and the identical `expectedOutput` ("A small, explainable boundary whose responsibility, lifetime, dependency direction, and verification are explicit."). Likewise the three RECALL answers are identical in every lesson, so e.g. *"What problem should hilt components, scopes, and lifetimes solve?"* is "answered" with the generic *"A named product, ownership, change… rather than diagram conformity"* rather than anything about Hilt components/scopes. Also the `SCOUT` block and the "Production architecture is evaluated…" paragraph + FLOW are verbatim identical in all 21 lessons. This isn't wrong, but it substantially lowers the teaching value versus Levels 1–4, and the recall questions don't test the lesson's actual content.

---

## Level 06 — Platform Capabilities (24 lessons)

**Technically accurate** across notifications/channels, exact alarms vs WorkManager vs foreground services, Glance widgets, shortcuts/Sharesheet, Photo Picker/FileProvider/clipboard, App Links, camera intent vs CameraX (use cases, ImageAnalysis/backpressure/`ImageProxy.close()`), Media3/ExoPlayer, MediaSession/background playback, Picture-in-Picture, DownloadManager, location (approximate/precise, geofencing, background-location policy), Maps Compose, BLE/GATT (Nearby Devices permissions), NFC/NDEF, biometrics (`BiometricPrompt`, `BIOMETRIC_STRONG`/`DEVICE_CREDENTIAL`, Keystore key binding), Credential Manager/passkeys, WebView containment, in-app updates/reviews. API names and security guidance are correct.

- **[MED]** **Same templating defects as Level 05, level-wide.** The `SCOUT` block, the "Treat the platform boundary as an unreliable collaborator…" paragraph, the "Platform capability loop" FLOW, the "Capability review" table, the three INSPECT walkthrough lines, the INSPECT `expectedOutput`, and the three RECALL Q&A are verbatim-identical in all 24 lessons, and the "The system is part of the feature" callout again ends with the appended trap-mistake fragment (e.g. `week-23-01`: *"…is part of the feature: Creating a new high-importance channel to bypass the importance the user selected on an existing channel."*). Recall answers are generic and don't test the specific lesson. Same fix as Level 05.
- **[MED — verify]** `level-06-week-23-06-verified-app-links-deep-link-security` states: *"Android 15 dynamic rules can refine allowed paths where supported, but server rules cannot safely expand beyond the declared manifest scope."* I could not confirm that Android 15 provides a runtime/dynamic mechanism to refine App Links **path** rules (App Link path matching is declared in the manifest intent-filter; domain verification is via Digital Asset Links). Please verify this "Android 15 dynamic rules" claim against official docs; if no such feature exists it should be removed to avoid teaching a non-existent capability.

---

## Level 07 — Testing & Quality (24 lessons)

**Technically accurate** across test strategy/JUnit, fakes/mocks/stubs, parameterized & property-based testing (kotest `checkAll`), coroutine tests (`runTest`/`StandardTestDispatcher`/`advanceTimeBy`/`runCurrent`), Flow/StateFlow tests (`backgroundScope`, `WhileSubscribed` needs a collector), ViewModel state-sequence, repository contract & integration tests, Room DAO/migration tests (`MigrationTestHelper.createDatabase`/`runMigrationsAndValidate`), DataStore serializer/corruption tests, MockWebServer, Robolectric vs device fidelity, process-death vs recreation (`ActivityScenario.recreate`), Compose semantics tests, Espresso/IdlingResource, UI Automator, screenshot testing, accessibility checks, animation/flakiness/orchestration/sharding, Android lint/baselines/custom checks, Detekt/Ktlint, dependency analysis, Kover/JaCoCo/Sonar, and Kotlin ABI/binary-compatibility validation. API names and Gradle DSL snippets are correct.

- **[MED]** **Same level-wide templating defect as Levels 05–06.** Identical `SCOUT` block, the "Trustworthy tests control time, identity, data…" paragraph, the "Confidence feedback loop" FLOW, the "Evidence review" table, the three INSPECT walkthrough lines + `expectedOutput`, and the three RECALL Q&A appear verbatim in all 24 lessons; the "A passing check can still be weak" callout again ends with the appended trap-mistake fragment. Recall answers are generic and don't test each lesson's specific content. Same fix as Level 05.

---

## Level 08 — Security (24 lessons)

**Technically accurate and notably strong.** Threat modeling, MASVS, client/server trust, component/Intent/PendingIntent attack surface (`IntentSanitizer`), APK inspection/R8/mapping files, privacy surfaces, data classification, Android Keystore/StrongBox (`StrongBoxUnavailableException`, AES-GCM `ENCRYPTION_PADDING_NONE`), key lifecycle/rotation/attestation, Tink AEAD + associated data + envelope encryption, encrypted DB & SQL-injection safety (correctly notes EncryptedSharedPreferences/AndroidX Security-Crypto is deprecated and that Room doesn't auto-encrypt), biometric-protected keys/backup, TLS + Network Security Config, certificate/public-key pinning (SubjectPublicKeyInfo, backup pins, `CertificatePinner`) + rotation lab, secrets classification (Secrets Gradle Plugin ≠ confidentiality), client API-key restrictions (Play App Signing cert), app signing/upload-key reset, OAuth 2.0 + PKCE (public client), secure deep links/FileProvider, WebView origin/bridge security, Play Integrity request-hash binding + replay protection, and replay/fraud/idempotency. Security claims and the boundary between client and server authority are correct throughout.

- **[MED]** **Same level-wide templating defect as Levels 05–07.** Identical `SCOUT` block, the "Security controls must preserve the user promise…" paragraph, the "Threat-to-evidence path" FLOW, the "Security review record" table, the generic INSPECT walkthrough + `expectedOutput`, and a **second, identical trap** ("Treating an obfuscated or integrity-checked client decision as server authority") plus generic RECALL Q2/Q3 repeat verbatim in all 24 lessons; the "Security theatre to avoid" callout again ends with the appended trap-mistake fragment. Only the first trap and first recall question vary per lesson. Same fix as Level 05.

---

## Level 09 — Gradle & Build Engineering (30 lessons)

**Technically accurate.** Gradle lifecycle (init/config/execution), lazy task registration (`register` vs `create`, `TaskProvider`), Provider/Property API, task inputs/outputs/incrementality/`@CacheableTask`/`@PathSensitive`, Kotlin DSL, BuildService & Worker API isolation, Android source-set merge priority, build types/flavours/variant matrix + `beforeVariants`/`onVariants` Android Components API, variant-aware dependency resolution (`api` vs `implementation`, `dependencyInsight`), version catalogs/BOM/platform distinction (Compose BOM aligns, doesn't add libraries), dependency locking & verification, convention plugins, buildSrc vs included build, custom plugin/extension design, TestKit (`GradleRunner`/`TaskOutcome`), build scans/profiling, configuration cache vs build cache, local/remote cache correctness (`HttpBuildCache`, fork cache-poisoning), parallel execution/critical path, and KSP incremental (`Dependencies(aggregating=…)`, isolating vs aggregating). Commands and DSL are correct.

- **[MED]** **Same level-wide templating defect as Levels 05–08.** Identical `SCOUT`, the "Build logic is production infrastructure…" paragraph, "Build-engineering loop" FLOW, "Build review record" table, generic INSPECT walk + `expectedOutput`, and a second identical trap ("Optimising a synthetic clean build…") plus generic RECALL Q2/Q3 repeat verbatim in all 30 lessons; the "Common build trap" callout ends with the appended trap-mistake fragment. Same fix as Level 05.

---

## Level 10 — Performance & Reliability (24 lessons)

**Technically accurate.** Performance investigation methodology, Perfetto/system traces (CPU sampling vs instrumented method tracing, slice width ≠ CPU time), memory/leaks/bitmaps (decoded pixel memory), frame timing/jank (refresh-rate budget), Compose recomposition, battery/network (Doze, radio tail, WorkManager constraints), app size/AAB splits, cold/warm/hot startup + TTID/TTFD (`reportFullyDrawn`), startup critical path/providers, App Startup + lazy DI, Macrobenchmark (`measureRepeated`, `StartupTimingMetric`, `StartupMode`, `CompilationMode.None/Partial/Full`), Baseline vs Cloud vs Startup Profiles (`includeInStartupProfile`, DEX layout), ANR types + StrictMode, managed/native crash + `ApplicationExitInfo`/`getHistoricalProcessExitReasons`, OOM/LMK/`onTrimMemory`, retry storms/circuit breakers/jitter, and feature flags/kill switches. API names and platform behaviour are correct.

- **[MED]** **Same level-wide templating defect as Levels 05–09.** Identical `SCOUT`, the "Performance and reliability work is an evidence loop…" paragraph, "Evidence-driven engineering loop" FLOW, "Engineering review record" table, generic INSPECT walk + `expectedOutput`, a second identical trap ("Optimising or automating before defining the user-visible outcome…") and generic RECALL Q2/Q3 in all 24 lessons; the "Common trap" callout ends with the appended trap-mistake fragment. Same fix as Level 05.

---

## Level 11 — Android Internals (24 lessons)

**Technically accurate and expert-level** — the most fact-dense level, and it holds up. Linux processes/threads/UID vs PID/sandbox, Zygote fork + copy-on-write + USAP pools, system_server boot & framework services, Binder (Parcel, bounded transaction buffer, oneway ordering, `clearCallingIdentity` after authorization, `DeadObjectException`), ServiceManager/handles, ActivityManager/ActivityTaskManager/PackageManager/WindowManager responsibilities, launcher intent resolution vs execution, **`ActivityThread` is not a `Thread`** (correctly clarified), Instrumentation/attach-before-lifecycle, Compose phases/snapshot reads, Choreographer/VSync/FrameTimeline (60 Hz ≈ 16.7 ms, variable refresh), RenderThread/display lists/hardware layers, Surface/BufferQueue/Gralloc/fences, SurfaceFlinger/HWC device vs client composition, ART/DEX/JIT/AOT/profiles (`speed-profile`, class identity = name + defining loader), managed heap/GC/native memory (JNI global refs are GC roots), process importance/LMKD/`onTrimMemory`/`ApplicationExitInfo.REASON_LOW_MEMORY`, and Doze/App Standby/JobScheduler/wake locks/FGS. No incorrect claims found.

- **[MED]** **Same level-wide templating defect as Levels 05–10** (this level uses two template variants: weeks 44–46 "Build the causal model…"/"Platform internals are a causal map…"; weeks 47+ "Define the expert contract…"/"Internals knowledge is useful…"). Within each variant the `SCOUT`, boilerplate paragraph, FLOW, evidence table, generic INSPECT walk + `expectedOutput`, second identical trap, and generic RECALL Q2/Q3 repeat verbatim; the warning callout ends with the appended trap-mistake fragment. Same fix as Level 05.

---

## Level 12 — Specialization Tracks (30 lessons)

**Technically accurate and current** across a very broad, fact-dense range: public SDK API/behavioral contracts, binary/source compatibility on Kotlin-JVM (`NoSuchMethodError`/`apiCheck`), AAR contents/consumer R8 rules/Maven publication, SDK init/process isolation, SDK testing/security/observability, library baseline profiles, ML Kit on-device, **LiteRT** (correct current name for TFLite) quantization/delegates/benchmarking, model delivery/multimodal/governance, JNI (`JNIEnv` thread affinity, local/global refs, `RegisterNatives`, CheckJNI, modified UTF-8), CMake/NDK/ABIs/Rust interop (allocator contracts, C ABI), native debugging (HWASan/GWP-ASan/MTE/UBSan, tombstones, build IDs), adaptive windows/foldables/ChromeOS, Wear OS (`ScalingLazyColumn`, complications/tiles, Health Services), Android Auto vs Automotive OS (Cars App Library templates), Android TV (D-pad focus/`focusRestorer`), Android XR (Jetpack XR `Subspace`/`SpatialPanel`, Home Space — correctly flagged preview/device-dependent), AOSP (`repo init`/`sync`, `envsetup.sh`/`lunch`/`m`), Soong/`Android.bp`/APEX/flags, framework service changes, SELinux (avc denials, `audit2allow` caution, four-element access decision), HAL/stable AIDL/VINTF/Treble (HIDL legacy, append-only frozen interfaces), CTS/VTS/Gerrit (`Change-Id`/patchsets), integrity/attestation/strong auth, payment SDKs/tokenization/tap-to-pay (PCI DSS scope, Google Pay signed/encrypted payloads, HCE ≠ certified acceptance), certificate lifecycle/multi-brand, audit logging/regulated release evidence, and the portfolio/final capstones. No incorrect claims found.

- **[MED]** **Same level-wide templating defect as Levels 05–11** (uses the "Define the expert contract…"/"Expert engineering makes assumptions…" variant, identical `Expert evidence loop` FLOW, `Expert review record` table, generic INSPECT walk + `expectedOutput`, second identical trap "Treating one successful device…", and generic RECALL Q2/Q3 across all 30 lessons; the warning callout ends with the appended trap-mistake fragment). Same fix as Level 05.

---

## Verification note

Levels 1–4 were read in full. Levels 5–12 were read in full for the domain-specific paragraphs, code, tables, callouts, INSPECT, traps and recall of every lesson (the repeated boilerplate blocks were confirmed identical and skimmed). No source/live-doc lookups were performed for the "verify" items flagged above; those are the two claims I could not confirm from knowledge alone (`0..<` isn't one — it's the App Links "Android 15 dynamic rules" claim in Level 06).
