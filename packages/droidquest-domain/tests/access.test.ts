import assert from "node:assert/strict";
import test from "node:test";
import {
  canAccessRoadmapNode,
  canAccessSearchDocument,
  canStartRoadmapNode,
} from "../src/access.js";
import type { ContentIndex, RoadmapGraph, RoadmapNode, SearchDocument } from "../src/types.js";

const lockedNode: RoadmapNode = {
  id: "node-2",
  categoryId: "level-1",
  title: "Second lesson",
  type: "lesson",
  status: "available",
  lessonId: "lesson-2",
  difficulty: "beginner",
  estimatedLearningMinutes: 10,
  unlockPrerequisites: ["node-1"],
  rewards: { xp: 20, stars: 0 },
};

test("a roadmap node cannot start before its prerequisites are complete", () => {
  assert.equal(canStartRoadmapNode(lockedNode, []), false);
  assert.equal(canAccessRoadmapNode(lockedNode, []), false);
});

test("a roadmap node becomes accessible when its prerequisites are complete", () => {
  assert.equal(canStartRoadmapNode(lockedNode, ["node-1"]), true);
  assert.equal(canAccessRoadmapNode(lockedNode, ["node-1"]), true);
});

test("an already completed node remains available for revision", () => {
  assert.equal(canAccessRoadmapNode(lockedNode, ["node-2"]), true);
});

test("planned nodes cannot be opened even when their prerequisites are complete", () => {
  assert.equal(
    canAccessRoadmapNode({ ...lockedNode, status: "planned" }, ["node-1"]),
    false,
  );
});

const graph: RoadmapGraph = { nodes: [lockedNode], topologicalOrder: [lockedNode.id] };
const index: ContentIndex = {
  curriculumVersion: "test",
  contentRevision: 1,
  counts: {},
  categories: [],
  lessons: [],
  quizzes: [],
  challenges: [],
};
const searchDocument: SearchDocument = {
  id: "lesson-2",
  type: "lesson",
  title: "Second lesson",
  categoryId: "level-1",
  tags: [],
  text: "A lesson that has not unlocked yet.",
};

test("search cannot bypass a lesson's roadmap prerequisites", () => {
  assert.equal(canAccessSearchDocument(graph, index, searchDocument, []), false);
  assert.equal(canAccessSearchDocument(graph, index, searchDocument, ["node-1"]), true);
});
