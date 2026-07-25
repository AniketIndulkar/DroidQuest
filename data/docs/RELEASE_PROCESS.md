# Content release process

Content uses semantic versions independently from the Android application.

- Patch: wording, hint, explanation, or metadata correction that does not change IDs or progression.
- Minor: new backward-compatible lessons, weeks, question types, blocks, glossary entries, or optional branches.
- Major: breaking schema, ID, progression, or minimum app content API change.

Before release:

1. Verify facts and source references against current primary documentation.
2. Review teaching depth, code output, quiz answers, challenge feasibility, and prerequisite direction manually.
3. Run `npm run generate` followed by `npm run test:content`.
4. Open `review.html` and review every changed Learn block, answer, and planned/available roadmap state.
5. Increment `contentRevision` for every published snapshot and the semantic version when compatibility changes.
6. Tag the Git commit and publish the immutable content directory plus checksums as a GitHub release.

The Android app should always ship with a validated bundled snapshot. Optional GitHub sync downloads to a temporary location, verifies content API compatibility and hashes, validates the graph, and only then swaps the active local snapshot. A failed update leaves the previous content usable.

Never recycle a released ID. If content is retired, keep migration metadata so existing learner progress can be preserved or deliberately mapped.
