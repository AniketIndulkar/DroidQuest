"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";

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

const STORAGE_KEY = "droidquest.learner-progress.v1";

function add(values: string[], value: string) {
  return values.includes(value) ? values : [...values, value];
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

export function useLocalProgress() {
  const [progress, setProgress] = useState<LearnerProgress>(emptyProgress);
  const progressRef = useRef(progress);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    try {
      const saved = window.localStorage.getItem(STORAGE_KEY);
      if (saved) {
        const parsed = JSON.parse(saved) as Partial<LearnerProgress>;
        const merged = {
          ...emptyProgress,
          ...parsed,
          settings: { ...emptyProgress.settings, ...parsed.settings },
        };
        progressRef.current = merged;
        setProgress(merged);
      }
    } catch {
      // A malformed local snapshot should never prevent learning.
    }
    setReady(true);
  }, []);

  const commit = useCallback((update: (current: LearnerProgress) => LearnerProgress) => {
    const next = update(progressRef.current);
    progressRef.current = next;
    setProgress(next);
    try {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
    } catch {
      // Guest and privacy-restricted sessions may block browser storage. Keep the
      // in-memory snapshot usable so persistence can never block navigation.
    }
    return next;
  }, []);

  const api = useMemo(
    () => ({
      toggleStar(lessonId: string) {
        commit((current) => ({
          ...current,
          starredLessonIds: current.starredLessonIds.includes(lessonId)
            ? current.starredLessonIds.filter((id) => id !== lessonId)
            : [...current.starredLessonIds, lessonId],
        }));
      },
      markNodeRead(nodeId: string) {
        commit((current) => ({ ...current, readNodeIds: add(current.readNodeIds, nodeId) }));
      },
      recordQuiz(
        quizId: string,
        nodeId: string | undefined,
        score: number,
        passingScore: number,
        rewardXp: number,
        maxStars: number,
      ) {
        let firstPass = false;
        commit((current) => {
          const passed = score >= passingScore;
          firstPass = passed && !current.passedQuizIds.includes(quizId);
          const stars = passed
            ? score >= 1
              ? maxStars
              : Math.max(1, Math.min(maxStars, Math.ceil(score * maxStars)))
            : 0;
          return {
            ...current,
            quizAttempts: {
              ...current.quizAttempts,
              [quizId]: (current.quizAttempts[quizId] ?? 0) + 1,
            },
            bestQuizScore: {
              ...current.bestQuizScore,
              [quizId]: Math.max(score, current.bestQuizScore[quizId] ?? 0),
            },
            passedQuizIds: passed ? add(current.passedQuizIds, quizId) : current.passedQuizIds,
            completedNodeIds:
              firstPass && nodeId ? add(current.completedNodeIds, nodeId) : current.completedNodeIds,
            totalXp: current.totalXp + (firstPass ? rewardXp : 0),
            totalStars: current.totalStars + (firstPass ? stars : 0),
          };
        });
        return firstPass;
      },
      completeChallenge(challengeId: string, xp: number, stars: number) {
        commit((current) => {
          if (current.completedChallengeIds.includes(challengeId)) return current;
          return {
            ...current,
            completedChallengeIds: [...current.completedChallengeIds, challengeId],
            totalXp: current.totalXp + xp,
            totalStars: current.totalStars + stars,
          };
        });
      },
      saveReview(state: ReviewState) {
        commit((current) => ({
          ...current,
          reviewStates: { ...current.reviewStates, [state.recallItemId]: state },
        }));
      },
      updateSetting(name: "notifications" | "sound", value: boolean) {
        commit((current) => ({
          ...current,
          settings: { ...current.settings, [name]: value },
        }));
      },
    }),
    [commit],
  );

  return { progress, ready, ...api };
}
