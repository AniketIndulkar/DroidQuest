"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  loadChallenge,
  loadContent,
  loadLesson,
  loadQuiz,
  loadSearch,
} from "@/lib/content";
import {
  nextReviewState,
  type LearnerProgress,
  type ReviewRating,
  useLocalProgress,
} from "@/lib/progress";
import type {
  Category,
  Challenge,
  LearnBlock,
  Lesson,
  LoadedContent,
  Quiz,
  QuizQuestion,
  RoadmapNode,
  SearchDocument,
} from "@/lib/types";

type ViewName =
  | "home"
  | "map"
  | "category"
  | "lesson"
  | "quiz"
  | "challenge"
  | "search"
  | "starred"
  | "review"
  | "settings";

type View = { name: ViewName; id?: string };

const navItems: Array<{ name: ViewName; label: string; icon: string }> = [
  { name: "home", label: "Home", icon: "⌂" },
  { name: "map", label: "Quest map", icon: "◇" },
  { name: "search", label: "Search", icon: "⌕" },
  { name: "starred", label: "Starred", icon: "★" },
  { name: "settings", label: "Settings", icon: "⚙" },
];

function cleanTitle(title: string) {
  return title.replace(/^Level \d+:\s*/, "");
}

function progressForCategory(content: LoadedContent, progress: LearnerProgress, id: string) {
  const nodes = content.graph.nodes.filter(
    (node) => node.categoryId === id && node.type !== "level_preview",
  );
  const completed = nodes.filter((node) => progress.completedNodeIds.includes(node.id));
  return {
    completed: completed.length,
    total: nodes.length,
    percent: nodes.length ? Math.round((completed.length / nodes.length) * 100) : 0,
  };
}

function canStart(node: RoadmapNode, progress: LearnerProgress) {
  return (
    node.status !== "planned" &&
    node.type !== "level_preview" &&
    node.unlockPrerequisites.every((id) => progress.completedNodeIds.includes(id))
  );
}

function nextNode(content: LoadedContent, progress: LearnerProgress) {
  const byId = new Map(content.graph.nodes.map((node) => [node.id, node]));
  return content.graph.topologicalOrder
    .map((id) => byId.get(id))
    .find((node) => node && !progress.completedNodeIds.includes(node.id) && canStart(node, progress));
}

function nodeForLesson(content: LoadedContent, lessonId: string) {
  return content.graph.nodes.find((node) => node.lessonId === lessonId);
}

function nodeForQuiz(content: LoadedContent, quizId: string) {
  const direct = content.graph.nodes.find((node) => node.quizId === quizId);
  if (direct) return direct;
  const lessonIds = content.index.quizzes.find((item) => item.id === quizId)?.linkedLessonIds ?? [];
  return content.graph.nodes.find((node) => node.lessonId && lessonIds.includes(node.lessonId));
}

function dueReviewIds(progress: LearnerProgress) {
  const now = Date.now();
  return Object.values(progress.reviewStates)
    .filter((state) => state.dueAt <= now)
    .sort((a, b) => a.dueAt - b.dueAt)
    .map((state) => state.recallItemId);
}

