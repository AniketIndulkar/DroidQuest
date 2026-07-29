import assert from "node:assert/strict";
import { access, readFile } from "node:fs/promises";
import test from "node:test";

const projectRoot = new URL("../", import.meta.url);

async function render() {
  const workerUrl = new URL("../dist/server/index.js", import.meta.url);
  workerUrl.searchParams.set("test", `${process.pid}-${Date.now()}`);
  const { default: worker } = await import(workerUrl.href);

  return worker.fetch(
    new Request("http://localhost/", { headers: { accept: "text/html" } }),
    { ASSETS: { fetch: async () => new Response("Not found", { status: 404 }) } },
    { waitUntil() {}, passThroughOnException() {} },
  );
}

test("server-renders the DroidQuest application shell", async () => {
  const response = await render();
  assert.equal(response.status, 200);
  assert.match(response.headers.get("content-type") ?? "", /^text\/html\b/i);

  const html = await response.text();
  assert.match(html, /<title>DroidQuest — Learn Android by building<\/title>/i);
  assert.match(html, /Preparing your quest/);
  assert.match(html, /og\.png/);
  assert.doesNotMatch(html, /codex-preview|react-loading-skeleton|Your site is taking shape/i);
});

test("ships the verified shared curriculum snapshot", async () => {
  const index = JSON.parse(
    await readFile(new URL("../public/content/generated/content-index.json", import.meta.url), "utf8"),
  );
  assert.equal(index.curriculumVersion, "1.0.0");
  assert.equal(index.counts.categories, 12);
  assert.equal(index.counts.lessons, 302);
  assert.equal(index.counts.quizzes, 361);
  assert.equal(index.counts.challenges, 302);
  await access(new URL("../public/og.png", import.meta.url));
  await assert.rejects(access(new URL("app/_sites-preview", projectRoot)));
});
