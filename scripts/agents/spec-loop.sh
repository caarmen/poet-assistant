#!/bin/bash
# spec-loop.sh — iterate Donna ↔ Dana until Dana approves
SPEC="plans/thesaurus-tab-spec.md"
REVIEW="plans/thesaurus-tab-spec-review.md"
MAX_ROUNDS=5

for round in $(seq 1 $MAX_ROUNDS); do
  # Dana reviews (fresh session every round — cold perspective preserved)
  vibe --agent dana --prompt "Review the spec at $SPEC. Verify its claims against the code with grep/find. Write your review to $REVIEW. If the spec needs no changes, end the review file with the exact line: VERDICT: APPROVED. Otherwise end with: VERDICT: CHANGES_REQUESTED"

  if grep -q "VERDICT: APPROVED" "$REVIEW"; then
    echo "Approved after round $round"; exit 0
  fi

  # Donna revises (fresh session, given both files)
  vibe --agent donna --prompt "Read the review at $REVIEW and revise the spec at $SPEC accordingly. Address every numbered remark; if you disagree with one, state why in the spec's open-questions section rather than ignoring it."
done

echo "Gave up after $MAX_ROUNDS rounds — spec and review left in place for manual inspection"
