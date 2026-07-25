#!/usr/bin/env node
'use strict';

const path = require('node:path');
const { CONTENT, loadAll, relative, sha256, writeJson } = require('./lib/content');

const all = loadAll();
const mapItem = ({ file, data }) => ({
  id: data.id,
  title: data.title,
  categoryId: data.categoryId,
  difficulty: data.difficulty,
  estimatedLearningMinutes: data.estimatedLearningMinutes,
  tags: data.tags,
  path: relative(file),
  sha256: sha256(file)
});

const index = {
  id: 'droidquest-content-index',
  curriculumVersion: all.curriculum.version,
  contentRevision: all.curriculum.contentRevision,
  counts: {
    categories: all.categories.length,
    lessons: all.lessons.length,
    quizzes: all.quizzes.length,
    challenges: all.challenges.length,
    badges: all.badges.length,
    glossaryEntries: all.glossaries.reduce((sum, item) => sum + item.data.entries.length, 0)
  },
  categories: all.categories.map(({ file, data }) => ({ id: data.id, title: data.title, order: data.order, lessonIds: data.lessonIds, path: relative(file), sha256: sha256(file) })),
  lessons: all.lessons.map(mapItem),
  quizzes: all.quizzes.map(({ file, data }) => ({ id: data.id, title: data.title, categoryId: data.categoryId, linkedLessonIds: data.linkedLessonIds, kind: data.kind, path: relative(file), sha256: sha256(file) })),
  challenges: all.challenges.map(({ file, data }) => ({ id: data.id, title: data.title, categoryId: data.categoryId, lessonId: data.lessonId, path: relative(file), sha256: sha256(file) })),
  badges: all.badges.map(({ file, data }) => ({ id: data.id, title: data.title, categoryId: data.categoryId, path: relative(file), sha256: sha256(file) })),
  roadmap: all.roadmaps.map(({ file, data }) => ({ id: data.id, title: data.title, path: relative(file), sha256: sha256(file) }))
};

writeJson(path.join(CONTENT, 'generated', 'content-index.json'), index);
console.log(`Generated content-index.json with ${index.lessons.length} lessons.`);