export function DroidQuestApp() {
  const [content, setContent] = useState<LoadedContent>();
  const [error, setError] = useState("");
  const [view, setView] = useState<View>({ name: "home" });
  const [lesson, setLesson] = useState<Lesson>();
  const [quiz, setQuiz] = useState<Quiz>();
  const [challenge, setChallenge] = useState<Challenge>();
  const local = useLocalProgress();

  useEffect(() => {
    loadContent().then(setContent).catch((reason: Error) => setError(reason.message));
  }, []);

  const go = useCallback((name: ViewName, id?: string) => {
    setView({ name, id });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }, []);

  const openLesson = useCallback(
    async (id: string) => {
      if (!content) return;
      const loaded = await loadLesson(content.index, id);
      setLesson(loaded);
      const node = nodeForLesson(content, id);
      if (node) local.markNodeRead(node.id);
      go("lesson", id);
    },
    [content, go, local],
  );

  const openQuiz = useCallback(
    async (id: string) => {
      if (!content) return;
      setQuiz(await loadQuiz(content.index, id));
      go("quiz", id);
    },
    [content, go],
  );

  const openChallenge = useCallback(
    async (id: string) => {
      if (!content) return;
      setChallenge(await loadChallenge(content.index, id));
      go("challenge", id);
    },
    [content, go],
  );

  const openNode = useCallback(
    (node: RoadmapNode) => {
      if (node.lessonId) void openLesson(node.lessonId);
      else if (node.quizId) void openQuiz(node.quizId);
    },
    [openLesson, openQuiz],
  );

  const openSearchResult = useCallback(
    (result: SearchDocument) => {
      if (result.type === "lesson") void openLesson(result.id);
      else if (result.type === "quiz") void openQuiz(result.id);
      else if (result.type === "challenge") void openChallenge(result.id);
      else if (result.type === "category") go("category", result.categoryId ?? result.id);
      else if (result.lessonId) void openLesson(result.lessonId);
      else if (result.categoryId) go("category", result.categoryId);
    },
    [go, openChallenge, openLesson, openQuiz],
  );

  if (error) {
    return (
      <main className="center-state">
        <span className="state-icon">!</span>
        <h1>Content could not be loaded</h1>
        <p>{error}. Run the content sync and refresh this page.</p>
      </main>
    );
  }

  if (!content || !local.ready) {
    return (
      <main className="center-state loading-state">
        <span className="brand-mark">DQ</span>
        <p>Preparing your quest…</p>
      </main>
    );
  }

  const activeCategory = view.id
    ? content.categories.find((category) => category.id === view.id)
    : undefined;

  return (
    <div className="app-shell">
      <Sidebar
        active={view.name}
        progress={local.progress}
        onNavigate={(name) => go(name)}
      />
      <main className="main-panel">
        <Topbar
          content={content}
          view={view}
          category={activeCategory}
          onSearch={() => go("search")}
        />
        <div className="page-wrap">
          {view.name === "home" && (
            <HomeView
              content={content}
              progress={local.progress}
              onOpenNode={openNode}
              onCategory={(id) => go("category", id)}
              onReview={() => go("review")}
            />
          )}
          {view.name === "map" && (
            <MapView
              content={content}
              progress={local.progress}
              onCategory={(id) => go("category", id)}
            />
          )}
          {view.name === "category" && activeCategory && (
            <CategoryView
              content={content}
              category={activeCategory}
              progress={local.progress}
              onBack={() => go("map")}
              onOpenNode={openNode}
            />
          )}
          {view.name === "lesson" && lesson && (
            <LessonView
              lesson={lesson}
              category={content.categories.find((item) => item.id === lesson.categoryId)}
              progress={local.progress}
              onBack={() => go("category", lesson.categoryId)}
              onToggleStar={() => local.toggleStar(lesson.id)}
              onQuiz={() => void openQuiz(lesson.quizId)}
              onChallenge={() => void openChallenge(lesson.challengeId)}
              onReview={(id, rating) =>
                local.saveReview(
                  nextReviewState(
                    id,
                    local.progress.reviewStates[id],
                    rating,
                    lesson.revision.reviewIntervalsDays,
                  ),
                )
              }
            />
          )}
          {view.name === "quiz" && quiz && (
            <QuizView
              quiz={quiz}
              nodeId={nodeForQuiz(content, quiz.id)?.id}
              previousBest={local.progress.bestQuizScore[quiz.id] ?? 0}
              onBack={() => {
                const lessonId = quiz.linkedLessonIds[0];
                if (lessonId) void openLesson(lessonId);
                else go("map");
              }}
              onRecord={(score) =>
                local.recordQuiz(
                  quiz.id,
                  nodeForQuiz(content, quiz.id)?.id,
                  score,
                  quiz.passingScore,
                  quiz.rewards.xp,
                  quiz.rewards.stars,
                )
              }
            />
          )}
          {view.name === "challenge" && challenge && (
            <ChallengeView
              challenge={challenge}
              completed={local.progress.completedChallengeIds.includes(challenge.id)}
              onBack={() => void openLesson(challenge.lessonId)}
              onComplete={() =>
                local.completeChallenge(
                  challenge.id,
                  challenge.rewards.xp,
                  challenge.rewards.stars,
                )
              }
            />
          )}
          {view.name === "search" && (
            <SearchView onOpen={openSearchResult} />
          )}
          {view.name === "starred" && (
            <StarredView
              content={content}
              progress={local.progress}
              onLesson={(id) => void openLesson(id)}
            />
          )}
          {view.name === "review" && (
            <DailyReviewView
              content={content}
              progress={local.progress}
              onSave={local.saveReview}
              onDone={() => go("home")}
            />
          )}
          {view.name === "settings" && (
            <SettingsView progress={local.progress} onSetting={local.updateSetting} />
          )}
        </div>
      </main>
      <MobileNav active={view.name} onNavigate={(name) => go(name)} />
    </div>
  );
}

function Sidebar({
  active,
  progress,
  onNavigate,
}: {
  active: ViewName;
  progress: LearnerProgress;
  onNavigate: (name: ViewName) => void;
}) {
  return (
    <aside className="sidebar">
      <button className="brand" onClick={() => onNavigate("home")} aria-label="DroidQuest home">
        <span className="brand-mark">DQ</span>
        <span>
          <strong>DroidQuest</strong>
          <small>Android mastery</small>
        </span>
      </button>
      <nav aria-label="Primary navigation">
        {navItems.map((item) => (
          <button
            key={item.name}
            className={active === item.name ? "nav-item active" : "nav-item"}
            onClick={() => onNavigate(item.name)}
          >
            <span>{item.icon}</span>
            {item.label}
          </button>
        ))}
      </nav>
      <div className="sidebar-progress">
        <span className="eyebrow">Your progress</span>
        <strong>{progress.totalXp.toLocaleString()} XP</strong>
        <div className="mini-stats">
          <span>{progress.completedNodeIds.length} nodes</span>
          <span>{progress.totalStars} ★</span>
        </div>
        <p>Saved locally on this device</p>
      </div>
    </aside>
  );
}

function MobileNav({ active, onNavigate }: { active: ViewName; onNavigate: (name: ViewName) => void }) {
  return (
    <nav className="mobile-nav" aria-label="Mobile navigation">
      {navItems.map((item) => (
        <button
          key={item.name}
          className={active === item.name ? "active" : ""}
          onClick={() => onNavigate(item.name)}
          aria-label={item.label}
        >
          <span>{item.icon}</span>
          <small>{item.label}</small>
        </button>
      ))}
    </nav>
  );
}

function Topbar({
  content,
  view,
  category,
  onSearch,
}: {
  content: LoadedContent;
  view: View;
  category?: Category;
  onSearch: () => void;
}) {
  const title =
    view.name === "home"
      ? "Today’s quest"
      : view.name === "map"
        ? "Quest map"
        : view.name === "category"
          ? cleanTitle(category?.title ?? "Level")
          : view.name === "starred"
            ? "Starred lessons"
            : view.name === "settings"
              ? "Settings"
              : view.name === "search"
                ? "Search the curriculum"
                : "Learning session";
  return (
    <header className="topbar">
      <div>
        <span className="eyebrow">DroidQuest · v{content.curriculum.version}</span>
        <strong>{title}</strong>
      </div>
      <button className="search-trigger" onClick={onSearch} aria-label="Open search">
        <span>⌕</span>
        <span>Search 302 lessons</span>
        <kbd>⌘ K</kbd>
      </button>
      <span className="local-pill"><i /> Local mode</span>
    </header>
  );
}

