import type { ProgressStore } from "@droidquest/domain/ports/progress-store";
import type { LearnerProgress } from "@droidquest/domain/progress";

const STORAGE_KEY = "droidquest.desktop.learner-progress.v1";

export const desktopProgressStore: ProgressStore = {
  async load() {
    const saved = window.localStorage.getItem(STORAGE_KEY);
    return saved ? (JSON.parse(saved) as Partial<LearnerProgress>) : undefined;
  },
  async save(progress) {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(progress));
  },
};
