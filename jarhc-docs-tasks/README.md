# jarhc-docs-tasks

Build tooling for the JarHC documentation. This subproject is **not** part of
the published JarHC artifacts; it only contains tasks that help maintain the
documentation under [`website/docs/`](../website/docs).

## Generate report section screenshots

The documentation pages under [`website/docs/reports/`](../website/docs/reports) embed
screenshots of the individual sections of an example HTML report. These images
are generated from [`website/docs/examples/asm/report.html`](../website/docs/examples/asm/report.html)
instead of being captured manually.

```sh
./gradlew :jarhc-docs-tasks:generateDocScreenshots
```

This renders the example report in a headless browser (Chromium, managed by
[Playwright](https://playwright.dev/java/)) and writes one PNG per report
section to [`website/docs/assets/images/`](../website/docs/assets/images), e.g.
`report-section-jar-files.png`. Each screenshot is cropped tightly to the
section's content (title, description and tables). Images are rendered at 1x
scale, since wide sections are scaled down to the content column width when
embedded in the documentation anyway.

The mapping from report section to image file name is defined in
[`GenerateReportScreenshots.java`](src/main/java/org/jarhc/docs/GenerateReportScreenshots.java).
When a new report section is added, add an entry there.

### Prerequisites

Only a JDK is required (the same one used to build JarHC) — Playwright and its
browser are fetched automatically:

* The Playwright library is declared in the
  [version catalog](../gradle/libs.versions.toml) and resolved by Gradle.
* On the **first** run, Playwright downloads its browser into a shared cache
  (`~/.cache/ms-playwright` / `~/Library/Caches/ms-playwright`). This step needs
  network access; subsequent runs are offline.
