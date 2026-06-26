---
name: docs-review
description: >
  Review and update the JarHC project documentation under website/docs/. Use this whenever
  the user wants to review documentation, update or rewrite a docs page, check
  whether documentation is stale or out of date relative to the code, verify that a
  report-section page still matches its analyzer, or add a new documentation page.
  Also use it after changing source code (analyzers, command line options,
  dependencies) that the documentation describes, to find and refresh the affected
  pages. It can run a freshness check that flags pages whose sources changed after
  their last review.
---

# Documentation review

This skill helps keep the JarHC documentation under `website/docs/` accurate and in a
consistent style. It does three things: find pages that may be out of date,
update a page against its sources, and scaffold a new page.

## Conventions live in AGENTS.md

All documentation conventions (tone, the report-page skeleton, the column
documentation style, and the front-matter schema for `sources`,
`last_reviewed`, and `description`) are defined in the repository's `AGENTS.md`, under the
"Documentation" section. Read it first and follow it. Do not restate those rules
here; `AGENTS.md` is the single source of truth so the two cannot drift apart.

## Find pages that may be stale

Run the bundled script. It reports pages whose listed sources have a newer git
commit date than the page's `last_reviewed`, plus missing sources and sources
with no git history:

```shell
.agents/skills/docs-review/scripts/check-docs-freshness.sh
```

The script works from the git repository root and scans `website/docs/` by default; pass
a different directory as the first argument if needed. A non-zero exit code means
at least one page needs attention. "Stale" only means a source changed after the
review date, not that the change was necessarily doc-relevant, so each finding
still needs a human-style judgement during the update step.

## Update a page

For each page that needs attention:

1. Read the page and the files in its `sources` front matter.
2. Verify every statement on the page against the source. For a report-section
   page, the source is the analyzer (for example `BlacklistAnalyzer` for the
   Blacklist section); describe what it actually produces, including special
   value markers and column order.
3. Apply the conventions from `AGENTS.md` (tone, structure, column style).
4. If the source genuinely changed what the page must say, update the prose. If
   the page is already correct, leave the prose unchanged.
5. Either way, set `last_reviewed` to today's date.

## Add a new report-section page

Follow the report-page skeleton defined in `AGENTS.md`. Verify the column names
and behavior against the analyzer, and add the analyzer (and any resource that
drives its output) to the page's `sources` front matter. Also give the page a
`description` (see `AGENTS.md`).

## Validate the build

After updating or adding pages, build the documentation in strict mode to catch
broken internal links, invalid anchors, and pages missing from the navigation.
Strict mode turns MkDocs' link and navigation validation warnings into errors:

```shell
docker run --rm -v "$PWD/website":/docs squidfunk/mkdocs-material build --strict
```

A non-zero exit code means there is a link or navigation problem to fix.
This complements the freshness check: the freshness check finds out-of-date
pages, while the strict build verifies the documentation is internally consistent.
