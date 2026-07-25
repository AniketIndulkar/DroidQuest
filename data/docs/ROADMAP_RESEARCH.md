# Roadmap research and curriculum decisions

Research was reviewed on 25 July 2026. The supplied 52-week curriculum brief is the product direction; external sources are used to verify sequencing, terminology, and current Android recommendations. Lesson wording and examples are original.

## Primary references

- [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course) supports a zero-programming-experience entry, begins with Kotlin programs, variables, and functions, and later covers Compose, architecture, data, networking, testing, and adaptive layouts.
- [Android architecture recommendations](https://developer.android.com/topic/architecture/recommendations) informs the later UI, data, optional domain, repository, state-holder, and unidirectional-flow sequence.
- [Jetpack Compose course](https://developer.android.com/courses/jetpack-compose/course) informs the planned Compose progression from essentials through state, accessibility, testing, performance, and form factors.
- [Kotlin basic syntax](https://kotlinlang.org/docs/basic-syntax.html), [types](https://kotlinlang.org/docs/types-overview.html), [control flow](https://kotlinlang.org/docs/control-flow.html), [functions](https://kotlinlang.org/docs/functions.html), [null safety](https://kotlinlang.org/docs/null-safety.html), and [coding conventions](https://kotlinlang.org/docs/coding-conventions.html) are the primary Week 1 language references.
- Kotlin's official documentation for [classes](https://kotlinlang.org/docs/classes.html), [interfaces](https://kotlinlang.org/docs/interfaces.html), [data classes](https://kotlinlang.org/docs/data-classes.html), [sealed hierarchies](https://kotlinlang.org/docs/sealed-classes.html), [collections](https://kotlinlang.org/docs/collections-overview.html), [lambdas](https://kotlinlang.org/docs/lambdas.html), [extensions](https://kotlinlang.org/docs/extensions.html), and [scope functions](https://kotlinlang.org/docs/scope-functions.html) informs Week 2.
- Kotlin's references for [exceptions](https://kotlinlang.org/docs/exceptions.html), [generics](https://kotlinlang.org/docs/generics.html), [delegation](https://kotlinlang.org/docs/delegation.html), [inline functions](https://kotlinlang.org/docs/inline-functions.html), [value classes](https://kotlinlang.org/docs/inline-classes.html), [annotations](https://kotlinlang.org/docs/annotations.html), [reflection](https://kotlinlang.org/docs/reflection.html), and [Java interoperability](https://kotlinlang.org/docs/java-interop.html) inform Week 3.
- [Pro Git](https://git-scm.com/book/en/v2), [GitHub pull-request documentation](https://docs.github.com/en/pull-requests), [Android Studio debugging guidance](https://developer.android.com/studio/debug), [Android stack-trace guidance](https://developer.android.com/studio/debug/stacktraces), and [MDN's HTTP overview](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Overview) inform Week 4 engineering foundations.
- [Android roadmap on roadmap.sh](https://roadmap.sh/android) is used as secondary inspiration for breadth and dependency ordering, not as a source of lesson prose.

## Product decisions confirmed for DroidQuest

- Replace the former nine thematic regions with 12 curriculum levels spanning 52 weeks.
- Assume basic computer and arithmetic skills but no programming, Kotlin, Git, command-line, or HTTP experience.
- Limit each authored Learn stage to 10 minutes. Quiz, recall, and challenge duration are learner-dependent and displayed separately.
- Keep Scout as a brief motivation and transfer stage, never as a substitute for instruction.
- Make every Level 12 specialisation available. Learners may complete one, several, or all tracks without a forced choice.
- Author and review curriculum level by level. Level 1 was approved from its Week 1 sample before Weeks 2–4 were published.

## Week 1 sequence

Week 1 moves from execution to small-program design:

1. Program entry point, output, compiler feedback, and prediction.
2. Values, variables, types, immutability preference, and strings.
3. Expressions, Booleans, and conditional decisions.
4. Loops, ranges, boundaries, and accumulators.
5. Function contracts, parameters, returns, defaults, and decomposition.
6. Null safety, safe parsing, and explicit validation.
7. A small expense-entry pipeline and evidence-based debugging.

The sequence intentionally postpones Android UI. A learner first needs enough language fluency to understand the Kotlin used by Compose and the Android framework rather than memorising unexplained syntax.

## Weeks 2–4 sequence

Week 2 develops Kotlin modelling fluency through classes and composition, data/enum/sealed models, collection pipelines, and higher-order APIs. Week 3 introduces explicit failure modelling, generics and variance, delegation and compiler-specialised features, annotations/reflection/DSLs, and JVM interoperability and allocation. Week 4 adds the operating skills needed before Android projects become complex: terminal and process concepts, Git collaboration and documentation, evidence-based debugging, data structures and practical complexity, HTTP/REST/JSON boundaries, and an Expense Manager capstone whose domain can later be reused beneath Android UI.

This sequence follows the original curriculum's expert destination while keeping every Learn stage within the 10-minute reading budget. Larger synthesis work belongs in optional challenges and checkpoints rather than being disguised as a short lesson.

## Weeks 5–8 sequence

Level 2 follows the supplied platform-foundations scope while teaching modern defaults and legacy interoperability together:

1. Week 5 establishes Android Studio, SDK/emulator tooling, projects and source sets, Gradle/AGP build flow, manifests/resources, SDK-level contracts, variants, signing, APKs, and app bundles.
2. Week 6 separates application process, component, ViewModel, saved-state, and persistent lifetimes; it then covers Activity lifecycle, configuration recreation, process-death restoration, resource qualifiers, and localisation.
3. Week 7 teaches explicit and implicit intents, Activity Result contracts, contextual runtime permissions, component selection, services and foreground restrictions, broadcasts, providers, PendingIntent, tasks, Back, deep links, and verified App Links.
4. Week 8 provides the production legacy/hybrid foundation: XML View hierarchies and rendering, ConstraintLayout, View Binding, Fragment and view lifecycles, Fragment navigation, RecyclerView, View/Compose interoperability, and the lifecycle-safe Personal Notes capstone.

Primary references include [Android project structure](https://developer.android.com/studio/projects), [Gradle build overview](https://developer.android.com/build/gradle-build-overview), [Activity lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle), [saving UI state](https://developer.android.com/topic/libraries/architecture/saving-states), [runtime permissions](https://developer.android.com/training/permissions/requesting), [service guidance](https://developer.android.com/develop/background-work/services), [tasks and back stack](https://developer.android.com/guide/components/activities/tasks-and-back-stack), [App Links](https://developer.android.com/training/app-links), [Fragment lifecycle](https://developer.android.com/guide/fragments/lifecycle), [RecyclerView](https://developer.android.com/develop/ui/views/layout/recyclerview), and [Compose/View interoperability](https://developer.android.com/develop/ui/compose/migrate/interoperability-apis). These references were reviewed on 25 July 2026; lesson explanations and examples are original.

## Weeks 9–12 sequence

The first four weeks of Level 3 move from declarative rendering to production Compose mechanics:

1. Week 9 establishes the composition–layout–draw mental model, modifier ordering and constraints, core responsive layout relationships, semantic Material 3 components and slots, and controlled localised text/input.
2. Week 10 develops state lifetimes, state hoisting and unidirectional data flow, snapshot invalidation and stable identity, keyed lazy collections and paging boundaries, and typed navigation with destination-owned data.
3. Week 11 separates events from composition effects, then teaches coroutine effects and cancellation, symmetrical interop cleanup, lower-frequency derived observation, snapshot-to-Flow bridges, and lifecycle-aware ViewModel state collection.
4. Week 12 progresses into stability and measured performance, one-pass custom layout, accessible custom drawing, semantic and raw gesture handling, interruptible animation and nested-scroll negotiation, and focused design-system APIs.

Week 13 completes Level 3 with accessibility semantics and TalkBack, keyboard and scalable content, localisation and RTL, edge-to-edge insets, adaptive list-detail layouts, Navigation 3 concepts, Compose testing, screenshot evidence, and the Adaptive Shopping Application capstone.

The primary references for these weeks are the official Compose guides for [mental model](https://developer.android.com/develop/ui/compose/mental-model), [lifecycle and identity](https://developer.android.com/develop/ui/compose/lifecycle), [modifiers and constraints](https://developer.android.com/develop/ui/compose/modifiers), [state](https://developer.android.com/develop/ui/compose/state), [state hoisting](https://developer.android.com/develop/ui/compose/state-hoisting), [saving UI state](https://developer.android.com/develop/ui/compose/state-saving), [architecture](https://developer.android.com/develop/ui/compose/architecture), [side effects](https://developer.android.com/develop/ui/compose/side-effects), [navigation](https://developer.android.com/develop/ui/compose/navigation), [lists and grids](https://developer.android.com/develop/ui/compose/lists), [custom layouts](https://developer.android.com/develop/ui/compose/layouts/custom), [drawing](https://developer.android.com/develop/ui/compose/graphics/draw/overview), [gestures](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures), [animation](https://developer.android.com/develop/ui/compose/animation/introduction), [CompositionLocal](https://developer.android.com/develop/ui/compose/compositionlocal), [performance](https://developer.android.com/develop/ui/compose/performance), and [stability](https://developer.android.com/develop/ui/compose/performance/stability). Kotlin's official [coroutine cancellation guidance](https://kotlinlang.org/docs/cancellation-and-timeouts.html) informs the cancellation material. References were reviewed on 25 July 2026; all lesson prose, examples, questions, and challenges are original.

## Weeks 13–16 sequence

Week 13 closes the Compose level by turning its earlier mechanics into production UI:

1. Semantics, TalkBack, custom accessibility actions, and manual plus automated accessibility evidence.
2. Focus, keyboard and D-pad operation, scalable text, display size, and adaptive reflow.
3. Complete-message localisation, plural grammar, stable identity, RTL, dark theme, semantic colour, and dynamic-colour fallback.
4. Enforced edge-to-edge behaviour, changing system and IME insets, ownership, consumption, and system-bar contrast.
5. Window-based adaptive decisions, canonical list-detail panes, state continuity, and established Navigation Compose versus app-owned Navigation 3 back stacks.
6. Semantic UI tests, accessibility checks, controlled screenshot tests, and the Adaptive Shopping Application capstone.

Weeks 14–16 begin Level 4 in dependency order. Week 14 teaches suspend and main-safety before structured job ownership, cancellation, exception propagation, supervision, thread safety, and virtual-time testing. Week 15 then treats Flow as a stream abstraction: cold execution and context, transformation and retry, combining and latest replacement, backpressure, StateFlow/SharedFlow delivery contracts, sharing lifetimes, lifecycle-aware collection, and deterministic tests. Week 16 builds the remote-data boundary from protocol semantics through DTO serialization, Retrofit source and repository separation, OkHttp policy and redaction, typed failures, timeout and idempotent retry, and MockWebServer integration tests.

Current primary references include Android's official guidance for [Compose accessibility](https://developer.android.com/develop/ui/compose/accessibility), [semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics), [accessibility testing](https://developer.android.com/develop/ui/compose/accessibility/testing), [edge-to-edge](https://developer.android.com/develop/ui/compose/system/setup-e2e), [window insets](https://developer.android.com/develop/ui/compose/system/insets-ui), [adaptive applications](https://developer.android.com/develop/ui/compose/build-adaptive-apps), [list-detail layouts](https://developer.android.com/develop/adaptive-apps/guides/list-detail), [Navigation 3](https://developer.android.com/guide/navigation/navigation-3), [Compose testing](https://developer.android.com/develop/ui/compose/testing), [screenshot testing](https://developer.android.com/training/testing/ui-tests/screenshot), [Android coroutine best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices), [coroutine testing](https://developer.android.com/kotlin/coroutines/test), [Flow on Android](https://developer.android.com/kotlin/flow), [the Android data layer](https://developer.android.com/topic/architecture/data-layer), and [offline-first data guidance](https://developer.android.com/topic/architecture/data-layer/offline-first). Kotlin's official references for [coroutines](https://kotlinlang.org/docs/coroutines-basics.html), [cancellation](https://kotlinlang.org/docs/cancellation-and-timeouts.html), [exception handling](https://kotlinlang.org/docs/exception-handling.html), [shared mutable state](https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html), [Flow](https://kotlinlang.org/docs/coroutines-flow.html), and [JSON serialization](https://kotlinlang.org/docs/serialization-configure-json-serialization.html) provide language and library semantics. Retrofit, OkHttp, and MockWebServer behaviour is referenced from their official Square documentation. All lesson prose and assessment content is original.

## Weeks 17–18 sequence

Week 17 hardens remote delivery after the learner understands basic HTTP and clients:

1. OAuth access and refresh token purpose, public-client PKCE, credential lifetime, private storage boundaries, and logout.
2. Single-flight refresh under concurrent 401 responses, token generations, one bounded replay, rotation, and terminal session expiry.
3. Cursor-based Paging 3, PagingSource generations, refresh keys, ViewModel caching, and independent load states.
4. Streaming multipart uploads from content URIs, sampled progress, cancellation, stable idempotency, and durable transfer selection.
5. WebSocket, Server-Sent Events, callbackFlow ownership, heartbeat, reconnect, resume identity, deduplication, and snapshot reconciliation.
6. NetworkCapabilities as connectivity hints, metering policy, actual-request evidence, and coalesced restoration synchronization.

Week 18 then establishes a durable local authority and completes Level 4:

1. Room entities, keys, indices, DAOs, relationships, observable queries, and transaction boundaries.
2. Exported schemas, automatic and manual migrations, FTS, destructive-fallback policy, and real migration testing.
3. Preferences and typed DataStore, transactional updates, corruption policy, app files, cache files, and user-selected export.
4. Room as the single read source, ETag and TTL freshness, stale-but-usable state, deduplicated refresh, and protected local intent.
5. Optimistic writes backed by a durable outbox, stable operation IDs, at-least-once delivery, rejection, and domain-specific conflict resolution.
6. Unique WorkManager synchronization, constraints, scheduler backoff, cooperative stop, idempotent workers, and persistent-work testing.
7. The Offline-First News Reader capstone integrates all Level 4 boundaries and requires evidence for offline launch, process death, paging, token expiry, queued bookmarks, migration, retry, and eventual convergence.

The primary additions are Android's current official guidance for [Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview), [paging from network and Room](https://developer.android.com/topic/libraries/architecture/paging/v3-network-db), [Room](https://developer.android.com/training/data-storage/room), [Room relationships](https://developer.android.com/training/data-storage/room/relationships), [database migrations and testing](https://developer.android.com/training/data-storage/room/testing-db), [DataStore](https://developer.android.com/topic/libraries/architecture/datastore), [offline-first synchronization](https://developer.android.com/topic/architecture/data-layer/offline-first), [persistent work](https://developer.android.com/develop/background-work/background-tasks/persistent), [WorkRequest configuration](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work), and [unique work management](https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/manage-work). OAuth security follows the IETF's [OAuth 2.0 Security Best Current Practice](https://www.rfc-editor.org/rfc/rfc9700); transport-specific details use official OkHttp and Retrofit references. All teaching and assessment prose remains original.

## Weeks 19–22 sequence

Level 5 turns the completed data stack into an application structure that can evolve under production and team pressure:

1. Week 19 defines UI, data, and optional domain responsibilities; immutable UI state and UDF; state-holder lifetimes; durable events; repository and mapping contracts; focused use cases; typed failures; and proportionate use of MVVM, MVI, and Clean Architecture ideas.
2. Week 20 builds dependency injection manually before introducing Hilt and its Dagger graph. It covers composition roots, account and feature lifetimes, constructor injection, modules, generated components, scopes, qualifiers, multibindings, assisted factories, test replacement, compile-time diagnostics, and a bounded comparison with Koin.
3. Week 21 treats modularisation as an evidence-based decision. It covers cohesion and coupling, app, feature, data, core, API and implementation modules, acyclic graphs, navigation contracts, resource encapsulation, measured build effects, dynamic features, multi-module Hilt constraints, and SDK boundaries.
4. Week 22 completes the level with contract and architecture tests, purposeful fakes, decision records, incremental refactoring, fitness checks, the banking module graph, payment consistency and security boundaries, and the Multi-Module Banking Demo capstone.

The sequence deliberately teaches responsibilities and manual composition before frameworks and Gradle boundaries. This prevents learners from mistaking package names, interface count, Hilt annotations, or module count for architecture quality. Every structural choice must name the behaviour, ownership, risk, reuse, or measured build property it protects.

Primary references reviewed on 25 July 2026 are Android's official [architecture guide](https://developer.android.com/topic/architecture), [architecture recommendations](https://developer.android.com/topic/architecture/recommendations), [UI layer](https://developer.android.com/topic/architecture/ui-layer), [UI state production](https://developer.android.com/topic/architecture/ui-layer/state-production), [UI events](https://developer.android.com/topic/architecture/ui-layer/events), [data layer](https://developer.android.com/topic/architecture/data-layer), and [domain layer](https://developer.android.com/topic/architecture/domain-layer). Dependency injection follows the official [DI overview](https://developer.android.com/training/dependency-injection), [manual DI guide](https://developer.android.com/training/dependency-injection/manual), [Hilt guide](https://developer.android.com/training/dependency-injection/hilt-android), [Hilt testing guide](https://developer.android.com/training/dependency-injection/hilt-testing), and [multi-module Hilt guidance](https://developer.android.com/training/dependency-injection/hilt-multi-module). Modularisation and testing use the official [modularisation guide](https://developer.android.com/topic/modularization), [common patterns](https://developer.android.com/topic/modularization/patterns), [build optimisation guidance](https://developer.android.com/build/optimize-your-build), [test-double guide](https://developer.android.com/training/testing/fundamentals/test-doubles), and [what-to-test guidance](https://developer.android.com/training/testing/fundamentals/what-to-test). Dagger and Koin documentation are secondary references for framework-specific comparison. All lesson prose, examples, assessments, and challenges are original.
