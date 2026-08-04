---
name: jarhc-release
description: Guide and support the maintainer through releasing a new version of JarHC to GitHub and Maven Central. Use this whenever the user wants to cut, prepare, publish, or ship a new JarHC release, bump the version for a release, draft release notes for a new version, or asks "how do I release JarHC". Drives the multi-step release process, runs the scriptable steps, and stops at the manual/external gates (Sonatype OSSRH, GPG signing) for the user to perform.
---

# JarHC Release

Release a new version of JarHC to **GitHub** and **Maven Central**. This is a
**guided** process, not a fully automated one: you run and verify the steps that
can be scripted, and you **stop at the manual gates** (publishing in the Sonatype
OSSRH web UI, anything requiring credentials, GPG signing) so the maintainer can
do them. Never invent credentials, and never attempt to bypass a manual gate.

Work through the phases in order. After each phase, briefly report what happened
and confirm before moving on. If a verification step fails, stop and surface the
failure with its output rather than pushing ahead.

## Version numbers

Four version values appear throughout this skill. Do not hard-code them — derive
them at the start and substitute the placeholders consistently:

| Placeholder        | Role                        | Example          | How to determine it                                 |
|--------------------|-----------------------------|------------------|-----------------------------------------------------|
| `CURRENT_SNAPSHOT` | Current development version | `3.0.1-SNAPSHOT` | read `version` from `gradle.properties`             |
| `NEW_VERSION`      | New official release        | `3.0.1`          | `CURRENT_SNAPSHOT` without `-SNAPSHOT`, or ask      |
| `NEXT_VERSION`     | Next development version    | `3.0.2-SNAPSHOT` | bump `NEW_VERSION` (usually the patch) + `-SNAPSHOT`|
| `PREV_TAG`         | Previous release (git tag)  | `v3.0.0`         | latest `vX.Y.Z` from `git tag --sort=-v:refname`    |

The first three are bare version strings; `PREV_TAG` is a **git tag** (prefixed
with `v`). The release being cut is tagged `v${NEW_VERSION}` (see Phases 5 and 8).

Read `CURRENT_SNAPSHOT` from `gradle.properties`, then confirm `NEW_VERSION` and
`NEXT_VERSION` with the user (e.g. `3.0.1-SNAPSHOT` → release `3.0.1` → next
`3.0.2-SNAPSHOT`). As soon as the values are settled, define them as shell
variables and use the `${...}` form in the commands throughout this skill, so
each command is copy-paste ready and you never hand-substitute a version:

```shell
CURRENT_SNAPSHOT="3.0.1-SNAPSHOT"   # from gradle.properties
NEW_VERSION="3.0.1"                 # CURRENT_SNAPSHOT without -SNAPSHOT
NEXT_VERSION="3.0.2-SNAPSHOT"       # next development version
PREV_TAG="v3.0.0"                   # latest release tag
```

Replace the example values above with the real ones. Two caveats:

- **Shell variables do not persist** between separate command runs, and this
  process spans long waits (CI, Maven Central). Re-run the block above to
  re-establish the variables at the start of any new shell session before using
  them.
- **File edits are not shell.** Where a step changes a file (`gradle.properties`,
  `mkdocs.yml`), write the actual version value into the file — `${...}` only
  expands inside shell commands. The version placeholders shown in those file
  snippets stand for the value, not literal text to paste.

## Prerequisites

Verify these before starting; if any is missing, stop and tell the user.

- Clean working tree on `main`, up to date with `origin/main`
  (`git status`, `git fetch && git status`).
