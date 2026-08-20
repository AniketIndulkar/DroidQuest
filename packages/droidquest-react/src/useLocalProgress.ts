"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  addUnique,
  emptyProgress,
  mergeProgressSnapshot,
  type LearnerProgress,
  type ReviewState,
} from "@droidquest/domain/progress";
import type { ProgressStore } from "@droidquest/domain/ports/progress-store";

const STORAGE_KEY = "droidquest.learner-progress.v1";

export const browserProgressStore: ProgressStore = {
  async load() {
    const saved = window.localStorage.getItem(STORAGE_KEY);
    return saved ? (JSON.parse(saved) as Partial<LearnerProgress>) : undefined;
  },
  async save(progress) {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(progress));
  },
};

export function useLocalProgress(store: ProgressStore = browserProgressStore) {
  const [progress, setProgress] = useState<LearnerProgress>(emptyProgress);
  const progressRef = useRef(progress);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let active = true;
    async function restore() {
      try {
        const saved = await store.load();
        if (saved && active) {
          const merged = mergeProgressSnapshot(saved);
          progressRef.current = merged;
          setProgress(merged);
        }
      } catch {
        // A malformed or unavailable local snapshot should never prevent learning.
      } finally {
        if (active) setReady(true);
      }
    }
    void restore();
    return () => {
      active = false;
    };
  }, [store]);

  const commit = useCallback((update: (current: LearnerProgress) => LearnerProgress) => {
    const next = update(progressRef.current);
    progressRef.current = next;
    setProgress(next);
    void store.save(next).catch(() => {
      // Guest and privacy-restricted sessions may block browser storage. Keep the
      // in-memory snapshot usable so persistence can never block navigation.
    });
    return next;
  }, [store]);

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
        commit((current) => ({ ...current, readNodeIds: addUnique(current.readNodeIds, nodeId) }));
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
            passedQuizIds: passed ? addUnique(current.passedQuizIds, quizId) : current.passedQuizIds,
            completedNodeIds:
              firstPass && nodeId
                ? addUnique(current.completedNodeIds, nodeId)
                : current.completedNodeIds,
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