function HomeView({
  content,
  progress,
  onOpenNode,
  onCategory,
  onReview,
}: {
  content: LoadedContent;
  progress: LearnerProgress;
  onOpenNode: (node: RoadmapNode) => void;
  onCategory: (id: string) => void;
  onReview: () => void;
}) {
  const next = nextNode(content, progress);
  const current = content.categories.find((category) => category.id === next?.categoryId) ?? content.categories[0];
  const currentProgress = progressForCategory(content, progress, current.id);
  const due = dueReviewIds(progress).length;
  return (
    <div className="page home-page">
      <section className="welcome-row">
        <div>
          <span className="eyebrow">CONTINUE YOUR JOURNEY</span>
          <h1>Build real Android understanding.</h1>
          <p>Short lessons, practical challenges, and active recall—from Kotlin foundations to platform internals.</p>
        </div>
        <div className="level-orbit" style={{ "--accent": current.theme.color } as React.CSSProperties}>
          <span>LV</span>
          <strong>{current.order}</strong>
          <small>{currentProgress.percent}%</small>
        </div>
      </section>

      {next && (
        <section className="resume-card" style={{ "--accent": current.theme.color } as React.CSSProperties}>
          <div className="resume-copy">
            <span className="eyebrow">UP NEXT · {next.type.replace("_", " ")}</span>
            <h2>{next.title}</h2>
            <p>{cleanTitle(current.title)} · {next.estimatedLearningMinutes || "Checkpoint"} {next.estimatedLearningMinutes ? "min" : ""}</p>
            <div className="reward-row"><span>+{next.rewards.xp} XP</span><span>+{next.rewards.stars} ★</span></div>
          </div>
          <button className="primary-button" onClick={() => onOpenNode(next)}>Continue quest <span>→</span></button>
        </section>
      )}

      <div className="stats-grid">
        <StatCard icon="✓" value={progress.completedNodeIds.length} label="Nodes completed" tone="green" />
        <StatCard icon="◆" value={progress.totalXp.toLocaleString()} label="Experience earned" tone="violet" />
        <StatCard icon="★" value={progress.totalStars} label="Stars collected" tone="amber" />
        <StatCard icon="↻" value={due} label="Reviews due" tone="blue" action={due ? onReview : undefined} />
      </div>

      <div className="section-heading">
        <div><span className="eyebrow">THE ROADMAP</span><h2>Explore every level</h2></div>
        <button className="text-button" onClick={() => onCategory(current.id)}>View current level →</button>
      </div>
      <div className="category-grid">
        {content.categories.map((category) => {
          const value = progressForCategory(content, progress, category.id);
          const unlocked = content.graph.nodes
            .filter((node) => node.categoryId === category.id)
            .some((node) => progress.completedNodeIds.includes(node.id) || canStart(node, progress));
          return (
            <button
              className={`category-card ${unlocked ? "" : "locked"}`}
              style={{ "--accent": category.theme.color } as React.CSSProperties}
              key={category.id}
              onClick={() => onCategory(category.id)}
            >
              <span className="category-number">{String(category.order).padStart(2, "0")}</span>
              <span className="category-glyph">{unlocked ? "◇" : "■"}</span>
              <strong>{cleanTitle(category.title)}</strong>
              <small>Weeks {category.weekRange.start}–{category.weekRange.end}</small>
              <ProgressBar value={value.percent} />
              <span className="category-meta">{unlocked ? `${value.completed}/${value.total} complete` : "Locked"}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}

function StatCard({ icon, value, label, tone, action }: { icon: string; value: string | number; label: string; tone: string; action?: () => void }) {
  const Component = action ? "button" : "div";
  return (
    <Component className={`stat-card ${tone}`} onClick={action}>
      <span className="stat-icon">{icon}</span>
      <span><strong>{value}</strong><small>{label}</small></span>
      {action && <i>→</i>}
    </Component>
  );
}

function ProgressBar({ value }: { value: number }) {
  return <span className="progress-track"><i style={{ width: `${value}%` }} /></span>;
}

function MapView({ content, progress, onCategory }: { content: LoadedContent; progress: LearnerProgress; onCategory: (id: string) => void }) {
  const total = content.graph.nodes.filter((node) => node.type !== "level_preview").length;
  return (
    <div className="page">
      <section className="page-intro split-intro">
        <div><span className="eyebrow">52 WEEKS · 12 LEVELS</span><h1>Your path to platform expertise</h1><p>Each node unlocks when its prerequisites are complete. Challenges are optional; knowledge checks move the journey forward.</p></div>
        <div className="map-total"><strong>{progress.completedNodeIds.length}<small> / {total}</small></strong><span>nodes complete</span></div>
      </section>
      <div className="map-list">
        {content.categories.map((category, index) => {
          const value = progressForCategory(content, progress, category.id);
          const unlocked = content.graph.nodes.filter((node) => node.categoryId === category.id).some((node) => canStart(node, progress) || progress.completedNodeIds.includes(node.id));
          return (
            <div className="map-row" key={category.id}>
              <div className="map-rail"><span style={{ background: unlocked ? category.theme.color : undefined }}>{value.percent === 100 ? "✓" : category.order}</span>{index < content.categories.length - 1 && <i />}</div>
              <button className={`map-card ${unlocked ? "" : "locked"}`} onClick={() => onCategory(category.id)} style={{ "--accent": category.theme.color } as React.CSSProperties}>
                <div><span className="eyebrow">LEVEL {category.order} · WEEKS {category.weekRange.start}–{category.weekRange.end}</span><h2>{cleanTitle(category.title)}</h2><p>{category.description}</p></div>
                <div className="map-card-progress"><strong>{value.percent}%</strong><ProgressBar value={value.percent} /><small>{unlocked ? `${value.completed} of ${value.total} complete` : "Complete the previous level"}</small></div>
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function CategoryView({ content, category, progress, onBack, onOpenNode }: { content: LoadedContent; category: Category; progress: LearnerProgress; onBack: () => void; onOpenNode: (node: RoadmapNode) => void }) {
  const value = progressForCategory(content, progress, category.id);
  const byId = new Map(content.graph.nodes.map((node) => [node.id, node]));
  const nodes = content.graph.topologicalOrder.map((id) => byId.get(id)).filter((node): node is RoadmapNode => Boolean(node && node.categoryId === category.id));
  return (
    <div className="page">
      <button className="back-button" onClick={onBack}>← Quest map</button>
      <section className="category-hero" style={{ "--accent": category.theme.color } as React.CSSProperties}>
        <span className="hero-index">{String(category.order).padStart(2, "0")}</span>
        <div><span className="eyebrow">LEVEL {category.order} · WEEKS {category.weekRange.start}–{category.weekRange.end}</span><h1>{cleanTitle(category.title)}</h1><p>{category.description}</p></div>
        <div className="hero-progress"><strong>{value.percent}%</strong><span>complete</span><ProgressBar value={value.percent} /></div>
      </section>
      <div className="node-list">
        {nodes.map((node, index) => {
          const completed = progress.completedNodeIds.includes(node.id);
          const available = canStart(node, progress);
          return (
            <div className="node-row" key={node.id}>
              <div className="node-rail"><span className={completed ? "complete" : available ? "available" : "locked"}>{completed ? "✓" : available ? "▸" : "■"}</span>{index < nodes.length - 1 && <i />}</div>
              <button className={`node-card ${completed ? "complete" : available ? "available" : "locked"}`} disabled={!completed && !available} onClick={() => onOpenNode(node)}>
                <div><span className="node-kind">{node.type === "boss" ? "LEVEL BOSS" : node.type === "checkpoint" ? "CHECKPOINT" : `LESSON · ${node.estimatedLearningMinutes} MIN`}</span><strong>{node.title}</strong><small>{completed ? "Completed" : available ? "Ready to begin" : "Complete the previous node"}</small></div>
                <div className="reward-row"><span>+{node.rewards.xp} XP</span><span>{node.rewards.stars} ★</span></div>
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function LessonView({ lesson, category, progress, onBack, onToggleStar, onQuiz, onChallenge, onReview }: { lesson: Lesson; category?: Category; progress: LearnerProgress; onBack: () => void; onToggleStar: () => void; onQuiz: () => void; onChallenge: () => void; onReview: (id: string, rating: ReviewRating) => void }) {
  const starred = progress.starredLessonIds.includes(lesson.id);
  return (
    <div className="page lesson-page">
      <div className="lesson-toolbar"><button className="back-button" onClick={onBack}>← Level {category?.order}</button><button className={`star-button ${starred ? "active" : ""}`} onClick={onToggleStar}>{starred ? "★ Starred" : "☆ Star lesson"}</button></div>
      <section className="lesson-hero" style={{ "--accent": category?.theme.color ?? "#63d998" } as React.CSSProperties}>
        <div><span className="eyebrow">WEEK {lesson.week} · {lesson.difficulty.toUpperCase()}</span><h1>{lesson.title}</h1><p>{lesson.revealStages.scout.purpose}</p><div className="tag-row">{lesson.tags.slice(0, 5).map((tag) => <span key={tag}>{tag}</span>)}</div></div>
        <div className="lesson-time"><strong>{lesson.estimatedLearningMinutes}</strong><span>minutes</span><small>focused learning</small></div>
      </section>
      <div className="lesson-layout">
        <article className="lesson-content">
          <SectionBadge number="01" label="Scout" />
          <ContentPanel title="Why this matters"><p>{lesson.revealStages.scout.realWorldUse}</p><div className="outcome-box"><strong>By the end</strong><p>{lesson.revealStages.scout.outcome}</p></div></ContentPanel>

          <SectionBadge number="02" label="Learn" />
          {lesson.revealStages.learn.sections.map((section) => (
            <section className="learning-section" key={section.id}><h2>{section.title}</h2>{section.blocks.map((block, index) => <LearnBlockView block={block} key={`${section.id}-${index}`} />)}</section>
          ))}

          <SectionBadge number="03" label="Inspect" />
          <ContentPanel title={lesson.revealStages.inspect.title}>
            <CodeBlock language={lesson.revealStages.inspect.language} code={lesson.revealStages.inspect.code} />
            <ol className="walkthrough">{lesson.revealStages.inspect.walkthrough.map((step) => <li key={step}>{step}</li>)}</ol>
            <div className="output-block"><span>EXPECTED OUTPUT</span><pre>{lesson.revealStages.inspect.expectedOutput}</pre></div>
          </ContentPanel>

          <SectionBadge number="04" label="Trap check" />
          <div className="trap-grid">{lesson.revealStages.trap_check.map((trap) => <div className="trap-card" key={trap.mistake}><strong>Watch out</strong><h3>{trap.mistake}</h3><p>{trap.why}</p><span>Fix: {trap.fix}</span></div>)}</div>

          <SectionBadge number="05" label="Challenge" />
          <ContentPanel title="Put it into practice"><p>{lesson.revealStages.challenge_intro.task}</p><div className="outcome-box"><strong>Success looks like</strong><p>{lesson.revealStages.challenge_intro.successLooksLike}</p></div><button className="secondary-button" onClick={onChallenge}>Open challenge →</button></ContentPanel>

          <SectionBadge number="06" label="Recall" />
          <p className="section-lede">Explain each idea from memory before revealing the model answer.</p>
          <div className="recall-list">{lesson.revealStages.recall.map((item) => <RecallCard key={item.id} item={item} state={progress.reviewStates[item.id]} onRate={(rating) => onReview(item.id, rating)} />)}</div>

          <section className="further-reading"><span className="eyebrow">GO DEEPER</span><h2>Further reading</h2>{lesson.revealStages.learn.furtherReading.map((resource) => <a key={resource.url} href={resource.url} target="_blank" rel="noreferrer"><div><strong>{resource.title}</strong><span>{resource.publisher} · {resource.resourceType.replace("_", " ")}</span><p>{resource.whyRead}</p></div><i>↗</i></a>)}</section>
          <button className="finish-lesson" onClick={onQuiz}><span><small>READY TO CHECK YOUR UNDERSTANDING?</small><strong>Start lesson quiz</strong></span><i>→</i></button>
        </article>
        <aside className="lesson-aside">
          <div className="aside-card"><span className="eyebrow">OBJECTIVES</span>{lesson.revision.objectives.map((objective) => <p key={objective}><i>✓</i>{objective}</p>)}</div>
          <div className="aside-card"><span className="eyebrow">MASTERY</span><strong>{Math.round(lesson.revision.masteryThreshold * 100)}% to pass</strong><p className="muted">Earn up to {lesson.revision.starsAvailable} stars and {lesson.revision.xp} XP.</p></div>
        </aside>
      </div>
    </div>
  );
}

function SectionBadge({ number, label }: { number: string; label: string }) {
  return <div className="section-badge"><span>{number}</span><strong>{label}</strong><i /></div>;
}

function ContentPanel({ title, children }: { title: string; children: React.ReactNode }) {
  return <section className="content-panel"><h2>{title}</h2>{children}</section>;
}

function LearnBlockView({ block }: { block: LearnBlock }) {
  if (block.type === "paragraph") return <p className="body-copy">{block.text}</p>;
  if (block.type === "code") return <CodeBlock language={block.language ?? "code"} code={block.code ?? ""} caption={block.caption} />;
  if (block.type === "callout") return <div className={`callout ${block.tone ?? "note"}`}><strong>{block.title ?? "Remember"}</strong><p>{block.text}</p></div>;
  if (block.type === "flow") return <div className="flow-block"><h3>{block.title}</h3><div>{block.steps?.map((step, index) => <span key={step.id}><i>{index + 1}</i><strong>{step.label}</strong><small>{step.detail}</small></span>)}</div></div>;
  if (block.type === "table") return <div className="table-wrap"><h3>{block.title}</h3><table><thead><tr>{block.columns?.map((column) => <th key={column}>{column}</th>)}</tr></thead><tbody>{block.rows?.map((row, rowIndex) => <tr key={rowIndex}>{row.map((cell, cellIndex) => <td key={`${rowIndex}-${cellIndex}`}>{cell}</td>)}</tr>)}</tbody></table></div>;
  if (block.type === "list") return <div className="list-block"><h3>{block.title}</h3><ul>{block.items?.map((item) => <li key={item}>{item}</li>)}</ul></div>;
  return null;
}

function CodeBlock({ language, code, caption }: { language: string; code: string; caption?: string }) {
  const [copied, setCopied] = useState(false);
  return <div className="code-wrap"><div className="code-head"><span>{language}</span><button onClick={() => { void navigator.clipboard.writeText(code); setCopied(true); window.setTimeout(() => setCopied(false), 1200); }}>{copied ? "Copied" : "Copy"}</button></div><pre><code>{code}</code></pre>{caption && <p>{caption}</p>}</div>;
}

function RecallCard({ item, state, onRate }: { item: { id: string; prompt: string; answer: string }; state?: LearnerProgress["reviewStates"][string]; onRate: (rating: ReviewRating) => void }) {
  const [answer, setAnswer] = useState("");
  const [revealed, setRevealed] = useState(false);
  return <div className="recall-card"><span className="eyebrow">RETRIEVE FROM MEMORY</span><h3>{item.prompt}</h3><textarea value={answer} onChange={(event) => setAnswer(event.target.value)} placeholder="Write a short explanation first…" disabled={revealed} />{revealed && <div className="model-answer"><strong>MODEL ANSWER</strong><p>{item.answer}</p></div>}{!revealed ? <button className="secondary-button" disabled={!answer.trim()} onClick={() => setRevealed(true)}>Compare answer</button> : <div className="rating-row"><small>How well did you remember?</small><div>{(["again", "hard", "good", "easy"] as ReviewRating[]).map((rating) => <button key={rating} onClick={() => { onRate(rating); setAnswer(""); setRevealed(false); }}>{rating}</button>)}</div></div>}{state && <span className="scheduled">✓ Scheduled · {state.lastRating}</span>}</div>;
}

function QuizView({ quiz, nodeId, previousBest, onBack, onRecord }: { quiz: Quiz; nodeId?: string; previousBest: number; onBack: () => void; onRecord: (score: number) => boolean }) {
  const [index, setIndex] = useState(0);
  const [answer, setAnswer] = useState<unknown>(undefined);
  const [checked, setChecked] = useState(false);
  const [isCorrect, setIsCorrect] = useState(false);
  const [correctCount, setCorrectCount] = useState(0);
  const [done, setDone] = useState(false);
  const [finalScore, setFinalScore] = useState(0);
  const question = quiz.questions[index];

  useEffect(() => {
    if (question.type === "order_steps" && Array.isArray(question.answer)) {
      setAnswer([...question.answer].reverse());
    }
  }, [question.id, question.answer, question.type]);

  const answerReady =
    answer !== undefined &&
    answer !== "" &&
    (!Array.isArray(answer) || answer.length > 0) &&
    (question.type !== "match_pairs" ||
      (typeof answer === "object" &&
        answer !== null &&
        typeof question.answer === "object" &&
        !Array.isArray(question.answer) &&
        Object.keys(question.answer).every((key) => Boolean((answer as Record<string, string>)[key]))));

  function check() {
    const result = evaluateAnswer(question, answer);
    setIsCorrect(result);
    setChecked(true);
  }

  function advance(correct = isCorrect) {
    const totalCorrect = correctCount + (correct ? 1 : 0);
    if (index === quiz.questions.length - 1) {
      const score = totalCorrect / quiz.questions.length;
      setCorrectCount(totalCorrect);
      setFinalScore(score);
      onRecord(score);
      setDone(true);
      return;
    }
    setCorrectCount(totalCorrect);
    setIndex(index + 1);
    setAnswer(undefined);
    setChecked(false);
    setIsCorrect(false);
  }

  if (done) {
    const passed = finalScore >= quiz.passingScore;
    return <div className="page quiz-page"><section className={`quiz-result ${passed ? "passed" : "retry"}`}><span className="result-icon">{passed ? "✓" : "↻"}</span><span className="eyebrow">{passed ? "QUEST COMPLETE" : "KEEP LEARNING"}</span><h1>{Math.round(finalScore * 100)}%</h1><h2>{passed ? "Knowledge check passed" : "Review and try again"}</h2><p>Your best score is {Math.round(Math.max(previousBest, finalScore) * 100)}%. {passed ? `You earned up to ${quiz.rewards.stars} stars.` : `You need ${Math.round(quiz.passingScore * 100)}% to progress.`}</p><div className="result-actions"><button className="secondary-button" onClick={onBack}>Back to lesson</button>{!passed && <button className="primary-button" onClick={() => { setIndex(0); setAnswer(undefined); setChecked(false); setCorrectCount(0); setDone(false); }}>Try again</button>}</div></section></div>;
  }

  const openEnded = question.type === "short_answer" || question.type === "spot_bug";
  return <div className="page quiz-page"><button className="back-button" onClick={onBack}>← Exit quiz</button><div className="quiz-shell"><header><div><span className="eyebrow">KNOWLEDGE CHECK</span><h1>{quiz.title}</h1></div><span>{index + 1} / {quiz.questions.length}</span></header><ProgressBar value={((index + (checked ? 1 : 0)) / quiz.questions.length) * 100} /><section className="question-card"><span className="question-type">{question.type.replace("_", " ")}</span><h2>{question.prompt}</h2><QuestionInput question={question} value={answer} onChange={setAnswer} disabled={checked} />{checked && <div className={`feedback ${isCorrect ? "correct" : "incorrect"}`}><strong>{isCorrect ? "Correct" : openEnded ? "Compare your answer" : "Not quite"}</strong><p><b>Accepted answer:</b> {formatAnswer(question.answer)}</p><p>{question.explanation}</p></div>}<footer>{!checked ? <button className="primary-button" disabled={!answerReady} onClick={() => openEnded ? setChecked(true) : check()}>{openEnded ? "Show model answer" : "Check answer"}</button> : openEnded ? <div className="self-assess"><button className="secondary-button" onClick={() => advance(false)}>Not yet</button><button className="primary-button" onClick={() => advance(true)}>I captured the idea</button></div> : <button className="primary-button" onClick={() => advance()}>{index === quiz.questions.length - 1 ? "Finish quiz" : "Next question"} →</button>}</footer></section></div><span className="sr-only">Node {nodeId ?? "none"}</span></div>;
}

function QuestionInput({ question, value, onChange, disabled }: { question: QuizQuestion; value: unknown; onChange: (value: unknown) => void; disabled: boolean }) {
  if (question.type === "single_choice") return <div className="option-list">{question.options?.map((option) => <button disabled={disabled} className={value === option ? "selected" : ""} onClick={() => onChange(option)} key={option}><i />{option}</button>)}</div>;
  if (question.type === "true_false") return <div className="option-list horizontal">{[true, false].map((option) => <button disabled={disabled} className={value === option ? "selected" : ""} onClick={() => onChange(option)} key={String(option)}><i />{option ? "True" : "False"}</button>)}</div>;
  if (question.type === "multiple_choice") {
    const selected = Array.isArray(value) ? value as string[] : [];
    return <div className="option-list">{question.options?.map((option) => <button disabled={disabled} className={selected.includes(option) ? "selected" : ""} onClick={() => onChange(selected.includes(option) ? selected.filter((item) => item !== option) : [...selected, option])} key={option}><i className="square" />{option}</button>)}</div>;
  }
  if (question.type === "order_steps") {
    const source = Array.isArray(question.answer) ? question.answer as string[] : [];
    const ordered = Array.isArray(value) ? value as string[] : [...source].reverse();
    return <div className="order-list">{ordered.map((item, position) => <div key={item}><span>{position + 1}</span><p>{item}</p><button disabled={disabled || position === 0} onClick={() => { const next = [...ordered]; [next[position - 1], next[position]] = [next[position], next[position - 1]]; onChange(next); }}>↑</button><button disabled={disabled || position === ordered.length - 1} onClick={() => { const next = [...ordered]; [next[position + 1], next[position]] = [next[position], next[position + 1]]; onChange(next); }}>↓</button></div>)}</div>;
  }
  if (question.type === "match_pairs" && typeof question.answer === "object" && !Array.isArray(question.answer)) {
    const pairs = question.answer as Record<string, string>;
    const selections = (value ?? {}) as Record<string, string>;
    const choices = [...new Set(Object.values(pairs))];
    return <div className="match-list">{Object.keys(pairs).map((key) => <label key={key}><span>{key.replace(/([A-Z])/g, " $1").replace(/^./, (letter) => letter.toUpperCase())}</span><select disabled={disabled} value={selections[key] ?? ""} onChange={(event) => onChange({ ...selections, [key]: event.target.value })}><option value="">Choose a match</option>{choices.map((choice) => <option key={choice}>{choice}</option>)}</select></label>)}</div>;
  }
  return <textarea className="answer-field" disabled={disabled} value={typeof value === "string" || typeof value === "number" ? String(value) : ""} onChange={(event) => onChange(event.target.value)} placeholder={question.type === "code_output" ? "Enter the exact output…" : "Write your answer…"} />;
}

function normalize(value: unknown) {
  return String(value).trim().toLowerCase().replace(/\r\n/g, "\n").replace(/[ \t]+/g, " ");
}

function evaluateAnswer(question: QuizQuestion, value: unknown) {
  const expected = question.answer;
  if (Array.isArray(expected)) {
    const actual = Array.isArray(value) ? value : [];
    if (question.type === "multiple_choice") return [...actual].map(normalize).sort().join("|") === [...expected].map(normalize).sort().join("|");
    return actual.map(normalize).join("|") === expected.map(normalize).join("|");
  }
  if (typeof expected === "object") {
    const actual = (value ?? {}) as Record<string, string>;
    return Object.entries(expected).every(([key, item]) => normalize(actual[key]) === normalize(item));
  }
  if (typeof expected === "boolean") return value === expected;
  return normalize(value) === normalize(expected);
}

function formatAnswer(answer: QuizQuestion["answer"]) {
  if (Array.isArray(answer)) return answer.join(" → ");
  if (typeof answer === "object") return Object.entries(answer).map(([left, right]) => `${left}: ${right}`).join(", ");
  return String(answer);
}

function ChallengeView({ challenge, completed, onBack, onComplete }: { challenge: Challenge; completed: boolean; onBack: () => void; onComplete: () => void }) {
  const [hintCount, setHintCount] = useState(0);
  const [solution, setSolution] = useState(false);
  return <div className="page challenge-page"><button className="back-button" onClick={onBack}>← Back to lesson</button><section className="challenge-hero"><span className="eyebrow">OPTIONAL PRACTICE · {challenge.estimatedMinutes} MIN</span><h1>{challenge.title}</h1><p>{challenge.prompt}</p><div className="reward-row"><span>+{challenge.rewards.xp} XP</span><span>+{challenge.rewards.stars} ★</span></div></section><div className="challenge-layout"><div><ContentPanel title="Success criteria"><ul className="check-list">{challenge.successCriteria.map((item) => <li key={item}>{item}</li>)}</ul></ContentPanel><ContentPanel title="Starter code"><CodeBlock language={challenge.starterCode.language} code={challenge.starterCode.code} /></ContentPanel><ContentPanel title="Need a nudge?"><div className="hint-list">{challenge.hints.slice(0, hintCount).map((hint, index) => <p key={hint}><span>{index + 1}</span>{hint}</p>)}</div>{hintCount < challenge.hints.length && <button className="secondary-button" onClick={() => setHintCount(hintCount + 1)}>Reveal hint {hintCount + 1}</button>}</ContentPanel><ContentPanel title="Solution outline"><button className="secondary-button" onClick={() => setSolution(!solution)}>{solution ? "Hide" : "Reveal"} solution outline</button>{solution && <ol className="walkthrough">{challenge.solutionOutline.map((item) => <li key={item}>{item}</li>)}</ol>}</ContentPanel></div><aside className="lesson-aside"><div className="aside-card"><span className="eyebrow">VERIFY</span>{challenge.verification.map((item) => <p key={item}><i>✓</i>{item}</p>)}</div>{completed ? <div className="completion-card">✓ Challenge complete</div> : <button className="primary-button wide" onClick={onComplete}>Mark complete</button>}</aside></div></div>;
}

function SearchView({ onOpen }: { onOpen: (result: SearchDocument) => void }) {
  const [query, setQuery] = useState("");
  const [documents, setDocuments] = useState<SearchDocument[]>([]);
  useEffect(() => { loadSearch().then((index) => setDocuments(index.documents)); }, []);
  const results = useMemo(() => {
    const terms = query.toLowerCase().trim().split(/\s+/).filter(Boolean);
    if (!terms.length) return documents.slice(0, 12);
    return documents.map((document) => {
      const title = document.title.toLowerCase();
      const haystack = `${title} ${document.tags.join(" ")} ${document.text}`.toLowerCase();
      const score = terms.reduce((total, term) => total + (title.includes(term) ? 5 : haystack.includes(term) ? 1 : -20), 0);
      return { document, score };
    }).filter((item) => item.score >= 0).sort((a, b) => b.score - a.score).slice(0, 40).map((item) => item.document);
  }, [documents, query]);
  return <div className="page search-page"><section className="page-intro"><span className="eyebrow">302 LESSONS · 313 GLOSSARY TERMS</span><h1>Find any Android concept</h1><p>Search lessons, quizzes, challenges, and definitions across the complete curriculum.</p></section><div className="big-search"><span>⌕</span><input autoFocus value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Try “coroutines”, “Binder”, or “offline sync”…"/><kbd>ESC</kbd></div><div className="search-summary">{query ? `${results.length} results for “${query}”` : "Suggested starting points"}</div><div className="search-results">{results.map((result) => <button key={`${result.type}-${result.id}`} onClick={() => onOpen(result)}><span className={`result-type ${result.type}`}>{result.type.slice(0, 2).toUpperCase()}</span><div><strong>{result.title}</strong><small>{result.type.replace("_", " ")} · {result.tags.slice(0, 3).join(" · ")}</small><p>{result.text.slice(0, 150)}…</p></div><i>→</i></button>)}</div></div>;
}

function StarredView({ content, progress, onLesson }: { content: LoadedContent; progress: LearnerProgress; onLesson: (id: string) => void }) {
  const lessons = progress.starredLessonIds.map((id) => content.index.lessons.find((item) => item.id === id)).filter(Boolean);
  return <div className="page"><section className="page-intro"><span className="eyebrow">YOUR LIBRARY</span><h1>Starred lessons</h1><p>Keep important ideas close for quick revision.</p></section>{lessons.length ? <div className="saved-grid">{lessons.map((item) => item && <button key={item.id} onClick={() => onLesson(item.id)}><span>★</span><strong>{item.title}</strong><small>{item.difficulty} · {item.estimatedLearningMinutes} min</small><p>{item.tags?.slice(0, 4).join(" · ")}</p></button>)}</div> : <div className="empty-card"><span>☆</span><h2>No starred lessons yet</h2><p>Use “Star lesson” on any lesson page to save it here.</p></div>}</div>;
}

function DailyReviewView({ content, progress, onSave, onDone }: { content: LoadedContent; progress: LearnerProgress; onSave: (state: LearnerProgress["reviewStates"][string]) => void; onDone: () => void }) {
  const ids = dueReviewIds(progress);
  const [index, setIndex] = useState(0);
  const [lesson, setLesson] = useState<Lesson>();
  const [answer, setAnswer] = useState("");
  const [revealed, setRevealed] = useState(false);
  const id = ids[index];
  useEffect(() => {
    if (!id) return;
    const lessonId = id.replace(/-recall-\d+$/, "");
    loadLesson(content.index, lessonId).then(setLesson);
  }, [content.index, id]);
  if (!ids.length || index >= ids.length) return <div className="page review-page"><section className="empty-card"><span>✓</span><h1>Memory strengthened</h1><p>You’re caught up for now. Come back when another idea is ready to revisit.</p><button className="primary-button" onClick={onDone}>Back home</button></section></div>;
  const item = lesson?.revealStages.recall.find((recall) => recall.id === id);
  if (!item || !lesson) return <div className="page center-state"><p>Preparing review…</p></div>;
  return <div className="page review-page"><div className="review-head"><button className="back-button" onClick={onDone}>← End session</button><span>{index + 1} of {ids.length}</span></div><ProgressBar value={(index / ids.length) * 100} /><section className="review-card"><span className="eyebrow">{lesson.title}</span><h1>{item.prompt}</h1><textarea value={answer} disabled={revealed} onChange={(event) => setAnswer(event.target.value)} placeholder="Explain it from memory first…" />{revealed && <div className="model-answer"><strong>MODEL ANSWER</strong><p>{item.answer}</p></div>}{!revealed ? <button className="primary-button" disabled={!answer.trim()} onClick={() => setRevealed(true)}>Compare answer</button> : <div className="review-ratings"><p>How well did you remember the idea?</p>{(["again", "hard", "good", "easy"] as ReviewRating[]).map((rating) => <button key={rating} onClick={() => { onSave(nextReviewState(id, progress.reviewStates[id], rating, lesson.revision.reviewIntervalsDays)); setIndex(index + 1); setAnswer(""); setRevealed(false); }}>{rating}<small>{rating === "again" ? "10 min" : rating === "hard" ? "1 day" : rating === "good" ? "Next interval" : "Skip ahead"}</small></button>)}</div>}</section></div>;
}

function SettingsView({ progress, onSetting }: { progress: LearnerProgress; onSetting: (name: "notifications" | "sound", value: boolean) => void }) {
  return <div className="page settings-page"><section className="page-intro"><span className="eyebrow">LOCAL-FIRST</span><h1>Settings</h1><p>Your curriculum and progress work without an account. Cloud sync will arrive in the next phase.</p></section><section className="settings-section"><h2>Learning</h2><div className="settings-card"><SettingRow label="Daily review reminders" detail="Show calm reminders when recall items are due." enabled={progress.settings.notifications} onChange={(value) => onSetting("notifications", value)} /><SettingRow label="Sound effects" detail="Play subtle feedback during quizzes and challenges." enabled={progress.settings.sound} onChange={(value) => onSetting("sound", value)} /></div></section><section className="settings-section"><h2>Progress storage</h2><div className="storage-card"><span className="storage-icon">⌂</span><div><strong>Saved on this device</strong><p>{progress.completedNodeIds.length} nodes, {progress.passedQuizIds.length} passed quizzes, and {Object.keys(progress.reviewStates).length} review schedules are stored locally in this browser.</p></div><span className="local-pill"><i /> Active</span></div><div className="coming-card"><span>↻</span><div><strong>Cross-device sync</strong><p>Supabase account sync will connect web, Android, and iOS without changing your local-first workflow.</p></div><small>COMING NEXT</small></div></section></div>;
}

function SettingRow({ label, detail, enabled, onChange }: { label: string; detail: string; enabled: boolean; onChange: (value: boolean) => void }) {
  return <label className="setting-row"><span><strong>{label}</strong><small>{detail}</small></span><input type="checkbox" checked={enabled} onChange={(event) => onChange(event.target.checked)} /><i /></label>;
}
