#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');
const { ROOT, CONTENT } = require('./lib/content');

const names = ['content-index.json', 'search-index.json', 'roadmap-graph.json', '../review.html'];
const generatedDirectory = path.join(CONTENT, 'generated');
const before = new Map(names.map((name) => {
  const file = name === '../review.html' ? path.join(ROOT, 'review.html') : path.join(generatedDirectory, name);
  return [name, fs.existsSync(file) ? fs.readFileSync(file, 'utf8') : null];
}));

for (const script of ['generate-index.js', 'generate-search-index.js', 'generate-roadmap-graph.js', 'generate-review-page.js']) {
  execFileSync(process.execPath, [path.join(ROOT, 'scripts', script)], { cwd: ROOT, stdio: 'inherit' });
}

const changed = names.filter((name) => {
  const file = name === '../review.html' ? path.join(ROOT, 'review.html') : path.join(generatedDirectory, name);
  const after = fs.existsSync(file) ? fs.readFileSync(file, 'utf8') : null;
  return before.get(name) !== after;
});

if (changed.length) {
  console.error(`Generated content was stale: ${changed.join(', ')}. Updated files are ready to review.`);
  process.exit(1);
}

console.log('Generated content is deterministic and up to date.');
