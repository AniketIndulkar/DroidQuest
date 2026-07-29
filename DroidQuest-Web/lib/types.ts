export type IndexEntry = {
  id: string;
  title: string;
  path: string;
  order?: number;
  categoryId?: string;
  lessonId?: string;
  lessonIds?: string[];
  linkedLessonIds?: string[];
  difficulty?: string;
  estimatedLearningMinutes?: number;
  tags?: string[];
  kind?: string;
};

export type ContentIndex = {
  curriculumVersion: string;
  contentRevision: number;
  counts: Record<string, number>;
  categories: IndexEntry[];
  lessons: IndexEntry[];
  quizzes: IndexEntry[];
  challenges: IndexEntry[];
};

export type Category = {
  id: string;
  title: string;
  description: string;
  order: number;
  status: "planned" | "in_progress" | "complete";
  weekRange: { start: number; end: number };
  theme: { color: string; icon: string; mapMood: string };
};

export type Curriculum = {
  title: string;
  description: string;
  version: string;
  categoryIds: string[];
  authoredWeeks: Array<{
    id: string;
    number: number;
    categoryId: string;
    title: string;
    status: string;
    lessonIds: string[];
    checkpointQuizId: string;
  }>;
};

export type Rewards = { xp: number; stars: number };

export type RoadmapNode = {
  id: string;
  categoryId: string;
  title: string;
  type: "start" | "lesson" | "checkpoint" | "boss" | "level_preview";
  status: "available" | "planned";
  lessonId?: string;
  quizId?: string;
  difficulty: string;
  estimatedLearningMinutes: number;
  unlockPrerequisites: string[];
  rewards: Rewards;
};

export type RoadmapGraph = {
  nodes: RoadmapNode[];
  topologicalOrder: string[];
};

export type LearnBlock = {
  type: "paragraph" | "code" | "callout" | "flow" | "table" | "list";
  text?: string;
  title?: string;
  tone?: string;
  language?: string;
  code?: string;
  caption?: string;
  steps?: Array<{ id: string; label: string; detail: string }>;
  columns?: string[];
  rows?: string[][];
  items?: string[];
};

export type Lesson = {
  id: string;
  title: string;
  categoryId: string;
  week: number;
  difficulty: string;
  tags: string[];
  estimatedLearningMinutes: number;
  quizId: string;
  challengeId: string;
  revealStages: {
    scout: { purpose: string; realWorldUse: string; outcome: string };
    learn: {
      estimatedMinutes: number;
      sections: Array<{ id: string; title: string; blocks: LearnBlock[] }>;
      furtherReading: Array<{
        title: string;
        publisher: string;
        resourceType: string;
        url: string;
        whyRead: string;
      }>;
    };
    inspect: {
      title: string;
      language: string;
      code: string;
      walkthrough: string[];
      expectedOutput: string;
    };
    trap_check: Array<{ mistake: string; why: string; fix: string }>;
    challenge_intro: { task: string; successLooksLike: string };
    recall: Array<{ id: string; prompt: string; answer: string }>;
  };
  revision: {
    objectives: string[];
    reviewIntervalsDays: number[];
    masteryThreshold: number;
    xp: number;
    starsAvailable: number;
  };
};

export type QuizQuestion = {
  id: string;
  prompt: string;
  type:
    | "single_choice"
    | "multiple_choice"
    | "true_false"
    | "fill_blank"
    | "order_steps"
    | "match_pairs"
    | "code_output"
    | "spot_bug"
    | "short_answer";
  options?: string[];
  answer: string | number | boolean | string[] | Record<string, string>;
  explanation: string;
  lessonId: string;
};

export type Quiz = {
  id: string;
  title: string;
  categoryId: string;
  linkedLessonIds: string[];
  kind: string;
  passingScore: number;
  questions: QuizQuestion[];
  rewards: Rewards;
};

export type Challenge = {
  id: string;
  title: string;
  categoryId: string;
  lessonId: string;
  difficulty: string;
  estimatedMinutes: number;
  prompt: string;
  successCriteria: string[];
  hints: string[];
  starterCode: { language: string; code: string };
  solutionOutline: string[];
  verification: string[];
  rewards: Rewards;
};

export type SearchDocument = {
  id: string;
  type: "lesson" | "quiz" | "challenge" | "category" | "glossary";
  title: string;
  categoryId?: string;
  lessonId?: string;
  tags: string[];
  text: string;
};

export type SearchIndex = { documents: SearchDocument[] };

export type LoadedContent = {
  index: ContentIndex;
  curriculum: Curriculum;
  graph: RoadmapGraph;
  categories: Category[];
};
