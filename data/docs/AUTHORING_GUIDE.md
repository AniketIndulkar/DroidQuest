# Authoring guide

## Add a lesson

1. Choose its level and week, then assign a permanent ID: `level-<NN>-week-<NN>-<sequence>-<topic>`.
2. Create `content/lessons/<id>.json` and add the ID to the level's ordered `lessonIds`.
3. Keep `estimatedLearningMinutes` at 10 or below and match it to `revealStages.learn.estimatedMinutes`.
4. Write at least three Learn sections. Across them, include meaningful prose, at least one Kotlin code block, and at least one flow or table visual.
5. Write a complete Inspect example with expected output and a step-by-step explanation.
6. Add at least two actionable traps and three recall prompts.
7. Add two to four learner-facing `furtherReading` links. Include at least one official document or codelab and explain why each resource is worth opening.
8. Add official primary sources to `sourceRefs`; paraphrase and teach in DroidQuest's own voice.
9. Create and link the lesson quiz and challenge.
10. Add a roadmap node and prerequisite edge.
11. Run `npm run generate` and `npm run test:content`.

The validator rejects a Learn stage below 450 extracted words because that usually indicates summary text rather than instruction. It rejects more than 1,600 extracted words as likely to exceed the 10-minute contract. Code and visuals still require author judgement; passing a word-count check is not proof of good teaching.

## Write Scout well

Scout is a short orientation. It should answer why the concept matters, where it appears in Android work, and what the learner will do after the lesson. Do not define the whole topic or hide required instruction here.

## Add a quiz

A lesson quiz uses `kind: lesson`, links to exactly one lesson, and normally contains three focused questions. A weekly or level checkpoint may link to multiple lessons. Every question requires a globally unique ID, an explanation, and a `lessonId` included in the quiz's `linkedLessonIds`.

Use question types because they fit the concept, not to satisfy variety locally. The repository as a whole must exercise all supported types: `single_choice`, `multiple_choice`, `true_false`, `fill_blank`, `order_steps`, `match_pairs`, `code_output`, `spot_bug`, and `short_answer`.

## Add a challenge

The challenge should require the learner to produce or change something. Include observable success criteria, progressive hints, starter code, a non-copy-paste solution outline, and verification steps. Keep challenge duration separate from learning duration.

## Publish another week

Add the week's lessons, lesson quizzes, challenges, glossary links, roadmap nodes, and cumulative checkpoint. Then append the week to `curriculum.authoredWeeks`. Keep the level `in_progress` until all of its weeks and final checkpoint are complete.

Future Level 12 specialisations are independent branches. Never implement a rule that forces a learner to choose exactly two or locks one track after another is selected.
