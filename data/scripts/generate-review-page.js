#!/usr/bin/env node
'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { ROOT, loadAll } = require('./lib/content');

const all = loadAll();
const categoryOrder = new Map(all.categories.map(({ data }) => [data.id, data.order]));
const byCategory = (a, b) => {
  const categoryDelta = (categoryOrder.get(a.data.categoryId) || 0) - (categoryOrder.get(b.data.categoryId) || 0);
  if (categoryDelta !== 0) return categoryDelta;
  return a.data.id.localeCompare(b.data.id);
};

const payload = {
  generatedAt: new Date(0).toISOString(),
  curriculum: all.curriculum,
  categories: all.categories.map(({ data }) => data).sort((a, b) => a.order - b.order),
  lessons: all.lessons.map(({ data }) => data).sort((a, b) => byCategory({ data: a }, { data: b })),
  quizzes: all.quizzes.map(({ data }) => data).sort((a, b) => byCategory({ data: a }, { data: b })),
  challenges: all.challenges.map(({ data }) => data).sort((a, b) => byCategory({ data: a }, { data: b })),
  badges: all.badges.map(({ data }) => data).sort((a, b) => a.categoryId.localeCompare(b.categoryId)),
  glossary: all.glossaries.flatMap(({ data }) => data.entries).sort((a, b) => a.term.localeCompare(b.term)),
  roadmap: all.roadmaps[0] ? all.roadmaps[0].data : { nodes: [], edges: [] }
};

function escapeScriptJson(value) {
  return value
    .replace(/</g, '\\u003c')
    .replace(/>/g, '\\u003e')
    .replace(/&/g, '\\u0026')
    .replace(/\u2028/g, '\\u2028')
    .replace(/\u2029/g, '\\u2029');
}

