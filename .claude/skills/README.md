# Vendored skills

These skills are vendored (copied) from the fork
[`rzk91/matt-skills`](https://github.com/rzk91/matt-skills) — itself a fork of
[`mattpocock/skills`](https://github.com/mattpocock/skills) — so they are
available in every Claude Code session (including Claude Code on the web, where
`~/.claude` and plugin installs do not persist across the ephemeral container)
without fetching anything at startup.

Claude Code auto-discovers each `<skill>/SKILL.md` and registers it by its
one-line `description`. The full skill body is only read when the skill is
actually invoked (e.g. `/tdd`), so vendoring these costs no extra context at
session start.

## What's here

Everything under `skills/engineering/` and `skills/productivity/` from the fork,
flattened one level (each skill is a directory containing `SKILL.md` plus any
supporting reference files and `agents/` subdirectories it relies on).

Note: `code-review` shares a name with a Claude Code built-in skill.

## Re-syncing with the fork

These are frozen copies. To pull in upstream changes:

```sh
git clone --depth 1 https://github.com/rzk91/matt-skills.git /tmp/matt-skills
for cat in engineering productivity; do
  for d in /tmp/matt-skills/skills/$cat/*/; do
    name=$(basename "$d")
    rm -rf ".claude/skills/$name"
    cp -R "$d" ".claude/skills/$name"
  done
done
rm -rf /tmp/matt-skills
```

Review the diff before committing.
