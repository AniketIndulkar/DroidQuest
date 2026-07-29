import { cp, mkdir, readFile, rm } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const source = resolve(here, "../../data/content");
const target = resolve(here, "../public/content");

const index = JSON.parse(
  await readFile(resolve(source, "generated/content-index.json"), "utf8"),
);

await rm(target, { recursive: true, force: true });
await mkdir(dirname(target), { recursive: true });
await cp(source, target, { recursive: true });

console.log(
  `Synced DroidQuest ${index.curriculumVersion}: ${index.counts.lessons} lessons, ${index.counts.quizzes} quizzes, ${index.counts.challenges} challenges.`,
);