const html = `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>DroidQuest Content Review</title>
  <style>
    :root {
      color-scheme: light;
      --bg: #f6f7f9;
      --surface: #ffffff;
      --surface-2: #eef3f8;
      --ink: #17202a;
      --muted: #647184;
      --line: #d9e0ea;
      --accent: #147d64;
      --accent-2: #304ffe;
      --warn: #a65300;
      --shadow: 0 18px 55px rgba(23, 32, 42, 0.08);
      font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
    }

    * { box-sizing: border-box; }
    body {
      margin: 0;
      background: var(--bg);
      color: var(--ink);
      line-height: 1.5;
    }
    button, input, select {
      font: inherit;
    }
    button {
      border: 1px solid var(--line);
      background: var(--surface);
      color: var(--ink);
      border-radius: 6px;
      min-height: 38px;
      padding: 8px 12px;
      cursor: pointer;
    }
    button[aria-pressed="true"] {
      background: var(--ink);
      border-color: var(--ink);
      color: #fff;
    }
    input, select {
      border: 1px solid var(--line);
      border-radius: 6px;
      background: #fff;
      color: var(--ink);
      min-height: 40px;
      padding: 8px 10px;
      width: 100%;
    }
    code, pre {
      font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
    }
    pre {
      overflow: auto;
      background: #111827;
      color: #f8fafc;
      border-radius: 6px;
      padding: 14px;
      font-size: 13px;
    }
    .shell {
      max-width: 1480px;
      margin: 0 auto;
      padding: 24px;
    }
    .hero {
      display: grid;
      grid-template-columns: minmax(0, 1fr) auto;
      gap: 20px;
      align-items: end;
      margin-bottom: 20px;
    }
    .eyebrow {
      color: var(--accent);
      font-weight: 700;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      font-size: 12px;
    }
    h1 {
      margin: 6px 0 6px;
      font-size: clamp(30px, 4vw, 56px);
      line-height: 1;
      letter-spacing: 0;
    }
    .summary {
      color: var(--muted);
      max-width: 860px;
      margin: 0;
    }
    .toolbar {
      position: sticky;
      top: 0;
      z-index: 10;
      background: rgba(246, 247, 249, 0.94);
      backdrop-filter: blur(10px);
      border-bottom: 1px solid var(--line);
      margin: 0 -24px 20px;
      padding: 14px 24px;
      display: grid;
      grid-template-columns: minmax(220px, 1fr) minmax(160px, 220px) minmax(150px, 190px);
      gap: 12px;
    }
    .stats {
      display: grid;
      grid-template-columns: repeat(6, minmax(120px, 1fr));
      gap: 12px;
      margin-bottom: 20px;
    }
    .stat, .panel, .card {
      background: var(--surface);
      border: 1px solid var(--line);
      border-radius: 8px;
      box-shadow: var(--shadow);
    }
    .stat {
      padding: 14px;
    }
    .stat strong {
      display: block;
      font-size: 26px;
      line-height: 1.1;
    }
    .stat span {
      color: var(--muted);
      font-size: 13px;
    }
    .tabs {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 16px;
    }
    .view {
      display: none;
    }
    .view.active {
      display: block;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 14px;
    }
    .two-col {
      display: grid;
      grid-template-columns: minmax(300px, 420px) minmax(0, 1fr);
      gap: 16px;
      align-items: start;
    }
    .panel {
      padding: 16px;
    }
    .list {
      display: grid;
      gap: 10px;
      max-height: calc(100vh - 230px);
      overflow: auto;
      padding-right: 4px;
    }
    .card {
      padding: 14px;
    }
    .selectable {
      width: 100%;
      text-align: left;
    }
    .selectable.active {
      outline: 2px solid var(--accent);
      border-color: var(--accent);
    }
    .row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
    }
    .meta {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 10px;
    }
    .pill {
      display: inline-flex;
      align-items: center;
      border-radius: 999px;
      border: 1px solid var(--line);
      background: var(--surface-2);
      color: #344054;
      font-size: 12px;
      padding: 3px 8px;
      white-space: nowrap;
    }
    .pill.beginner { background: #e8f7ef; color: #17613d; border-color: #bce7cd; }
    .pill.intermediate { background: #eef2ff; color: #3040a0; border-color: #ccd6ff; }
    .pill.advanced { background: #fff3e6; color: #8a4500; border-color: #ffd2a1; }
    .pill.expert { background: #f5eafe; color: #6f2d8d; border-color: #dfc2f2; }
    .muted {
      color: var(--muted);
    }
    .title {
      margin: 0;
      font-size: 18px;
      line-height: 1.25;
    }
    .section-title {
      margin: 0 0 10px;
      font-size: 15px;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: var(--muted);
    }
    .stage {
      border-top: 1px solid var(--line);
      padding-top: 14px;
      margin-top: 14px;
    }
    .stage h3 {
      margin: 0 0 8px;
      font-size: 15px;
      text-transform: capitalize;
    }
    .learn-section {
      padding: 14px;
      margin: 12px 0;
      border: 1px solid var(--line);
      border-radius: 8px;
      background: #fbfcfe;
    }
    .learn-section h4 { margin: 0 0 10px; font-size: 17px; }
    .content-block { margin: 12px 0; }
    .content-block p { margin: 0; }
    .callout {
      padding: 12px;
      border-left: 4px solid var(--accent);
      background: #edf8f5;
      border-radius: 4px;
    }
    .callout.warning { border-left-color: var(--warn); background: #fff6e8; }
    .callout strong { display: block; margin-bottom: 4px; }
    .flow {
      display: flex;
      gap: 8px;
      overflow-x: auto;
      align-items: stretch;
      padding-bottom: 4px;
    }
    .flow-step {
      position: relative;
      min-width: 150px;
      flex: 1;
      border: 1px solid #bfcbe0;
      background: #f2f5ff;
      border-radius: 6px;
      padding: 10px;
    }
    .flow-step:not(:last-child)::after {
      content: '→';
      position: absolute;
      right: -10px;
      top: 50%;
      z-index: 2;
      background: var(--bg);
      color: var(--accent-2);
      font-weight: 800;
      padding: 0 2px;
    }
    .flow-step strong, .flow-step small { display: block; }
    .flow-step small { color: var(--muted); margin-top: 4px; }
    .content-table { width: 100%; border-collapse: collapse; font-size: 14px; }
    .content-table th, .content-table td { border: 1px solid var(--line); padding: 8px; text-align: left; vertical-align: top; }
    .content-table th { background: var(--surface-2); }
    .reading-list {
      margin-top: 18px;
      padding: 14px;
      border: 1px solid #bfd6cd;
      border-radius: 8px;
      background: #f2faf7;
    }
    .reading-list h4 { margin: 0 0 6px; }
    .reading-list a { color: #075e54; font-weight: 700; }
    .reading-list p { margin: 3px 0 10px; }
    details { margin: 8px 0; }
    details summary { cursor: pointer; font-weight: 600; }
    .plain-list {
      margin: 8px 0 0;
      padding-left: 20px;
    }
    .quiz-question {
      border-top: 1px solid var(--line);
      padding-top: 12px;
      margin-top: 12px;
    }
    .answers {
      background: #f8fafc;
      border: 1px solid var(--line);
      border-radius: 6px;
      padding: 10px;
      margin-top: 8px;
      white-space: pre-wrap;
    }
    .roadmap {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 10px;
    }
    .road-node {
      border-left: 5px solid var(--accent-2);
    }
    .road-node.boss {
      border-left-color: var(--warn);
    }
    .empty {
      padding: 30px;
      text-align: center;
      color: var(--muted);
      border: 1px dashed var(--line);
      border-radius: 8px;
      background: #fff;
    }
    @media (max-width: 1050px) {
      .stats, .grid, .roadmap { grid-template-columns: repeat(2, minmax(0, 1fr)); }
      .two-col { grid-template-columns: 1fr; }
      .list { max-height: none; }
    }
    @media (max-width: 700px) {
      .shell { padding: 16px; }
      .hero, .toolbar, .stats, .grid, .roadmap { grid-template-columns: 1fr; }
      .toolbar { margin-left: -16px; margin-right: -16px; padding-left: 16px; padding-right: 16px; }
      h1 { font-size: 34px; }
    }
  </style>
</head>
<body>
  <script id="dq-data" type="application/json">${escapeScriptJson(JSON.stringify(payload))}</script>
  <main class="shell">
    <section class="hero" aria-labelledby="page-title">
      <div>
        <div class="eyebrow">Local content review</div>
        <h1 id="page-title">DroidQuest Content Review</h1>
        <p class="summary" id="summary"></p>
      </div>
      <div class="meta">
        <span class="pill" id="version-pill"></span>
        <span class="pill">Standalone HTML</span>
      </div>
    </section>

    <section class="toolbar" aria-label="Review filters">
      <label>
        <span class="muted">Search</span>
        <input id="search" type="search" placeholder="Search lessons, quizzes, challenges, tags, glossary">
      </label>
      <label>
        <span class="muted">Level</span>
        <select id="category-filter"></select>
      </label>
      <label>
        <span class="muted">Difficulty</span>
        <select id="difficulty-filter"></select>
      </label>
    </section>

    <section class="stats" id="stats" aria-label="Content statistics"></section>

    <nav class="tabs" aria-label="Review sections">
      <button data-tab="overview" aria-pressed="true">Overview</button>
      <button data-tab="lessons" aria-pressed="false">Lessons</button>
      <button data-tab="quizzes" aria-pressed="false">Quizzes</button>
      <button data-tab="challenges" aria-pressed="false">Challenges</button>
      <button data-tab="roadmap" aria-pressed="false">Roadmap</button>
      <button data-tab="glossary" aria-pressed="false">Glossary</button>
    </nav>

    <section id="overview" class="view active"></section>
    <section id="lessons" class="view"></section>
    <section id="quizzes" class="view"></section>
    <section id="challenges" class="view"></section>
    <section id="roadmap" class="view"></section>
    <section id="glossary" class="view"></section>
  </main>

  <script>
    const data = JSON.parse(document.getElementById('dq-data').textContent);
    const state = { tab: 'overview', query: '', category: 'all', difficulty: 'all', selectedLessonId: null };
    const categoriesById = new Map(data.categories.map((item) => [item.id, item]));
    const lessonsById = new Map(data.lessons.map((item) => [item.id, item]));
    const quizzesById = new Map(data.quizzes.map((item) => [item.id, item]));
    const challengesById = new Map(data.challenges.map((item) => [item.id, item]));

    function text(value) {
      if (value === null || value === undefined) return '';
      if (Array.isArray(value)) return value.map(text).join(' ');
      if (typeof value === 'object') return JSON.stringify(value);
      return String(value);
    }

    function el(tag, attrs = {}, children = []) {
      const node = document.createElement(tag);
      Object.entries(attrs).forEach(([key, value]) => {
        if (key === 'className') node.className = value;
        else if (key === 'text') node.textContent = value;
        else if (key.startsWith('on') && typeof value === 'function') node.addEventListener(key.slice(2), value);
        else node.setAttribute(key, value);
      });
      for (const child of Array.isArray(children) ? children : [children]) {
        if (child === null || child === undefined) continue;
        node.append(child.nodeType ? child : document.createTextNode(String(child)));
      }
      return node;
    }

    function replace(id, node) {
      const target = document.getElementById(id);
      target.replaceChildren(node);
    }

    function pills(values) {
      return el('div', { className: 'meta' }, values.filter(Boolean).map((value) => {
        const label = text(value);
        const difficulty = ['beginner', 'intermediate', 'advanced', 'expert'].includes(label) ? ' ' + label : '';
        return el('span', { className: 'pill' + difficulty, text: label });
      }));
    }

    function searchable(value) {
      return text(value).toLowerCase();
    }

    function itemMatches(item) {
      const query = state.query.trim().toLowerCase();
      const categoryOk = state.category === 'all' || item.categoryId === state.category || item.id === state.category;
      const difficultyOk = state.difficulty === 'all' || item.difficulty === state.difficulty;
      if (!categoryOk || !difficultyOk) return false;
      if (!query) return true;
      return searchable(item).includes(query);
    }

    function lessonMatches(lesson) {
      const quiz = quizzesById.get(lesson.quizId);
      const challenge = challengesById.get(lesson.challengeId);
      return itemMatches({
        ...lesson,
        quizText: quiz,
        challengeText: challenge,
        categoryTitle: categoriesById.get(lesson.categoryId)?.title
      });
    }

    function filteredLessons() {
      return data.lessons.filter(lessonMatches);
    }

    function stat(label, value) {
      return el('article', { className: 'stat' }, [
        el('strong', { text: value }),
        el('span', { text: label })
      ]);
    }

    function renderStats() {
      const questionCount = data.quizzes.reduce((sum, quiz) => sum + quiz.questions.length, 0);
      const minutes = data.lessons.reduce((sum, lesson) => sum + lesson.estimatedLearningMinutes, 0);
      replace('stats', el('div', { className: 'stats' }, [
        stat('levels', data.categories.length),
        stat('lessons', data.lessons.length),
        stat('quiz questions', questionCount),
        stat('challenges', data.challenges.length),
        stat('roadmap nodes', data.roadmap.nodes.length),
        stat('authored learn minutes', minutes)
      ]));
    }

    function categoryCard(category) {
      const lessons = data.lessons.filter((lesson) => lesson.categoryId === category.id);
      const questionCount = data.quizzes
        .filter((quiz) => quiz.categoryId === category.id)
        .reduce((sum, quiz) => sum + quiz.questions.length, 0);
      const badge = data.badges.find((item) => item.categoryId === category.id);
      return el('article', { className: 'card' }, [
        el('div', { className: 'row' }, [
          el('h2', { className: 'title', text: category.title }),
          el('span', { className: 'pill ' + category.difficulty, text: category.difficulty })
        ]),
        el('p', { className: 'muted', text: category.description }),
        pills([
          lessons.length + ' lessons',
          questionCount + ' questions',
          category.status,
          'Weeks ' + category.weekRange.start + '–' + category.weekRange.end,
          category.estimatedLearningMinutes + ' authored learn min',
          badge ? badge.title : null
        ]),
        el('p', { text: 'Unlocks after: ' + (category.unlockPrerequisites.length ? category.unlockPrerequisites.join(', ') : 'available at start') }),
        el('h3', { className: 'section-title', text: 'Planned coverage' }),
        el('ul', { className: 'plain-list' }, category.plannedTopics.map((topic) => el('li', { text: topic })))
      ]);
    }

    function renderOverview() {
      const visible = data.categories.filter((category) => itemMatches(category));
      replace('overview', visible.length
        ? el('div', { className: 'grid' }, visible.map(categoryCard))
        : el('div', { className: 'empty', text: 'No levels match the current filters.' }));
    }

    function renderBlock(block) {
      if (block.type === 'paragraph') return el('div', { className: 'content-block' }, el('p', { text: block.text }));
      if (block.type === 'code') return el('div', { className: 'content-block' }, [
        el('pre', {}, el('code', { text: block.code })),
        el('p', { className: 'muted', text: block.caption })
      ]);
      if (block.type === 'callout') return el('aside', { className: 'content-block callout ' + block.tone }, [
        el('strong', { text: block.title }), el('span', { text: block.text })
      ]);
      if (block.type === 'flow') return el('div', { className: 'content-block' }, [
        el('h5', { text: block.title }),
        el('div', { className: 'flow' }, block.steps.map((step) => el('div', { className: 'flow-step' }, [
          el('strong', { text: step.label }), el('small', { text: step.detail })
        ])))
      ]);
      if (block.type === 'table') return el('div', { className: 'content-block' }, [
        el('h5', { text: block.title }),
        el('table', { className: 'content-table' }, [
          el('thead', {}, el('tr', {}, block.columns.map((column) => el('th', { text: column })))),
          el('tbody', {}, block.rows.map((row) => el('tr', {}, row.map((cell) => el('td', { text: cell })))))
        ])
      ]);
      if (block.type === 'list') return el('div', { className: 'content-block' }, [
        el('h5', { text: block.title }), el('ul', { className: 'plain-list' }, block.items.map((item) => el('li', { text: item })))
      ]);
      return el('pre', {}, el('code', { text: text(block) }));
    }

    function renderScout(value) {
      return el('section', { className: 'stage' }, [
        el('h3', { text: 'Scout: context before learning' }),
        el('p', { text: value.purpose }),
        el('p', {}, [el('strong', { text: 'Real Android use: ' }), value.realWorldUse]),
        el('p', {}, [el('strong', { text: 'Outcome: ' }), value.outcome])
      ]);
    }

    function renderLearn(value) {
      return el('section', { className: 'stage' }, [
        el('div', { className: 'row' }, [el('h3', { text: 'Learn' }), el('span', { className: 'pill', text: value.estimatedMinutes + ' min reading' })]),
        ...value.sections.map((section) => el('article', { className: 'learn-section' }, [
          el('h4', { text: section.title }), ...section.blocks.map(renderBlock)
        ])),
        el('aside', { className: 'reading-list' }, [
          el('h4', { text: 'Further reading' }),
          el('p', { className: 'muted', text: 'Optional references to deepen or reinforce this lesson.' }),
          el('ul', { className: 'plain-list' }, value.furtherReading.map((resource) => el('li', {}, [
            el('a', { href: resource.url, target: '_blank', rel: 'noreferrer', text: resource.title }),
            el('span', { className: 'muted', text: ' · ' + resource.publisher + ' · ' + resource.resourceType.replace('_', ' ') }),
            el('p', { text: resource.whyRead })
          ])))
        ])
      ]);
    }

    function renderInspect(value) {
      return el('section', { className: 'stage' }, [
        el('h3', { text: 'Inspect: ' + value.title }),
        el('pre', {}, el('code', { text: value.code })),
        el('ol', { className: 'plain-list' }, value.walkthrough.map((item) => el('li', { text: item }))),
        el('div', { className: 'answers', text: 'Expected output:\\n' + value.expectedOutput })
      ]);
    }

    function renderTraps(items) {
      return el('section', { className: 'stage' }, [
        el('h3', { text: 'Trap check' }),
        ...items.map((item) => el('div', { className: 'callout warning content-block' }, [
          el('strong', { text: item.mistake }), el('p', { text: item.why }), el('p', {}, [el('strong', { text: 'Fix: ' }), item.fix])
        ]))
      ]);
    }

    function renderRecall(items) {
      return el('section', { className: 'stage' }, [
        el('h3', { text: 'Recall (self-paced)' }),
        ...items.map((item) => el('details', {}, [el('summary', { text: item.prompt }), el('p', { text: item.answer })]))
      ]);
    }

    function questionCard(question, index) {
      return el('article', { className: 'quiz-question' }, [
        el('div', { className: 'row' }, [
          el('h4', { className: 'title', text: String(index + 1) + '. ' + question.prompt }),
          el('span', { className: 'pill', text: question.type })
        ]),
        question.options ? el('ul', { className: 'plain-list' }, question.options.map((option) => el('li', { text: text(option) }))) : null,
        el('div', { className: 'answers', text: 'Answer: ' + text(question.answer) }),
        el('p', { className: 'muted', text: question.explanation })
      ]);
    }

    function lessonDetail(lesson) {
      if (!lesson) return el('div', { className: 'empty', text: 'Select a lesson to review it.' });
      const quiz = quizzesById.get(lesson.quizId);
      const challenge = challengesById.get(lesson.challengeId);
      const category = categoriesById.get(lesson.categoryId);
      return el('article', { className: 'panel' }, [
        el('div', { className: 'row' }, [
          el('div', {}, [
            el('p', { className: 'section-title', text: category ? category.title : lesson.categoryId }),
            el('h2', { className: 'title', text: lesson.title })
          ]),
          el('span', { className: 'pill ' + lesson.difficulty, text: lesson.difficulty })
        ]),
        pills(['Week ' + lesson.week, lesson.estimatedLearningMinutes + ' min learning', 'quiz and recall self-paced', lesson.revision.xp + ' XP', lesson.revision.starsAvailable + ' stars', ...lesson.tags]),
        renderScout(lesson.revealStages.scout),
        renderLearn(lesson.revealStages.learn),
        renderInspect(lesson.revealStages.inspect),
        renderTraps(lesson.revealStages.trap_check),
        el('section', { className: 'stage' }, [
          el('h3', { text: 'Challenge introduction' }),
          el('p', { text: lesson.revealStages.challenge_intro.task }),
          el('p', {}, [el('strong', { text: 'Success looks like: ' }), lesson.revealStages.challenge_intro.successLooksLike])
        ]),
        renderRecall(lesson.revealStages.recall),
        el('section', { className: 'stage' }, [
          el('h3', { text: 'Quiz' }),
          el('p', { text: quiz ? quiz.title + ' (' + quiz.questions.length + ' questions)' : 'Missing quiz' }),
          quiz ? el('div', {}, quiz.questions.map(questionCard)) : null
        ]),
        el('section', { className: 'stage' }, [
          el('h3', { text: 'Challenge' }),
          el('p', { text: challenge ? challenge.prompt : 'Missing challenge' }),
          challenge && challenge.starterCode ? el('pre', {}, [el('code', { text: challenge.starterCode.code })]) : null
        ])
      ]);
    }

    function lessonButton(lesson) {
      const category = categoriesById.get(lesson.categoryId);
      return el('button', {
        className: 'card selectable' + (state.selectedLessonId === lesson.id ? ' active' : ''),
        onclick: () => {
          state.selectedLessonId = lesson.id;
          renderAll();
        }
      }, [
        el('h3', { className: 'title', text: lesson.title }),
        el('p', { className: 'muted', text: category ? category.title : lesson.categoryId }),
        pills([lesson.difficulty, lesson.estimatedLearningMinutes + ' min learning', ...lesson.tags.slice(0, 3)])
      ]);
    }

    function renderLessons() {
      const lessons = filteredLessons();
      if (!state.selectedLessonId || !lessons.some((lesson) => lesson.id === state.selectedLessonId)) {
        state.selectedLessonId = lessons[0] ? lessons[0].id : null;
      }
      replace('lessons', lessons.length
        ? el('div', { className: 'two-col' }, [
          el('aside', { className: 'list' }, lessons.map(lessonButton)),
          lessonDetail(lessonsById.get(state.selectedLessonId))
        ])
        : el('div', { className: 'empty', text: 'No lessons match the current filters.' }));
    }

    function renderQuizCard(quiz) {
      const linkedLessons = quiz.linkedLessonIds.map((id) => lessonsById.get(id)).filter(Boolean);
      const typeCounts = quiz.questions.reduce((acc, question) => {
        acc[question.type] = (acc[question.type] || 0) + 1;
        return acc;
      }, {});
      return el('article', { className: 'card' }, [
        el('div', { className: 'row' }, [
          el('h2', { className: 'title', text: quiz.title }),
          el('span', { className: 'pill ' + quiz.difficulty, text: quiz.difficulty })
        ]),
        el('p', { className: 'muted', text: 'Linked lessons: ' + linkedLessons.map((lesson) => lesson.title).join(', ') }),
        pills([quiz.kind, quiz.questions.length + ' questions', ...Object.entries(typeCounts).map(([type, count]) => type + ': ' + count)]),
        el('div', {}, quiz.questions.map(questionCard))
      ]);
    }

    function renderQuizzes() {
      const quizzes = data.quizzes.filter((quiz) => itemMatches({ ...quiz, lessonText: quiz.linkedLessonIds.map((id) => lessonsById.get(id)) }));
      replace('quizzes', quizzes.length
        ? el('div', { className: 'grid' }, quizzes.map(renderQuizCard))
        : el('div', { className: 'empty', text: 'No quizzes match the current filters.' }));
    }

    function renderChallengeCard(challenge) {
      const lesson = lessonsById.get(challenge.lessonId);
      return el('article', { className: 'card' }, [
        el('div', { className: 'row' }, [
          el('h2', { className: 'title', text: challenge.title }),
          el('span', { className: 'pill ' + challenge.difficulty, text: challenge.difficulty })
        ]),
        el('p', { className: 'muted', text: lesson ? 'Linked lesson: ' + lesson.title : 'Linked lesson missing' }),
        el('p', { text: challenge.prompt }),
        pills([challenge.estimatedMinutes + ' min', challenge.rewards.xp + ' XP', challenge.rewards.stars + ' stars']),
        el('h3', { className: 'section-title', text: 'Success criteria' }),
        el('ul', { className: 'plain-list' }, challenge.successCriteria.map((item) => el('li', { text: item }))),
        challenge.starterCode ? el('pre', {}, [el('code', { text: challenge.starterCode.code })]) : null
      ]);
    }

    function renderChallenges() {
      const challenges = data.challenges.filter((challenge) => itemMatches({ ...challenge, lessonText: lessonsById.get(challenge.lessonId) }));
      replace('challenges', challenges.length
        ? el('div', { className: 'grid' }, challenges.map(renderChallengeCard))
        : el('div', { className: 'empty', text: 'No challenges match the current filters.' }));
    }

    function renderRoadmap() {
      const nodes = data.roadmap.nodes.filter((node) => itemMatches({ ...node, categoryTitle: categoriesById.get(node.categoryId)?.title }));
      replace('roadmap', nodes.length
        ? el('div', { className: 'roadmap' }, nodes.map((node) => el('article', { className: 'card road-node ' + node.type }, [
          el('div', { className: 'row' }, [
            el('h2', { className: 'title', text: node.title }),
            el('span', { className: 'pill', text: node.type })
          ]),
          el('p', { className: 'muted', text: categoriesById.get(node.categoryId)?.title || node.categoryId }),
          pills([node.status, node.difficulty, node.estimatedLearningMinutes + ' min learning', node.rewards.xp + ' XP', node.rewards.stars + ' stars']),
          el('p', { text: 'Prerequisites: ' + (node.unlockPrerequisites.length ? node.unlockPrerequisites.join(', ') : 'none') })
        ])))
        : el('div', { className: 'empty', text: 'No roadmap nodes match the current filters.' }));
    }

    function renderGlossary() {
      const query = state.query.trim().toLowerCase();
      const entries = data.glossary.filter((entry) => {
        const categoryOk = state.category === 'all' || entry.categoryId === state.category;
        return categoryOk && (!query || searchable(entry).includes(query));
      });
      replace('glossary', entries.length
        ? el('div', { className: 'grid' }, entries.map((entry) => el('article', { className: 'card' }, [
          el('h2', { className: 'title', text: entry.term }),
          el('p', { text: entry.definition }),
          pills([categoriesById.get(entry.categoryId)?.title || entry.categoryId, ...(entry.relatedLessonIds || []).slice(0, 2)])
        ])))
        : el('div', { className: 'empty', text: 'No glossary entries match the current filters.' }));
    }

    function renderControls() {
      const categorySelect = document.getElementById('category-filter');
      if (!categorySelect.childElementCount) {
        categorySelect.append(el('option', { value: 'all', text: 'All levels' }));
        data.categories.forEach((category) => categorySelect.append(el('option', { value: category.id, text: category.title })));
      }
      categorySelect.value = state.category;
      const difficultySelect = document.getElementById('difficulty-filter');
      if (!difficultySelect.childElementCount) {
        ['all', 'beginner', 'intermediate', 'advanced', 'expert'].forEach((value) => {
          difficultySelect.append(el('option', { value, text: value === 'all' ? 'All difficulties' : value }));
        });
      }
      difficultySelect.value = state.difficulty;
    }

    function renderAll() {
      document.getElementById('summary').textContent = data.curriculum.description;
      document.getElementById('version-pill').textContent = 'Version ' + data.curriculum.version;
      renderControls();
      renderStats();
      renderOverview();
      renderLessons();
      renderQuizzes();
      renderChallenges();
      renderRoadmap();
      renderGlossary();
      document.querySelectorAll('.view').forEach((view) => view.classList.toggle('active', view.id === state.tab));
      document.querySelectorAll('.tabs button').forEach((button) => {
        button.setAttribute('aria-pressed', String(button.dataset.tab === state.tab));
      });
    }

    document.getElementById('search').addEventListener('input', (event) => {
      state.query = event.target.value;
      renderAll();
    });
    document.getElementById('category-filter').addEventListener('change', (event) => {
      state.category = event.target.value;
      renderAll();
    });
    document.getElementById('difficulty-filter').addEventListener('change', (event) => {
      state.difficulty = event.target.value;
      renderAll();
    });
    document.querySelectorAll('.tabs button').forEach((button) => {
      button.addEventListener('click', () => {
        state.tab = button.dataset.tab;
        renderAll();
      });
    });

    renderAll();
  </script>
</body>
</html>
`;

const target = path.join(ROOT, 'review.html');
fs.writeFileSync(target, html);
console.log(`Generated review.html for ${payload.lessons.length} lessons.`);
