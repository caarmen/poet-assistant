The following information must be supplied by the caller:
BRANCH - a git branch currently in development.
BASE_BRANCH - the base branch of the development.
FEATURE - the name of a feature.

You are reviewing branch $BRANCH against the spec at plans/$FEATURE-spec.md.

First read these files if they exist, in this order:
- plans/$FEATURE-spec-issues.md — known holes in the spec itself.
- plans/$FEATURE-revisions.md — Chloe's dispositions from previous rounds.

Rules for previous-round material:
- Do not re-raise a remark whose disposition you accept, or one you
  marked [WITHDRAWN] last round.
- For a remark Chloe disputed with evidence: re-examine with fresh eyes
  against the evidence cited — either withdraw it (mark [WITHDRAWN] in
  your review) or explain specifically why the cited evidence does not
  address your concern. Never repeat your original argument unchanged.
  Verify claimed evidence actually exists in the code — read the cited
  lines yourself.
- Judgment calls (style, taste) defer to the implementer unless the
  remark is a correctness issue.

Review the changes: run 'git diff ${BASE_BRANCH}...HEAD' and 'git log ${BASE_BRANCH}..HEAD',
and read the actual code — not just the diff — wherever the diff
references it. Check: correctness, spec compliance (claim-by-claim
against the spec, EXCEPT where the spec itself is flawed — see below),
and code quality. Some deviation from the spec is acceptable if this results
in improved code (ex: clearer or more idiomatic code). The spec is really more of a guideline.
We often discover nuances during development that would be hard to anticipate when writing the spec.
Favor clean idiomatic modern code, over strict spec compliance.

Look for opportunities for simplification and to use more idiomatic and modern kotlin code.
One goal is to use a modern stack (ex: livedata migrated to flow, xml views migrated to compose),
but another goal is for the code to be more idiomatic for modern standards. Much of the application
was written over 10 years ago in Java, and simply moving code from one package/class to another
to respect clean archi layers may not be enough to truly 'modernize' the code.

Tests: don't run tests unless you need to prove something. If you run tests, run
./gradlew testDebugUnitTest --tests name-of-test-class-or-method

If you need to run instrumentation tests (only to prove something, not for a quick sanity check),
only run the specific test, and only if a single device is connected with adb. Don't try
to launch devices.

Tests are slow, so only run them if really really needed.

Spec issues: if a blocking problem originates in the SPEC rather than
the implementation (the code faithfully implements a flawed spec), tag
the remark [SPEC-ISSUE], describe the flaw, and do NOT count it against
Chloe's approval. Do not demand a clever workaround of a spec defect.

Write your review to plans/$FEATURE-code-review.md with severity tags:
[BLOCKING] / [SHOULD-FIX] / [NIT] / [SPEC-ISSUE] / [WITHDRAWN]
If no [BLOCKING] or [SHOULD-FIX] remarks remain, end the file with the
exact line: VERDICT: APPROVED
Otherwise end with: VERDICT: CHANGES_REQUESTED
