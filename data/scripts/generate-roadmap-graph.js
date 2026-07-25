#!/usr/bin/env node
'use strict';

const path = require('node:path');
const { CONTENT, loadAll, writeJson } = require('./lib/content');

const all = loadAll();
const nodes = all.roadmaps.flatMap(({ data }) => data.nodes);
const explicitEdges = all.roadmaps.flatMap(({ data }) => data.edges);
const prerequisiteEdges = nodes.flatMap((node) => node.unlockPrerequisites.map((from) => ({ from, to: node.id, kind: 'prerequisite' })));
const edgeKey = (edge) => `${edge.from}|${edge.to}`;
const edgeMap = new Map([...explicitEdges, ...prerequisiteEdges].map((edge) => [edgeKey(edge), edge]));
const edges = [...edgeMap.values()].sort((a, b) => edgeKey(a).localeCompare(edgeKey(b)));
const adjacency = Object.fromEntries(nodes.map((node) => [node.id, []]));
const incoming = Object.fromEntries(nodes.map((node) => [node.id, []]));
for (const edge of edges) { adjacency[edge.from].push(edge.to); incoming[edge.to].push(edge.from); }
for (const values of Object.values(adjacency)) values.sort();
for (const values of Object.values(incoming)) values.sort();

const indegree = Object.fromEntries(nodes.map((node) => [node.id, incoming[node.id].length]));
const queue = nodes.map((node) => node.id).filter((id) => indegree[id] === 0).sort();
const topologicalOrder = [];
while (queue.length) {
  const current = queue.shift();
  topologicalOrder.push(current);
  for (const next of adjacency[current]) {
    indegree[next] -= 1;
    if (indegree[next] === 0) { queue.push(next); queue.sort(); }
  }
}
if (topologicalOrder.length !== nodes.length) throw new Error('Cannot generate roadmap graph: cycle detected.');

const categories = all.categories.map(({ data }) => ({
  id: data.id,
  title: data.title,
  order: data.order,
  startNodeIds: nodes.filter((node) => node.categoryId === data.id && node.type === 'start').map((node) => node.id),
  checkpointNodeIds: nodes.filter((node) => node.categoryId === data.id && ['boss', 'checkpoint'].includes(node.type)).map((node) => node.id),
  previewNodeIds: nodes.filter((node) => node.categoryId === data.id && node.type === 'level_preview').map((node) => node.id)
}));

writeJson(path.join(CONTENT, 'generated', 'roadmap-graph.json'), {
  id: 'droidquest-roadmap-graph',
  curriculumVersion: all.curriculum.version,
  nodes: nodes.sort((a, b) => a.id.localeCompare(b.id)),
  edges,
  adjacency,
  incoming,
  topologicalOrder,
  categories
});
console.log(`Generated roadmap-graph.json with ${nodes.length} nodes and ${edges.length} edges.`);
