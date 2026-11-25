The following information must be supplied by the caller:
BRANCH - a git branch currently in development.
BASE_BRANCH - the base branch of the development.
FEATURE - the name of a feature.

Cameron's review is at plans/$FEATURE-code-review.md.

For each remark:
- If you agree: fix it, with a fixup commit, to adjust the commit which originally
  introduced the code.
  -- If you disagree: do NOT fix blindly. Record a disposition in
  plans/$FEATURE-revisions.md under the remark ID with your reasoning and concrete
  evidence from the code (file, line, behavior) — Cameron will verify
  it, so cite only what exists.
  -- [NIT]: fix if trivial, otherwise same disposition mechanism.
  -- [SPEC-ISSUE]: do not 'fix' it. If the issue is new (not already in
  plans/$FEATURE-spec-issues.md), add it there: what the spec says, the code reality,
  and 2-3 candidate resolutions with tradeoffs. If the issue blocks
  sensible implementation entirely, say so explicitly in your reply.

Do not re-litigate remarks marked [WITHDRAWN].

Commit your fixes with a descriptive message (process notes belong in
plans/$FEATURE-revisions.md / plans/$FEATURE-spec-issues.md, not commit messages).
