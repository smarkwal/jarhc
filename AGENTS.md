# AGENTS.md

Conventions for working in the JarHC repository.

## Documentation

The documentation under `docs/` is published with MkDocs (Material theme).
The pages under `docs/reports/` describe the sections of a JarHC report.
[docs/reports/jar-files.md](docs/reports/jar-files.md) is the reference example
for the style described below.

### Source tracking (front matter)

Every documentation page that is derived from the source code begins with a YAML
front matter block. It records the most important sources the page is based on
and the date the page was last reviewed against them:

```yaml
---
sources:
  - jarhc/src/main/java/org/jarhc/analyzer/JarFilesAnalyzer.java
last_reviewed: 2026-06-25
---
```

- List only the most important sources, not every file that was read. A good
  entry is one whose change would plausibly change what the page must say, for
  example the analyzer of a report section and any configuration or resource that
  drives its output. A reviewer who scans these few files should naturally reach
  any further resources they reference, so those do not need to be listed.
- `last_reviewed` is the date (in `YYYY-MM-DD` format) on which the page was last
  confirmed to match its sources. Update it whenever the page is reviewed, even if
  no content changes, because the content is still correct.
- The front matter is page metadata. It is not rendered into the published HTML
  and is not shown to readers.

### Tone and accuracy

- Write in a precise and professional style.
- Do not use em dashes or other stylistic tells. Use plain punctuation such as
  commas, colons, and parentheses.
- Verify every description against the analyzer source code. Do not assume or
  guess behavior. For a report section, read the corresponding analyzer (for
  example `JarFilesAnalyzer` for the "JAR Files" section) and describe what it
  actually produces.
- Do not mention implementation details in user-facing text. Describe the
  information shown to the user, not the internal mechanism used to obtain it
  (for example, name the data in a column, not the API used to look it up).

### Report-page structure

Each report page follows the same skeleton:

1. An H1 heading equal to the report section name.
2. An intro paragraph stating what the section lists and its granularity, for
   example "one row per JAR file".
3. The lead-in line `The table contains the following columns:`.
4. One block per column (see the column style below), in the same order as the
   columns appear in the report.
5. A closing paragraph describing any summary or total row, if the section has
   one.
6. An **Example** title (bold), followed by the screenshot link to the example
   report.
7. A `Next:` line linking to the next report page.

### Column documentation style

Document each column as a bold title on its own line, followed by a paragraph:

```markdown
**Column name**

Description of the column as one or more sentences.
```

- The bold title must match the exact column header used in the report.
- Use sub-bullets only when a column has several distinct notations or cases
  (for example the "Packages" and "Issues" columns in the JAR Files page).
- Document special value markers exactly as the report emits them, formatted as
  inline code: `[unknown]`, `[error]`, `[none]`, `[no class files]`.
- Show concrete examples in inline code where they aid understanding, for example
  `Yes (Java 17, Java 11)`.

### Reviewing and updating documentation

The `docs-review` skill (`.agents/skills/docs-review/`) packages the workflow for
reviewing and updating these pages. It bundles a freshness check that flags pages
whose `sources` changed (by git commit date) after their `last_reviewed` date:

```shell
.agents/skills/docs-review/scripts/check-docs-freshness.sh
```

Run it before a release, or after changing analyzers, command line options, or
dependencies, to find pages that need a re-review. See the skill for the
page-by-page review and update workflow.
