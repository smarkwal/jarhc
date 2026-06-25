# AGENTS.md

Conventions for working in the JarHC repository.

## Documentation

The documentation under `docs/` is published with MkDocs (Material theme).
The pages under `docs/reports/` describe the sections of a JarHC report.
[docs/reports/jar-files.md](docs/reports/jar-files.md) is the reference example
for the style described below.

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
6. The screenshot link to the example report.
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
