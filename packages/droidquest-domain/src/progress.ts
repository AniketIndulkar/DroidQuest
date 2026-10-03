export type ReviewRating = "again" | "hard" | "good" | "easy";

export type ReviewState = {
  recallItemId: string;
  dueAt: number;
  intervalDays: number;
  repetitions: number;
  lapses: number;
  lastReviewedAt: number;
  lastRating: ReviewRating;
};

export type LearnerProgress = {
  completedNodeIds: string[];
  starredLessonIds: string[];
  completedChallengeIds: string[];
  passedQuizIds: string[];
  readNodeIds: string[];
  bestQuizScore: Record<string, number>;
  quizAttempts: Record<string, number>;
  reviewStates: Record<string, ReviewState>;
  totalXp: number;
  totalStars: number;
  settings: { notifications: boolean; sound: boolean };
};

export const emptyProgress: LearnerProgress = {
  completedNodeIds: [],
  starredLessonIds: [],
  completedChallengeIds: [],
  passedQuizIds: [],
  readNodeIds: [],
  bestQuizScore: {},
  quizAttempts: {},
  reviewStates: {},
  totalXp: 0,
  totalStars: 0,
  settings: { notifications: true, sound: true },
};

export function addUnique(values: string[], value: string): string[] {
  return values.includes(value) ? values : [...values, value];
}

export function mergeProgressSnapshot(snapshot: Partial<LearnerProgress>): LearnerProgress {
  return {
    ...emptyProgress,
    ...snapshot,
    settings: { ...emptyProgress.settings, ...snapshot.settings },
  };
}

function successfulInterval(current: number, values: number[], skip: number) {
  const next = values.findIndex((value) => value > current);
  if (next >= 0) return values[Math.min(next + skip, values.length - 1)];
  return Math.max(values.at(-1) ?? 1, Math.max(1, current) * (skip === 0 ? 2 : 3));
}

export function nextReviewState(
  recallItemId: string,
  previous: ReviewState | undefined,
  rating: ReviewRating,
  authored: number[],
  now = Date.now(),
): ReviewState {
  const intervals = [...new Set(authored.filter((value) => value > 0))].sort((a, b) => a - b);
  const safe = intervals.length ? intervals : [1, 7, 21];
  const current = previous?.intervalDays ?? 0;
  const interval =
    rating === "again"
      ? 0
      : rating === "hard"
        ? current <= 1
          ? 1
          : Math.max(1, Math.floor(current / 2))
        : successfulInterval(current, safe, rating === "easy" ? 1 : 0);
  const bounded = Math.min(interval, 365);
  return {
    recallItemId,
    dueAt: now + (rating === "again" ? 10 * 60_000 : bounded * 86_400_000),
    intervalDays: bounded,
    repetitions: (previous?.repetitions ?? 0) + (rating === "again" ? 0 : 1),
    lapses: (previous?.lapses ?? 0) + (rating === "again" && previous ? 1 : 0),
    lastReviewedAt: now,
    lastRating: rating,
  };
}
