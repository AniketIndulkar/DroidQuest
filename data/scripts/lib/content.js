'use strict';

const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');

const ROOT = path.resolve(__dirname, '..', '..');
const CONTENT = path.join(ROOT, 'content');

function jsonFiles(directory) {
  if (!fs.existsSync(directory)) return [];
  return fs.readdirSync(directory)
    .filter((name) => name.endsWith('.json'))
    .sort()
    .map((name) => path.join(directory, name));
}

function readJson(file) {
  return JSON.parse(fs.readFileSync(file, 'utf8'));
}

function loadCollection(name) {
  return jsonFiles(path.join(CONTENT, name)).map((file) => ({ file, data: readJson(file) }));
}

function relative(file) {
  return path.relative(ROOT, file).split(path.sep).join('/');
}

function stableSort(value) {
  if (Array.isArray(value)) return value.map(stableSort);
  if (value && typeof value === 'object') {
    return Object.fromEntries(Object.keys(value).sort().map((key) => [key, stableSort(value[key])]));
  }
  return value;
}

function writeJson(file, value) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, `${JSON.stringify(value, null, 2)}\n`);
}

function sha256(file) {
  return crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
}

function loadAll() {
  return {
    curriculum: readJson(path.join(CONTENT, 'curriculum.json')),
    categories: loadCollection('categories'),
    lessons: loadCollection('lessons'),
    quizzes: loadCollection('quizzes'),
    challenges: loadCollection('challenges'),
    glossaries: loadCollection('glossary'),
    badges: loadCollection('badges'),
    roadmaps: loadCollection('roadmap')
  };
}

module.exports = { ROOT, CONTENT, jsonFiles, readJson, loadCollection, loadAll, relative, stableSort, writeJson, sha256 };
