# Validation rules

`npm run validate` performs schema and cross-file checks. `npm run test:content` also regenerates derived files, confirms they were already current, and validates again.

## Structural checks

- Every JSON and schema file parses.
- Category, lesson, quiz, challenge, badge, glossary, and roadmap records match their JSON Schema.
- IDs are unique and source filenames match record IDs.
- Exactly 12 ordered curriculum levels exist.
- Planned levels publish no lesson IDs; in-progress and complete levels publish at least one lesson.
- Lesson week numbers fit their level's week range.

## Instruction checks

- Every lesson contains all six reveal stages.
- Learn time is 1–10 minutes and agrees with the lesson-level estimate.
- Learn contains 450–1,600 extracted words, a code block, and a flow or table.
- Learn ends with two to four unique further-reading links, including an official document or codelab.
- Every lesson has a dedicated lesson quiz and challenge.
- Every question has an explanation and links to a lesson covered by its quiz.
- Question IDs are globally unique and all supported question types appear in published content.

## Reference and graph checks

- All category, lesson, quiz, challenge, glossary, and badge references resolve.
- Every roadmap edge and unlock prerequisite points to a valid node.
- Available lesson and checkpoint nodes contain the corresponding content link.
- Planned level previews cannot masquerade as available content.
- The combined roadmap dependency graph contains no cycle.
- Every level has an available node or planned preview.
- Every authored week lists valid lessons and a valid checkpoint.

## Generated output checks

Generation must produce a content index, search index, roadmap graph, and standalone review page. `scripts/check-generated.js` compares deterministic output before and after generation; CI fails when committed generated files are stale or invalid.
