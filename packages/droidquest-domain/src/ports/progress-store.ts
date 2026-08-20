import type { LearnerProgress } from "../progress";

export interface ProgressStore {
  load(): Promise<Partial<LearnerProgress> | undefined>;
  save(progress: LearnerProgress): Promise<void>;
}
