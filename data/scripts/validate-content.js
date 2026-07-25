#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const Ajv = require('ajv');
const addFormats = require('ajv-formats');
const { ROOT, CONTENT, readJson, loadAll, relative } = require('./lib/content');

const errors = [];
const warn = (message) => errors.push(message);

function parseAllJson() {
  for (const root of [path.join(ROOT, 'schema'), CONTENT]) {
    const stack = [root];
    while (stack.length) {
      const current = stack.pop();
      for (const entry of fs.readdirSync(current, { withFileTypes: true })) {
        const file = path.join(current, entry.name);
        if (entry.isDirectory()) stack.push(file);
        else if (entry.name.endsWith('.json')) {
          try { readJson(file); } catch (error) { warn(`${relative(file)}: invalid JSON: ${error.message}`); }
        }
      }
    }
  }
}

function schemaValidation(collections) {
  const ajv = new Ajv({ allErrors: true, strict: true });
  addFormats(ajv);
  const mapping = {
    categories: 'category.schema.json', lessons: 'lesson.schema.json', quizzes: 'quiz.schema.json',
    challenges: 'challenge.schema.json', glossaries: 'glossary.schema.json', badges: 'badge.schema.json',
    roadmaps: 'roadmap.schema.json'
  };
  for (const [collection, schemaName] of Object.entries(mapping)) {
    const validate = ajv.compile(readJson(path.join(ROOT, 'schema', schemaName)));
    for (const { file, data } of collections[collection]) {
      if (!validate(data)) for (const issue of validate.errors) warn(`${relative(file)}${issue.instancePath || '/'} ${issue.message}`);
    }
  }
}

function uniqueValues(items, label, select = (item) => item.data.id) {
  const seen = new Map();
  for (const item of items) {
    const value = select(item);
    if (seen.has(value)) warn(`Duplicate ${label} '${value}' in ${relative(seen.get(value))} and ${relative(item.file)}`);
    else seen.set(value, item.file);
  }
}

function validateFilename(item) {
  const filename = path.basename(item.file, '.json');
  if (filename !== item.data.id) warn(`${relative(item.file)}: filename must match id '${item.data.id}.json'`);
}

function detectCycles(nodeIds, edges) {
  const adjacency = new Map([...nodeIds].map((id) => [id, []]));
  for (const { from, to } of edges) if (adjacency.has(from) && adjacency.has(to)) adjacency.get(from).push(to);
  const state = new Map();
  const stack = [];
  function visit(node) {
    if (state.get(node) === 1) {
      const start = stack.indexOf(node);
      warn(`Roadmap cycle detected: ${[...stack.slice(start), node].join(' -> ')}`);
      return;
    }
    if (state.get(node) === 2) return;
    state.set(node, 1); stack.push(node);
    for (const next of adjacency.get(node) || []) visit(next);
    stack.pop(); state.set(node, 2);
  }
  for (const node of nodeIds) visit(node);
}

function stringsIn(value) {
  if (typeof value === 'string') return [value];
  if (Array.isArray(value)) return value.flatMap(stringsIn);
  if (value && typeof value === 'object') return Object.entries(value)
    .filter(([key]) => !['type', 'language', 'id'].includes(key))
    .flatMap(([, child]) => stringsIn(child));
  return [];
}

