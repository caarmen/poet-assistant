#!/bin/bash
# dev-loop.sh — Cameron ↔ Chloe loop for the thesaurus-tab feature.
# Hardcoded for this feature; generalize only after the pattern is proven.

SPEC="plans/thesaurus-tab-spec.md"
REVIEW="plans/thesaurus-tab-code-review.md"
REVISIONS="plans/thesaurus-tab-revisions.md"
SPEC_ISSUES="plans/thesaurus-tab-spec-issues.md"
BRANCH="feature/thesaurus-tab"
BASE_BRANCH="loops"
MAX_ROUNDS=5

set -euo pipefail

for round in $(seq 1 "$MAX_ROUNDS"); do
  echo "=== Round $round: Cameron reviews ==="

  vibe --agent cameron --prompt "You are reviewing branch $BRANCH against the spec at $SPEC.

First read these files if they exist, in this order:
- $SPEC_ISSUES — known holes in the spec itself.
- $REVISIONS — Chloe's dispositions from previous rounds.

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
and code quality.

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

Write your review to $REVIEW with severity tags:
  [BLOCKING] / [SHOULD-FIX] / [NIT] / [SPEC-ISSUE] / [WITHDRAWN]
If no [BLOCKING] or [SHOULD-FIX] remarks remain, end the file with the
exact line: VERDICT: APPROVED
Otherwise end with: VERDICT: CHANGES_REQUESTED"

  if grep -q "VERDICT: APPROVED" "$REVIEW"; then
    echo "=== Approved after round $round ==="
    if [ -f "$SPEC_ISSUES" ]; then
      echo ""
      echo "NOTE: spec issues were raised during development:"
      echo "  $SPEC_ISSUES"
      echo "Consider a spec-amendment round (Dana reviews, Donna amends)"
      echo "BEFORE merging, then fold amendments back into the branch."
    fi
    echo ""
    echo "Merge decision is yours. Recent commits on $BRANCH:"
    git log --oneline ${BASE_BRANCH}.."$BRANCH"
    exit 0
  fi

  if [ "$round" -eq "$MAX_ROUNDS" ]; then
    break
  fi

  echo "=== Round $round: Chloe revises ==="

  # Chloe: resumed kickoff session — her mental model accumulates.
  vibe --agent chloe --continue --prompt "Cameron's review is at $REVIEW.

For each remark:
- If you agree: fix it, with a fixup commit, to adjust the commit which originally
  introduced the code.
- If you disagree: do NOT fix blindly. Record a disposition in
  $REVISIONS under the remark ID with your reasoning and concrete
  evidence from the code (file, line, behavior) — Cameron will verify
  it, so cite only what exists.
- [NIT]: fix if trivial, otherwise same disposition mechanism.
- [SPEC-ISSUE]: do not 'fix' it. If the issue is new (not already in
  $SPEC_ISSUES), add it there: what the spec says, the code reality,
  and 2-3 candidate resolutions with tradeoffs. If the issue blocks
  sensible implementation entirely, say so explicitly in your reply.

Do not re-litigate remarks marked [WITHDRAWN].

Commit your fixes with a descriptive message (process notes belong in
$REVISIONS / $SPEC_ISSUES, not commit messages)."
done

echo "=== No convergence after $MAX_ROUNDS rounds ==="
echo "Arbitrate via $REVISIONS (and $SPEC_ISSUES if present), then rerun."
exit 1