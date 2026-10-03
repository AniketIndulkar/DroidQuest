import assert from "node:assert/strict";
import test from "node:test";
import {
  addUnique,
  emptyProgress,
  mergeProgressSnapshot,
  nextReviewState,
} from "../src/progress.js";

test("merges older progress snapshots with current defaults", () => {
  const result = mergeProgressSnapshot({
    completedNodeIds: ["node-1"],
    settings: { notifications: false, sound: true },
  });

  assert.deepEqual(result.completedNodeIds, ["node-1"]);
  assert.deepEqual(result.starredLessonIds, []);
  assert.equal(result.settings.notifications, false);
  assert.equal(result.settings.sound, true);
});

test("addUnique is idempotent", () => {
  const values = ["lesson-1"];
  assert.equal(addUnique(values, "lesson-1"), values);
  assert.deepEqual(addUnique(values, "lesson-2"), ["lesson-1", "lesson-2"]);
});

test("review scheduling uses authored intervals deterministically", () => {
  const now = Date.UTC(2026, 0, 1);
  const first = nextReviewState("recall-1", undefined, "good", [1, 7, 21], now);
  const second = nextReviewState("recall-1", first, "easy", [1, 7, 21], now);

  assert.equal(first.intervalDays, 1);
  assert.equal(first.dueAt, now + 86_400_000);
  assert.equal(second.intervalDays, 21);
  assert.equal(second.repetitions, 2);
  assert.equal(emptyProgress.totalXp, 0);
});

test("again schedules a short retry and records a lapse", () => {
  const now = Date.UTC(2026, 0, 1);
  const previous = nextReviewState("recall-1", undefined, "good", [1, 7, 21], now);
  const result = nextReviewState("recall-1", previous, "again", [1, 7, 21], now);

  assert.equal(result.intervalDays, 0);
  assert.equal(result.dueAt, now + 600_000);
  assert.equal(result.lapses, 1);
});