function semanticValidation(all) {
  const types = ['categories', 'lessons', 'quizzes', 'challenges', 'glossaries', 'badges', 'roadmaps'];
  for (const type of types) {
    uniqueValues(all[type], `${type} id`);
    for (const item of all[type]) validateFilename(item);
  }

  const categories = new Map(all.categories.map((item) => [item.data.id, item.data]));
  const lessons = new Map(all.lessons.map((item) => [item.data.id, item.data]));
  const quizzes = new Map(all.quizzes.map((item) => [item.data.id, item.data]));
  const challenges = new Map(all.challenges.map((item) => [item.data.id, item.data]));

  if (categories.size !== 12) warn(`Expected 12 curriculum levels, found ${categories.size}`);
  uniqueValues(all.categories, 'category order', (item) => item.data.order);
  for (const category of categories.values()) {
    if (category.weekRange.start > category.weekRange.end) warn(`${category.id}: weekRange start must not exceed end`);
    if (category.status === 'planned' && category.lessonIds.length) warn(`${category.id}: planned level cannot publish lessonIds`);
    if (category.status !== 'planned' && !category.lessonIds.length) warn(`${category.id}: ${category.status} level must publish at least one lesson`);
    for (const prerequisite of category.unlockPrerequisites) if (!categories.has(prerequisite)) warn(`${category.id}: unknown category prerequisite '${prerequisite}'`);
    for (const lessonId of category.lessonIds) {
      const lesson = lessons.get(lessonId);
      if (!lesson) warn(`${category.id}: unknown lesson '${lessonId}'`);
      else if (lesson.categoryId !== category.id) warn(`${category.id}: lesson '${lessonId}' belongs to '${lesson.categoryId}'`);
    }
    if (category.checkpointQuizId && !quizzes.has(category.checkpointQuizId)) warn(`${category.id}: unknown checkpoint quiz '${category.checkpointQuizId}'`);
  }

  for (const lesson of lessons.values()) {
    const category = categories.get(lesson.categoryId);
    if (!category) warn(`${lesson.id}: unknown category '${lesson.categoryId}'`);
    else if (lesson.week < category.weekRange.start || lesson.week > category.weekRange.end) warn(`${lesson.id}: week ${lesson.week} falls outside ${category.id}'s week range`);
    for (const prerequisite of lesson.prerequisites) if (!lessons.has(prerequisite)) warn(`${lesson.id}: unknown lesson prerequisite '${prerequisite}'`);
    const stages = lesson.revealStages;
    for (const stage of ['scout', 'learn', 'inspect', 'trap_check', 'challenge_intro', 'recall']) if (!(stage in stages)) warn(`${lesson.id}: missing reveal stage '${stage}'`);
    if (stages.learn.estimatedMinutes !== lesson.estimatedLearningMinutes) warn(`${lesson.id}: learn estimate must match estimatedLearningMinutes`);
    const wordCount = stringsIn(stages.learn.sections).join(' ').trim().split(/\s+/).filter(Boolean).length;
    if (wordCount < 450) warn(`${lesson.id}: learn stage is too shallow (${wordCount} words; minimum 450)`);
    if (wordCount > 1600) warn(`${lesson.id}: learn stage may exceed 10 minutes (${wordCount} words; maximum 1600)`);
    const blocks = stages.learn.sections.flatMap((section) => section.blocks);
    if (!blocks.some((block) => block.type === 'code')) warn(`${lesson.id}: learn stage needs a code block`);
    if (!blocks.some((block) => ['flow', 'table'].includes(block.type))) warn(`${lesson.id}: learn stage needs a flow or table visual`);
    const readingUrls = stages.learn.furtherReading.map((resource) => resource.url);
    if (new Set(readingUrls).size !== readingUrls.length) warn(`${lesson.id}: further-reading URLs must be unique`);
    if (!stages.learn.furtherReading.some((resource) => ['official_docs', 'codelab'].includes(resource.resourceType))) warn(`${lesson.id}: further reading needs at least one official resource`);
    const quiz = quizzes.get(lesson.quizId);
    const challenge = challenges.get(lesson.challengeId);
    if (!quiz) warn(`${lesson.id}: unknown quiz '${lesson.quizId}'`);
    else if (quiz.kind !== 'lesson' || quiz.linkedLessonIds.length !== 1 || quiz.linkedLessonIds[0] !== lesson.id) warn(`${lesson.id}: quiz '${quiz.id}' must be a lesson quiz linked only to this lesson`);
    if (!challenge) warn(`${lesson.id}: unknown challenge '${lesson.challengeId}'`);
    else if (challenge.lessonId !== lesson.id) warn(`${lesson.id}: challenge '${challenge.id}' links back to '${challenge.lessonId}'`);
  }

  const questionItems = [];
  const usedQuestionTypes = new Set();
  for (const { file, data: quiz } of all.quizzes) {
    if (!categories.has(quiz.categoryId)) warn(`${quiz.id}: unknown category '${quiz.categoryId}'`);
    for (const lessonId of quiz.linkedLessonIds) if (!lessons.has(lessonId)) warn(`${quiz.id}: unknown linked lesson '${lessonId}'`);
    for (const question of quiz.questions) {
      questionItems.push({ file, data: question });
      usedQuestionTypes.add(question.type);
      if (!lessons.has(question.lessonId)) warn(`${quiz.id}/${question.id}: unknown linked lesson '${question.lessonId}'`);
      if (!quiz.linkedLessonIds.includes(question.lessonId)) warn(`${quiz.id}/${question.id}: question lesson is not listed in linkedLessonIds`);
      if (!question.explanation.trim()) warn(`${quiz.id}/${question.id}: explanation is required`);
    }
  }
  uniqueValues(questionItems, 'question id');
  for (const type of ['single_choice', 'multiple_choice', 'true_false', 'fill_blank', 'order_steps', 'match_pairs', 'code_output', 'spot_bug', 'short_answer']) {
    if (!usedQuestionTypes.has(type)) warn(`No quiz question exercises type '${type}'`);
  }

  for (const challenge of challenges.values()) {
    if (!categories.has(challenge.categoryId)) warn(`${challenge.id}: unknown category '${challenge.categoryId}'`);
    if (!lessons.has(challenge.lessonId)) warn(`${challenge.id}: unknown lesson '${challenge.lessonId}'`);
  }

  for (const glossary of all.glossaries.map((item) => item.data)) {
    const entryIds = new Set();
    for (const entry of glossary.entries) {
      if (entryIds.has(entry.id)) warn(`${glossary.id}: duplicate glossary entry '${entry.id}'`);
      entryIds.add(entry.id);
      if (!categories.has(entry.categoryId)) warn(`${glossary.id}/${entry.id}: unknown category '${entry.categoryId}'`);
      for (const lessonId of entry.relatedLessonIds) if (!lessons.has(lessonId)) warn(`${glossary.id}/${entry.id}: unknown lesson '${lessonId}'`);
    }
  }

  for (const badge of all.badges.map((item) => item.data)) {
    if (!categories.has(badge.categoryId)) warn(`${badge.id}: unknown category '${badge.categoryId}'`);
    if (!quizzes.has(badge.criteria.targetId) && !categories.has(badge.criteria.targetId)) warn(`${badge.id}: unknown criteria target '${badge.criteria.targetId}'`);
  }

  const allNodes = [];
  for (const roadmap of all.roadmaps.map((item) => item.data)) {
    const nodeIds = new Set(roadmap.nodes.map((node) => node.id));
    if (nodeIds.size !== roadmap.nodes.length) warn(`${roadmap.id}: duplicate roadmap node id`);
    for (const node of roadmap.nodes) {
      allNodes.push(node);
      if (!categories.has(node.categoryId)) warn(`${roadmap.id}/${node.id}: unknown category '${node.categoryId}'`);
      if (node.lessonId && !lessons.has(node.lessonId)) warn(`${roadmap.id}/${node.id}: unknown lesson '${node.lessonId}'`);
      if (node.quizId && !quizzes.has(node.quizId)) warn(`${roadmap.id}/${node.id}: unknown quiz '${node.quizId}'`);
      if (['start', 'lesson'].includes(node.type) && node.status === 'available' && !node.lessonId) warn(`${roadmap.id}/${node.id}: available lesson node needs lessonId`);
      if (['checkpoint', 'boss'].includes(node.type) && node.status === 'available' && !node.quizId) warn(`${roadmap.id}/${node.id}: available checkpoint node needs quizId`);
      if (node.type === 'level_preview' && node.status !== 'planned') warn(`${roadmap.id}/${node.id}: level preview must be planned`);
      for (const prerequisite of node.unlockPrerequisites) if (!nodeIds.has(prerequisite)) warn(`${roadmap.id}/${node.id}: unknown node prerequisite '${prerequisite}'`);
    }
    for (const edge of roadmap.edges) {
      if (!nodeIds.has(edge.from)) warn(`${roadmap.id}: edge source '${edge.from}' is not a node`);
      if (!nodeIds.has(edge.to)) warn(`${roadmap.id}: edge target '${edge.to}' is not a node`);
    }
    const prerequisiteEdges = roadmap.nodes.flatMap((node) => node.unlockPrerequisites.map((from) => ({ from, to: node.id })));
    detectCycles(nodeIds, [...roadmap.edges, ...prerequisiteEdges]);
  }
  const globalNodeIds = new Set(allNodes.map((node) => node.id));
  if (globalNodeIds.size !== allNodes.length) warn('Roadmap node IDs must be globally unique');
  for (const category of categories.values()) {
    const nodes = allNodes.filter((node) => node.categoryId === category.id);
    if (!nodes.length) warn(`${category.id}: roadmap needs an authored node or planned preview`);
    if (category.roadmapStartNodeId && !nodes.some((node) => node.id === category.roadmapStartNodeId)) warn(`${category.id}: roadmapStartNodeId is not in this level`);
    if (category.status !== 'planned' && !nodes.some((node) => node.type === 'start')) warn(`${category.id}: published level needs a start node`);
  }

  const manifest = all.curriculum;
  if (!manifest || manifest.id !== 'droidquest-curriculum') warn('content/curriculum.json must identify droidquest-curriculum');
  if (manifest.categoryIds?.length !== categories.size) warn('curriculum categoryIds must list every category');
  for (const categoryId of manifest.categoryIds || []) if (!categories.has(categoryId)) warn(`curriculum: unknown category '${categoryId}'`);
  for (const week of manifest.authoredWeeks || []) {
    for (const lessonId of week.lessonIds) if (!lessons.has(lessonId)) warn(`curriculum/${week.id}: unknown lesson '${lessonId}'`);
    if (!quizzes.has(week.checkpointQuizId)) warn(`curriculum/${week.id}: unknown checkpoint quiz '${week.checkpointQuizId}'`);
  }

  const generatedChecks = {
    'content-index.json': (value) => Array.isArray(value.lessons) && value.lessons.length === lessons.size,
    'search-index.json': (value) => Array.isArray(value.documents) && value.documents.length >= lessons.size,
    'roadmap-graph.json': (value) => Array.isArray(value.topologicalOrder) && value.topologicalOrder.length === allNodes.length
  };
  for (const [name, check] of Object.entries(generatedChecks)) {
    const file = path.join(CONTENT, 'generated', name);
    if (fs.existsSync(file)) {
      try { if (!check(readJson(file))) warn(`content/generated/${name}: generated structure or counts are invalid`); }
      catch (error) { warn(`content/generated/${name}: ${error.message}`); }
    }
  }
}

parseAllJson();
if (errors.length === 0) {
  const all = loadAll();
  schemaValidation(all);
  if (errors.length === 0) semanticValidation(all);
}

if (errors.length) {
  console.error(`Content validation failed with ${errors.length} issue(s):`);
  for (const issue of errors) console.error(`- ${issue}`);
  process.exit(1);
}

const all = loadAll();
const questionCount = all.quizzes.reduce((sum, item) => sum + item.data.questions.length, 0);
console.log(`Content valid: ${all.categories.length} levels, ${all.lessons.length} lessons, ${all.quizzes.length} quizzes (${questionCount} questions), ${all.challenges.length} challenges.`);