- **Sonatype OSSRH credentials** configured outside this repository (in
  `~/.gradle/gradle.properties` or the user's password manager). Do not read,
  echo, or store these.
- **GPG signing key** configured outside this repository and **not expired**.
- **Sonar token** available (environment variable or `~/.gradle/gradle.properties`).

You cannot check the secret values themselves (they live outside the repo) — just
remind the user to confirm they are present and current.

---

## Phase 1 — Pre-release preparation

1. Ask the user to review the latest SonarQube report and address relevant
   findings:
   ```shell
   open "https://sonarcloud.io/summary/overall?id=smarkwal_jarhc"
   ```
2. Set the release version in `gradle.properties` — this is the single source of
   truth for the version:
   ```properties
   version = NEW_VERSION
   ```

Do **not** grep-and-replace the version across the repo here. Most occurrences
of the old snapshot version live in generated files (test resources, example
reports) that are refreshed automatically in Phase 2; editing them by hand now
just creates churn that regeneration would overwrite. Any genuine leftover
references are caught by the verification in Phase 4, after regeneration.

## Phase 2 — Build & verify

`JAVA_HOME` must be set (the example-report and screenshot steps below rely on
it). Run the full build, all tests, and the release tests, regenerating test
resources as needed:

```shell
./gradlew clean :jarhc:build :jarhc-release-tests:test
```

Regenerate the generated artifacts that ship with the docs. Run them in the order
shown — the inline comments note the dependency between the steps:

```shell
# 1. example reports (produces docs/examples/asm/report.html, used below)
( cd docs/examples && ./example-reports.sh )

# 2. documentation screenshots (rendered from the asm example report)
./gradlew :jarhc-docs-tasks:generateDocScreenshots

# 3. JarHC's own report (independent of the two steps above)
./gradlew :jarhc:jarhcReport
```

If anything fails, stop and report it — do not continue to tagging.

## Phase 3 — Release notes

Draft the release notes from the commit history since the previous release, then
have the user review and edit them — the generated notes are always a draft.

1. Review every commit since the previous release. Open the compare view in the
   browser and/or inspect the history locally; where a commit message is unclear,
   look at the actual change:
   ```shell
   open "https://github.com/smarkwal/jarhc/compare/${PREV_TAG}...main"
   git log "${PREV_TAG}..HEAD" --oneline
   git diff "${PREV_TAG}..HEAD" -- <path>     # for a specific change
   ```
2. Create the release notes file:
   ```shell
   touch "docs/releases/v${NEW_VERSION}.md"
   ```
3. Write the notes into that file, following the structure of the existing files
   in `docs/releases/`: the title `# Version NEW_VERSION`, then a `### Highlights`
   section (each highlight a bold title + short paragraph), then `### Other
   changes` grouped by area (Application, Report, individual report sections, HTML
   report, JSON report, Code, ...). Focus on user-facing changes; omit purely
   internal refactorings, dependency bumps, and CI/test-only changes unless they
   affect users.
4. Add a navigation entry at the **top** of the `Release Notes` section in
   `mkdocs.yml` (newest version first):
   ```yaml
   - Release Notes:
       - Version NEW_VERSION: releases/vNEW_VERSION.md
       - Version PREV_VERSION: releases/vPREV_VERSION.md
       # ... older versions below
   ```
5. Validate the documentation build in strict mode (catches broken links,
   invalid anchors, and pages missing from the navigation):
   ```shell
   docker run --rm -v "$PWD":/docs squidfunk/mkdocs-material build --strict
   ```

## Phase 4 — Verify version, commit & CI

First verify the version was updated everywhere. The generated files (test
resources, example reports) were already refreshed in Phase 2, so search only
**tracked** files for any *remaining* reference to the old snapshot version:

```shell
git grep -nF "${CURRENT_SNAPSHOT}"
```

`git grep` searches only files tracked by git, so build output and anything in
`.gitignore` are ignored automatically — no manual exclude list needed. A clean
result (no output) is expected: `gradle.properties` now holds `NEW_VERSION`, and
the generated files were regenerated in Phase 2.

**If there is any hit, stop and ask the user how to proceed — do not silently fix
it.** A leftover reference to the old version usually means an earlier step did
not do all of its work (e.g. a generated file that was not regenerated), or that
the release process is missing a step. Hand-patching the file would hide that
signal. Show the user the hits and let them decide.

Then review all changes, and commit and push:

```shell
git commit -m "Project: Change version from ${CURRENT_SNAPSHOT} to ${NEW_VERSION}."
git push
```

**Gate:** wait for the GitHub **Build** workflow to finish successfully before
continuing — poll it from the terminal, or open it in the browser:

```shell
gh run list --workflow=build.yml
gh run watch
open "https://github.com/smarkwal/jarhc/actions/workflows/build.yml"
```

Then ask the user to confirm the post-commit SonarQube quality gate passes:

```shell
open "https://sonarcloud.io/summary/overall?id=smarkwal_jarhc"
```

## Phase 5 — Tag

Only after CI is green:

```shell
git tag "v${NEW_VERSION}"
git push origin "v${NEW_VERSION}"
```

## Phase 6 — Build release artifacts

Build the official artifacts once (tests already passed in Phases 2 and 4):

```shell
./gradlew clean :jarhc:build -Pskip.tests
```

This single local build is the source of truth for **both** Maven Central and
the GitHub Release. The next two phases reuse its output in `jarhc/build/libs/`
— do **not** run `clean` again until both are done, so identical bytes ship
everywhere.

## Phase 7 — Release to Maven Central  ⛔ manual gate

Publish the artifacts from Phase 6 to Sonatype OSSRH:

```shell
./gradlew :jarhc:publishToSonatype --info
```

Then **stop and hand off to the user** — these steps happen in the OSSRH web UI
and the agent must not attempt them:

1. Open the staging repository:
   ```shell
   open "https://oss.sonatype.org/#stagingRepositories"
   ```
2. Inspect the staged artifacts.
3. **Close** the staging repository and wait for validation to complete.
4. **Release** the artifacts to Maven Central.

Note: artifacts can take up to ~30 minutes to appear on Maven Central, and up to
~4 hours for the search index to update. Do not start Phase 8 until the user
confirms the Maven Central release succeeded.

## Phase 8 — Release to GitHub

Only **after** Maven Central has the artifacts (Phase 7). Create the release as a
**draft** first, attaching the jars from the same local build:

```shell
gh release create "v${NEW_VERSION}" --draft --title "v${NEW_VERSION}" \
  --notes-file "docs/releases/v${NEW_VERSION}.md" \
  "jarhc/build/libs/jarhc-${NEW_VERSION}.jar" \
  "jarhc/build/libs/jarhc-${NEW_VERSION}-app.jar"
```

The `jarhc-${NEW_VERSION}.jar` here is byte-identical to the one published to
Maven Central, so both channels match. After the user reviews the draft in the
GitHub web UI, publish it:

```shell
gh release edit "v${NEW_VERSION}" --draft=false
```

## Phase 9 — Verify availability

Confirm the new version is published (some pages lag — see the timing note in
Phase 7):

```shell
open "https://repo1.maven.org/maven2/org/jarhc/jarhc/"
open "https://mvnrepository.com/artifact/org.jarhc/jarhc"
open "https://central.sonatype.com/artifact/org.jarhc/jarhc/versions"
open "https://deps.dev/maven/org.jarhc%3Ajarhc"
```

## Phase 10 — Post-release

Bump to the next development snapshot in `gradle.properties`:

```properties
version = NEXT_VERSION
```

Rebuild, run the tests, and refresh the generated docs artifacts so everything
reflects the new snapshot version — the same steps as Phase 2, in the same order
(example reports before screenshots):

```shell
./gradlew clean :jarhc:build :jarhc-release-tests:test
( cd docs/examples && ./example-reports.sh )
./gradlew :jarhc-docs-tasks:generateDocScreenshots
./gradlew :jarhc:jarhcReport
```

Then verify no genuine reference to the just-released version is left behind
(again, tracked files only):

```shell
git grep -nF "${NEW_VERSION}"
```

A bare release version like `3.0.1` can appear legitimately — release notes,
changelogs, the new git tag in docs — and can also match as a substring of a
longer number (e.g. `3.0.10`). Scan the hits and update only the ones that should
have moved to the next snapshot; expected mentions of the released version are
fine to leave.

Review all uncommitted changes, then commit and push:

```shell
git commit -m "Project: Change version from ${NEW_VERSION} to ${NEXT_VERSION}."
git push
```

---

## Reference links

- Publishing guide: <https://central.sonatype.org/publish/publish-guide/>
- Generate user token: <https://central.sonatype.org/publish/generate-token/>
- Gradle signing plugin: <https://docs.gradle.org/current/userguide/signing_plugin.html>
- Nexus publish plugin: <https://github.com/gradle-nexus/publish-plugin/blob/master/README.md>

## Notes on scope and safety

- **Manual gates are intentional.** Sonatype close/release, GPG signing, and
  anything touching credentials are done by the user, not the agent.
- **Never handle secrets.** Credentials live outside the repository
  (`~/.gradle/gradle.properties`, env vars, a password manager). Do not read,
  print, copy, or commit them, and do not add them to any file in the repo.
- **One build, shipped twice.** Phases 6–8 reuse a single `jarhc/build/libs/`
  output so Maven Central and the GitHub Release are identical; don't rebuild
  between them.
- **Stop on failure.** If a build, test, CI, or doc-validation step fails, stop
  and report it rather than continuing the release.
