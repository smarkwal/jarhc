/*
 * Copyright 2026 Stephan Markwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jarhc.analyzer;

import static org.jarhc.TestUtils.assertValuesEquals;
import static org.jarhc.utils.StringUtils.joinLines;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.artifacts.Vulnerability;
import org.jarhc.artifacts.VulnerabilityFinder;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class VulnerabilitiesAnalyzerTest {

	private final VulnerabilityFinder finder = mock(VulnerabilityFinder.class);
	private final Logger logger = LoggerBuilder.collect(VulnerabilitiesAnalyzer.class);
	private final VulnerabilitiesAnalyzer analyzer = new VulnerabilitiesAnalyzer(finder, logger);

	private static final Vulnerability LOG4SHELL = new Vulnerability("GHSA-jfh8-c2jp-5v3q", List.of("CVE-2021-44228"), 10.0, "CVSS:3.1/AV:N", "Remote code injection in Log4j", "https://osv.dev/GHSA-jfh8-c2jp-5v3q");
	private static final Vulnerability INCOMPLETE_FIX = new Vulnerability("GHSA-7rjr-3q55-vv33", List.of("CVE-2021-45046"), 9.0, "CVSS:3.1/AV:N", "Incomplete fix", "https://osv.dev/GHSA-7rjr-3q55-vv33");
	// SHARED has both a CVE alias and a secondary non-CVE alias to exercise the column split
	private static final Vulnerability SHARED = new Vulnerability("GHSA-shared", List.of("CVE-2020-1234", "GHSA-secondary"), 5.0, "CVSS:3.1/AV:L", "Shared issue", "https://osv.dev/GHSA-shared");
	private static final Vulnerability NO_CVE = new Vulnerability("GHSA-new0", List.of(), null, null, "Very new issue", "https://osv.dev/GHSA-new0");

	@Test
	void analyze_sortsByCveAndAggregatesArtifacts() throws RepositoryException {

		// prepare: libA has 3 vulnerabilities, libB has the shared one + a no-CVE one
		Artifact libA = new Artifact("group:libA:1.0:jar");
		Artifact libB = new Artifact("group:libB:2.0:jar");
		doReturn(List.of(LOG4SHELL, INCOMPLETE_FIX, SHARED)).when(finder).findVulnerabilities(libA);
		doReturn(List.of(SHARED, NO_CVE)).when(finder).findVulnerabilities(libB);

		Classpath classpath = classpath(
				JarFile.withName("libA.jar").withArtifact("group:libA:1.0:jar").build(),
				JarFile.withName("libB.jar").withArtifact("group:libB:2.0:jar").build()
		);

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		List<String[]> rows = assertTable(section);
		assertEquals(4, rows.size());

		// order: no-CVE first ([unknown]), then year desc (2021 before 2020), then sequence desc (45046 before 44228)
		// the CVE column links to the NVD detail page; advisory column shows the advisory link
		assertValuesEquals(rows.get(0), "[unknown]", "libB", "[unknown]", "Very new issue", "[GHSA-new0](https://osv.dev/GHSA-new0)");
		assertValuesEquals(rows.get(1), "[CVE-2021-45046](https://nvd.nist.gov/vuln/detail/CVE-2021-45046)", "libA", "9.0 Critical", "Incomplete fix", "[GHSA-7rjr-3q55-vv33](https://osv.dev/GHSA-7rjr-3q55-vv33)");
		assertValuesEquals(rows.get(2), "[CVE-2021-44228](https://nvd.nist.gov/vuln/detail/CVE-2021-44228)", "libA", "10.0 Critical", "Remote code injection in Log4j", "[GHSA-jfh8-c2jp-5v3q](https://osv.dev/GHSA-jfh8-c2jp-5v3q)");
		// SHARED: the CVE alias goes into the CVE column, the secondary GHSA alias is appended in the advisory column
		assertValuesEquals(rows.get(3), "[CVE-2020-1234](https://nvd.nist.gov/vuln/detail/CVE-2020-1234)", joinLines("libA", "libB"), "5.0 Medium", "Shared issue", joinLines("[GHSA-shared](https://osv.dev/GHSA-shared)", "GHSA-secondary"));
	}

	@Test
	void cveComparator_sortsByYearThenSequenceDescending() {
		VulnerabilitiesAnalyzer.CveComparator comparator = VulnerabilitiesAnalyzer.CveComparator.INSTANCE;

		// [unknown] (no-CVE) comes before any CVE
		assertNegative(comparator.compare("[unknown]", "CVE-2026-1"));
		// any value without a CVE pattern sorts before any CVE (general rule)
		assertNegative(comparator.compare("GHSA-aaa", "CVE-2026-1"));

		// newer year first
		assertNegative(comparator.compare("CVE-2026-1", "CVE-2021-99999"));

		// within a year, higher sequence number first
		assertNegative(comparator.compare("CVE-2021-45046", "CVE-2021-44228"));

		// numeric (not lexical) comparison of the sequence number
		assertNegative(comparator.compare("CVE-2021-100", "CVE-2021-99"));

		// also works when the CVE is wrapped in a Markdown link (as rendered in the report)
		assertNegative(comparator.compare(
				"[CVE-2021-45046](https://nvd.nist.gov/vuln/detail/CVE-2021-45046)",
				"[CVE-2021-44228](https://nvd.nist.gov/vuln/detail/CVE-2021-44228)"));
	}

	@Test
	void analyze_truncatesLongDescription() throws RepositoryException {

		String longTitle = "A".repeat(250);
		Vulnerability vulnerability = new Vulnerability("GHSA-long", List.of("CVE-2021-1"), 5.0, "v", longTitle, "https://osv.dev/GHSA-long");

		Artifact lib = new Artifact("group:lib:1.0:jar");
		doReturn(List.of(vulnerability)).when(finder).findVulnerabilities(lib);

		Classpath classpath = classpath(
				JarFile.withName("lib.jar").withArtifact("group:lib:1.0:jar").build()
		);

		ReportSection section = analyzer.analyze(classpath);

		List<String[]> rows = assertTable(section);
		assertEquals(1, rows.size());

		String description = rows.get(0)[3];
		assertEquals(128, description.length());
		assertEquals("A".repeat(122) + " [...]", description);
	}

	@Test
	void analyze_skipsArtifactsWithUnknownCoordinates() {

		Classpath classpath = classpath(
				JarFile.withName("unknown.jar").withArtifacts(List.of()).build()
		);

		ReportSection section = analyzer.analyze(classpath);

		List<String[]> rows = assertTable(section);
		assertEquals(0, rows.size());
	}

	@Test
	void analyze_logsErrorOnLookupFailure() throws RepositoryException {

		Artifact lib = new Artifact("group:lib:1.0:jar");
		doThrow(new RepositoryException("boom")).when(finder).findVulnerabilities(lib);

		Classpath classpath = classpath(
				JarFile.withName("lib.jar").withArtifact("group:lib:1.0:jar").build()
		);

		ReportSection section = analyzer.analyze(classpath);

		List<String[]> rows = assertTable(section);
		assertEquals(0, rows.size());
	}

	private static Classpath classpath(JarFile... jarFiles) {
		List<JarFile> list = new ArrayList<>(List.of(jarFiles));
		return new Classpath(list, null, ClassLoaderStrategy.ParentLast);
	}

	private static List<String[]> assertTable(ReportSection section) {
		assertNotNull(section);
		assertEquals("Vulnerabilities", section.getTitle());

		List<Object> contents = section.getContent();
		assertEquals(1, contents.size());
		Object content = contents.get(0);
		assertInstanceOf(ReportTable.class, content);
		ReportTable table = (ReportTable) content;

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "CVE", "Artifacts", "Severity", "Description", "Advisory");

		// the report rows are ordered by Application via sortRows(); do the same here
		table.sortRows();
		return table.getRows();
	}

	private static void assertNegative(int value) {
		org.junit.jupiter.api.Assertions.assertTrue(value < 0, "expected negative value but was " + value);
	}

}
