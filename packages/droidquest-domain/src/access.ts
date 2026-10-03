import type {
  ContentIndex,
  RoadmapGraph,
  RoadmapNode,
  SearchDocument,
} from "./types";

export function canStartRoadmapNode(
  node: RoadmapNode,
  completedNodeIds: readonly string[],
): boolean {
  return (
    node.status !== "planned" &&
    node.type !== "level_preview" &&
    node.unlockPrerequisites.every((id) => completedNodeIds.includes(id))
  );
}

export function canAccessRoadmapNode(
  node: RoadmapNode,
  completedNodeIds: readonly string[],
): boolean {
  return completedNodeIds.includes(node.id) || canStartRoadmapNode(node, completedNodeIds);
}

function nodeForSearchDocument(
  graph: RoadmapGraph,
  index: ContentIndex,
  document: SearchDocument,
) {
  if (document.type === "lesson") {
    return graph.nodes.find((node) => node.lessonId === document.id);
  }
  if (document.type === "challenge") {
    const lessonId =
      document.lessonId ?? index.challenges.find((item) => item.id === document.id)?.lessonId;
    return graph.nodes.find((node) => node.lessonId === lessonId);
  }
  if (document.type === "quiz") {
    const direct = graph.nodes.find((node) => node.quizId === document.id);
    if (direct) return direct;
    const lessonIds =
      index.quizzes.find((item) => item.id === document.id)?.linkedLessonIds ?? [];
    return graph.nodes.find((node) => node.lessonId && lessonIds.includes(node.lessonId));
  }
  return undefined;
}

export function canAccessSearchDocument(
  graph: RoadmapGraph,
  index: ContentIndex,
  document: SearchDocument,
  completedNodeIds: readonly string[],
): boolean {
  if (document.type === "glossary") return true;
  if (document.type === "category") {
    const categoryId = document.categoryId ?? document.id;
    return graph.nodes
      .filter((node) => node.categoryId === categoryId)
      .some((node) => canAccessRoadmapNode(node, completedNodeIds));
  }
  const node = nodeForSearchDocument(graph, index, document);
  return Boolean(node && canAccessRoadmapNode(node, completedNodeIds));
}
