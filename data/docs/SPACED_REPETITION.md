# Active recall and spaced repetition

DroidQuest treats retrieval practice as support for long-term learning, not as another gate.
Roadmap unlocks continue to depend on lesson and checkpoint quizzes. Review state only affects
recommendations and the daily review queue.

## Gentle quiz feedback

- Objective questions are graded automatically.
- A wrong objective answer always reveals the accepted answer and explanation before continuing.
- `code_output` asks for the program's printed output, not source code, and renders the expected
  output without collapsing line breaks.
- `short_answer` and `spot_bug` are open-ended. The learner compares their response with a model
  answer and records whether they captured the idea; exact prose is never required.
- A failed quiz offers an immediate retry. Feedback language describes what to review and does not
  frame mistakes as punishment.

## Recall item identity

Every authored recall prompt has a permanent kebab-case `id`. Learner review state is keyed by this
ID, never by prompt text or array position. Recall IDs remain stable after release.

## First scheduling policy

The initial offline scheduler uses each lesson's authored `reviewIntervalsDays` as its progression.
After revealing the model answer, the learner chooses:

- `again`: revisit after ten minutes; count a lapse when the item was previously reviewed.
- `hard`: revisit after one day, or half the current interval for an established item.
- `good`: advance to the next authored interval, then double beyond the authored list.
- `easy`: skip one authored interval, then grow faster beyond the list.

The policy is pure and receives the current time explicitly so scheduling is deterministic in tests.
It can later be replaced by an adaptive scheduler without changing content IDs or stored progress.

## Review experience

- Recall in a lesson requires an attempted response before the model answer is shown.
- Rating the comparison schedules the next retrieval.
- Home shows a calm `Review due` card only when items are due.
- Daily review mixes due items from completed learning and does not award repeatable XP.
- Review sessions never display a punitive lifetime backlog or reset curriculum progress.

## Persistence

Curriculum remains immutable. Per-item review state lives with learner progress and records the due
time, interval, repetitions, lapses, last review time, and last rating. The first implementation uses
a compact versioned DataStore representation behind `ProgressRepository`; callers do not depend on
that storage format.

