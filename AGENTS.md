# AGENTS.md

Conventions for working in the JarHC repository. Keep this file tight: short bullet
lists of facts, minimal prose. The Documentation section is the deliberate
exception, it is a detailed style guide. Preserve both styles when editing.

## Workflow

- Pushes to `main` are blocked by GitHub.
- All changes go through a branch and a pull request.
- GitHub Copilot reviews every pull request.
- Leave `dependabot/...` branches to Dependabot.

## Project layout

- Multi-project build: root + three subprojects.
  - `jarhc` — the tool itself (library and command line application).
  - `jarhc-release-tests` — release and integration tests (Testcontainers, Docker).
  - `jarhc-docs-tasks` — documentation helper tasks (Playwright).
- `website/` — MkDocs documentation sources (`mkdocs.yml`, `docs/`).
- Add the Apache 2.0 license header to new source files.

## Java and Gradle versions

- `jarhc` bytecode: Java 11.
- Build runs on: Java 17 (subprojects fail fast on anything older).
- CI installs both JDK 11 and 17.
- Gradle wrapper: 9.6.1.

## Dependencies

- Versions live in the version catalog: `gradle/libs.versions.toml`.
- Dependency locking is enabled; lockfiles are committed.
- Check for updates: `./gradlew dependencyUpdates`.
- After changing any version, refresh the lockfiles: `./gradlew updateGradleLockfiles --write-locks`.
- Dependabot runs monthly:
  - GitHub Actions: grouped into one PR; bumps the SHA and the `# vX.Y.Z` comment.
  - Python docs toolchain (`website/`): grouped into one PR.
  - Gradle dependencies: intentionally disabled (`open-pull-requests-limit: 0`); update manually.

## Building

- `./gradlew :jarhc:build` — compile, unit tests, reports, artifacts (this is what CI runs).
- Requires Java 17 to run Gradle.
- `jarhc-release-tests` needs Docker; it is not part of the default build.
- `:sonar` needs a token (`SONAR_TOKEN` env var or `sonar.token` system property). It runs automatically on CI for pushes (not Dependabot branches); run it locally (with a token in your Gradle settings) only for a reason such as a Sonar plugin update, a Sonar config change, or checking Sonar compatibility with a new Java or Gradle version.

## GitHub Actions

- Pin every action by full commit SHA with a `# vX.Y.Z` comment.
- Keep `cache-provider: basic` on `gradle/actions` v6+ to keep the free GitHub Actions cache (v6 made the default caching a commercial component).

## Documentation

The documentation under `website/docs/` is published with MkDocs (Material theme).
The pages under `website/docs/reports/` describe the sections of a JarHC report.
[website/docs/reports/jar-files.md](website/docs/reports/jar-files.md) is the reference example
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
- The `sources` and `last_reviewed` fields are internal metadata: they are not
  rendered into the published HTML and are not shown to readers. The `description`
  field described next is the exception, it is published.

### Page description (front matter)

Each page should also carry a `description` in its front matter, alongside the
source-tracking fields:

```yaml
---
description: The JarHC JAR Files report, an overview of every JAR file on the classpath, including size, class count, Java version, and checksums.
sources:
  - jarhc/src/main/java/org/jarhc/analyzer/JarFilesAnalyzer.java
last_reviewed: 2026-06-25
---
```

Unlike `sources` and `last_reviewed`, this field is published. MkDocs renders it
into the page's `<meta name="description">` tag, which search engines use for the
result snippet and which appears in link previews. Pages without it fall back to
the site-wide `site_description` from `mkdocs.yml`.

- Write one concise sentence (roughly 150 characters) stating what the page
  covers, phrased for someone reading a search result. Follow the same tone rules
  as the page body (precise and professional, no em dashes or other stylistic
  tells), and keep each page's description distinct so every snippet is meaningful.
- Do not use double quotes in the value. MkDocs does not escape them, so a `"`
  would close the HTML attribute early and break the tag. Use single quotes if you
  need to quote a term, for example `'JAR hell'`.

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

## Categories

Shared terms, used both as commit message prefixes and as branch name categories:

- `project` — project setup: dependency and tooling upgrades, version changes, build configuration
- `feature` — add a new feature
- `bugfix` — fix a bug
- `code` — refactor existing code
- `tests` — add or change tests
- `docs` — update documentation
- `ci` — continuous integration, such as GitHub Actions workflows
- `release` — release activities
- `hotfix` — urgent fix

## Commit messages

- Always start the subject with a category prefix: a term from the list above, capitalized, followed by a colon. Example: "Project: Upgrade SonarQube plugin".
- After the prefix, use the imperative mood with a capitalized first word.
- Combine closely related changes as separate sentences in the subject.
- Add a body (after a blank line) only when the change needs a rationale; state the why, not the diff.

## Branch names

- `<category>/<kebab-case-description>`. Example: `project/upgrade-gradle`.
- Leave `dependabot/...` branches to Dependabot.

## Pull request names

- Same as the branch name.

## Handling Copilot comments

- Do not apply Copilot's suggestions automatically.
- Review each comment, plus any "comments suppressed due to low confidence" note, and decide whether it is worth fixing or a false positive.
- Propose how a fix would look and wait for the user's approval before changing anything.
- Once approved: apply the fix, run the build to test it, commit and push, then reply to the comment and mark the thread resolved.
