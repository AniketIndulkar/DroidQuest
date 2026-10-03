# Learning bite: behavioural design

## Product intent

The widget gives the user one useful, finite interaction at the point where a phone unlock often
ends at the launcher. It is not intended to increase unlocks, manufacture a streak, or compete for
attention. It has no notification loop, points, countdown, or infinite feed. One bite should be
understandable on its own and skipping should carry no penalty.

Android does not offer a reliable, battery-respectful third-party callback for every unlock. The
widget therefore occupies the home-screen moment, requests a system-managed refresh every 12 hours,
and lets the user request another item. Android may defer periodic updates.

## Why the loop is designed this way

| Finding | Product decision |
| --- | --- |
| Retrieval changes memory more than repeated exposure | Target two MCQs for every one fact |
| MCQ distractors can reinforce false information | Reveal the accepted answer and authored explanation after every response |
| Durable retention benefits from distributed encounters | Persist item memory and expand correct reviews across 1, 3, 7, 21, and 60 days |
| Failure signals that retrieval is not yet stable | Return a missed question after 10 minutes and reset its interval stage |
| Mixed practice makes the learner identify the relevant concept | Exclude the three most recent topics when another eligible topic exists |
| Tiny content is useful only if it is coherent | Use complete authored recall answers and filter content that cannot fit without truncating its meaning |

The schedule is deliberately a transparent heuristic, not a claim to be an optimal memory model.
The existing corpus does not yet have enough widget-answer history to fit FSRS-style parameters, and
the user's desired retention horizon is unknown. The first release records per-item shown, correct,
wrong, interval-stage, and due-time state locally so its assumptions can be tested later.

## “Random” versus useful variety

Uniform randomness would over-sample some topics, repeat items immediately, and neglect material the
learner missed. Selection is constrained randomness:

1. Due items outrank unseen items; unseen items outrank not-yet-due items.
2. The fact/question cadence chooses the preferred kind when available.
3. Recent topics and items are removed when alternatives exist.
4. One eligible item is sampled randomly from the remaining pool.

This preserves surprise without surrendering instructional intent. The content continues to come
from DroidQuest's verified offline snapshot; the widget does not generate facts or answers.

## Risks and limits

- **Recognition is not free recall.** MCQs are appropriate for a tap-only widget but can overstate
  mastery. The app's recall prompts remain the stronger learning surface.
- **A fact can create familiarity without learning.** Facts are a minority and carry a recall cue;
  repeated retrieval remains the main loop.
- **Interruption can make difficulty undesirable.** The user can skip instantly, there is no loss,
  and content is capped to four short options.
- **Compulsion is the wrong success metric.** Do not optimize unlock count or add variable rewards.
  Prefer delayed correctness and voluntary answer rate.
- **Topic difficulty differs.** The initial intervals are product defaults. Change them only against
  observed delayed-recall evidence, not same-session answer rate.
- **Local state is not canonical course mastery.** Widget memory is intentionally separate from XP,
  quiz passage, and roadmap unlocks, preventing a glance interaction from farming progress.

## Evaluation plan

Before tuning, measure locally or with explicit analytics consent:

- answer rate per shown question;
- delayed correctness by interval stage, topic, and previous miss count;
- skip rate and time-to-answer (to detect prompts that are too long or ambiguous);
- incorrect-option concentration (to detect misleading distractors);
- repeat accuracy after 1/3/7/21/60 days, rather than immediate accuracy alone.

Useful experiments should alter one mechanism at a time: two-versus-one question cadence, 10-minute
versus next-day lapse recovery, or recent-topic window size. The primary outcome should be delayed
correctness with guardrails on skip rate and widget removal—not raw taps.

## Research and platform references

- Karpicke & Roediger, *The Critical Importance of Retrieval for Learning* (Science, 2008),
  <https://doi.org/10.1126/science.1152408>
- Butler & Roediger, *Feedback Enhances the Positive Effects and Reduces the Negative Effects of
  Multiple-Choice Testing* (Memory & Cognition, 2008), <https://doi.org/10.3758/MC.36.3.604>
- Cepeda et al., *Spacing Effects in Learning: A Temporal Ridgeline of Optimal Retention*
  (Psychological Science, 2008), <https://doi.org/10.1111/j.1467-9280.2008.02209.x>
- Rohrer & Taylor, *The Shuffling of Mathematics Problems Improves Learning* (Instructional Science,
  2007), <https://doi.org/10.1007/s11251-007-9015-8>
- Android Developers, *App widgets overview*,
  <https://developer.android.com/develop/ui/views/appwidgets/overview>
- Android Developers, *Implicit broadcast exceptions*,
  <https://developer.android.com/develop/background-work/background-tasks/broadcasts/broadcast-exceptions>
