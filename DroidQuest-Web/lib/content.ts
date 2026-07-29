import type {
  Challenge,
  ContentIndex,
  Curriculum,
  IndexEntry,
  Lesson,
  LoadedContent,
  Quiz,
  RoadmapGraph,
  SearchIndex,
} from "./types";

const cache = new Map<string, unknown>();

async function json<T>(path: string): Promise<T> {
  const publicPath = path.startsWith("content/") ? `/${path}` : path;
  if (cache.has(publicPath)) return cache.get(publicPath) as T;
  const response = await fetch(publicPath);
  if (!response.ok) throw new Error(`Could not load ${publicPath}`);
  const value = (await response.json()) as T;
  cache.set(publicPath, value);
  return value;
}

function entry(index: ContentIndex, collection: keyof ContentIndex, id: string) {
  const values = index[collection];
  if (!Array.isArray(values)) return undefined;
  return (values as IndexEntry[]).find((item) => item.id === id);
}

export async function loadContent(): Promise<LoadedContent> {
  const [index, curriculum, graph] = await Promise.all([
    json<ContentIndex>("/content/generated/content-index.json"),
    json<Curriculum>("/content/curriculum.json"),
    json<RoadmapGraph>("/content/generated/roadmap-graph.json"),
  ]);
  const categories = await Promise.all(
    index.categories
      .slice()
      .sort((a, b) => (a.order ?? 0) - (b.order ?? 0))
      .map((item) => json<LoadedContent["categories"][number]>(item.path)),
  );
  return { index, curriculum, graph, categories };
}

export function loadLesson(index: ContentIndex, id: string) {
  const item = entry(index, "lessons", id);
  if (!item) throw new Error(`Unknown lesson ${id}`);
  return json<Lesson>(item.path);
}

export function loadQuiz(index: ContentIndex, id: string) {
  const item = entry(index, "quizzes", id);
  if (!item) throw new Error(`Unknown quiz ${id}`);
  return json<Quiz>(item.path);
}

export function loadChallenge(index: ContentIndex, id: string) {
  const item = entry(index, "challenges", id);
  if (!item) throw new Error(`Unknown challenge ${id}`);
  return json<Challenge>(item.path);
}

export function loadSearch() {
  return json<SearchIndex>("/content/generated/search-index.json");
}
