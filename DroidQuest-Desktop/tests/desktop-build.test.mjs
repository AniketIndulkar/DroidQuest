import assert from "node:assert/strict";
import { access, readFile } from "node:fs/promises";
import test from "node:test";

test("desktop build contains the application and curriculum", async () => {
  const html = await readFile(new URL("../dist/index.html", import.meta.url), "utf8");
  assert.match(html, /<title>DroidQuest<\/title>/);
  assert.match(html, /src="\/assets\/[^\"]+\.js"/);

  const index = JSON.parse(
    await readFile(
      new URL("../dist/content/generated/content-index.json", import.meta.url),
      "utf8",
    ),
  );
  assert.equal(index.curriculumVersion, "1.0.0");
  assert.equal(index.counts.lessons, 302);
  await access(new URL("../dist/content/generated/roadmap-graph.json", import.meta.url));
});
