# Vulnerabilities

Reports known security vulnerabilities for the artifacts on the classpath.

For every artifact that can be identified by its Maven coordinates, JarHC looks
up known security advisories using the [deps.dev](https://deps.dev) API from
Google, which aggregates data from the [OSV](https://osv.dev) database.

The report contains one row per vulnerability, listing all affected artifacts:

* **CVE** &ndash; the CVE identifier(s) of the vulnerability, linking to the
  corresponding [NVD](https://nvd.nist.gov) detail page. For very recent
  advisories that have not yet been assigned a CVE, the advisory identifier
  (for example, a GHSA ID) is shown instead.
* **Artifacts** &ndash; all artifacts on the classpath affected by the vulnerability.
* **Severity** &ndash; the CVSS v3 base score and its qualitative rating
  (Critical, High, Medium, Low). Advisories that have not yet been scored are
  reported as `[unknown]`.
* **Description** &ndash; a short description of the vulnerability (truncated to
  128 characters).
* **Advisory** &ndash; a link to the advisory with further details.

Vulnerabilities are sorted by CVE: advisories without a CVE first, then by year
and by number (newest first).

Next: [Dependencies](dependencies.md)
