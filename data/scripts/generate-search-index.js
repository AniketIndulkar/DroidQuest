#!/usr/bin/env node
'use strict';

const path = require('node:path');
const { CONTENT, loadAll, writeJson } = require('./lib/content');

const all = loadAll();
const categories = new Map(all.categories.map(({ data }) => [data.id, data]));
const normalize = (text) => text.toLowerCase().normalize('NFKD').replace(/[^a-z0-9+#.]+/g, ' ').trim();
const documents = [];

for (const { data } of all.categories) {
  documents.push({ id: data.id, type: 'category', title: data.title, categoryId: data.id, tags: data.tags, text: normalize(`${data.title} ${data.description} ${data.tags.join(' ')}`) });
}
for (const { data } of all.lessons) {
  const stageText = JSON.stringify(data.revealStages);
  const text = [data.title, stageText, ...data.tags].join(' ');
  documents.push({ id: data.id, type: 'lesson', title: data.title, categoryId: data.categoryId, categoryTitle: categories.get(data.categoryId).title, tags: data.tags, difficulty: data.difficulty, estimatedLearningMinutes: data.estimatedLearningMinutes, text: normalize(text) });
}
for (const { data } of all.quizzes) {
  documents.push({ id: data.id, type: 'quiz', title: data.title, categoryId: data.categoryId, linkedLessonIds: data.linkedLessonIds, tags: ['quiz', data.kind], text: normalize(`${data.title} ${data.questions.map((q) => q.prompt).join(' ')}`) });
}
for (const { data } of all.challenges) {
  documents.push({ id: data.id, type: 'challenge', title: data.title, categoryId: data.categoryId, lessonId: data.lessonId, tags: ['challenge', data.difficulty], text: normalize(`${data.title} ${data.prompt} ${data.successCriteria.join(' ')}`) });
}
for (const { data } of all.glossaries) {
  for (const entry of data.entries) documents.push({ id: entry.id, type: 'glossary', title: entry.term, tags: entry.tags, relatedLessonIds: entry.relatedLessonIds, text: normalize(`${entry.term} ${entry.definition} ${entry.tags.join(' ')}`) });
}

documents.sort((a, b) => a.id.localeCompare(b.id));
writeJson(path.join(CONTENT, 'generated', 'search-index.json'), {
  id: 'droidquest-search-index',
  curriculumVersion: all.curriculum.version,
  normalization: 'lowercase-nfkd-alphanumeric-plus-hash-dot',
  documents
});
console.log(`Generated search-index.json with ${documents.length} searchable documents.`);
