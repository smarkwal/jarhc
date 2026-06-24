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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.artifacts.Vulnerability;
import org.jarhc.artifacts.VulnerabilityFinder;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.Markdown;
import org.jarhc.utils.StringUtils;
import org.slf4j.Logger;

/**
 * Analyzer reporting known security vulnerabilities for the artifacts on the
 * classpath, based on data from a {@link VulnerabilityFinder}.
 * <p>
 * The report contains one row per vulnerability, listing all affected artifacts.
 */
public class VulnerabilitiesAnalyzer implements Analyzer {

	private static final Pattern CVE_PATTERN = Pattern.compile("CVE-(\\d{4})-(\\d+)");

	// base URL for CVE details in the NVD (National Vulnerability Database)
	private static final String NVD_URL = "https://nvd.nist.gov/vuln/detail/";

	// descriptions longer than this are truncated (with a "more" indicator appended)
	private static final int MAX_DESCRIPTION_LENGTH = 128;

	private final VulnerabilityFinder vulnerabilityFinder;
	private final Logger logger;

	public VulnerabilitiesAnalyzer(VulnerabilityFinder vulnerabilityFinder, Logger logger) {
		if (vulnerabilityFinder == null) throw new IllegalArgumentException("vulnerabilityFinder");
		this.vulnerabilityFinder = vulnerabilityFinder;
		this.logger = logger;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Vulnerabilities", "Known security vulnerabilities found in the artifacts.");
		section.addTable(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		// collect affected artifacts per vulnerability
		Map<Vulnerability, TreeSet<String>> artifactsByVulnerability = collectVulnerabilities(classpath);

		// rows are ordered by the CVE comparator on the first ("CVE") column
		ReportTable table = new ReportTable("CVE", "Artifacts", "Severity", "Description", "Advisory")
				.withRowComparator(CveComparator.INSTANCE);
		for (Map.Entry<Vulnerability, TreeSet<String>> entry : artifactsByVulnerability.entrySet()) {
			Vulnerability vulnerability = entry.getKey();
			TreeSet<String> artifacts = entry.getValue();
			table.addRow(
					getCveInfo(vulnerability),
					StringUtils.joinLines(artifacts),
					getSeverityInfo(vulnerability),
					getDescription(vulnerability),
					getAdvisoryLink(vulnerability)
			);
		}
		return table;
	}

	private Map<Vulnerability, TreeSet<String>> collectVulnerabilities(Classpath classpath) {

		// use a sorted map keyed by advisory ID so identical advisories merge,
		// independent of object identity
		Map<Vulnerability, TreeSet<String>> result = new LinkedHashMap<>();

		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			Artifact artifact = getArtifact(jarFile);
			if (artifact == null) {
				// coordinates unknown (or lookup failed): cannot query vulnerabilities
				continue;
			}

			List<Vulnerability> vulnerabilities;
			try {
				vulnerabilities = vulnerabilityFinder.findVulnerabilities(artifact);
			} catch (RepositoryException e) {
				logger.error("Vulnerability lookup error for artifact: {}", artifact.toCoordinates(), e);
				continue;
			}

			for (Vulnerability vulnerability : vulnerabilities) {
				result.computeIfAbsent(vulnerability, v -> new TreeSet<>(StringUtils.SMART_ORDER))
						.add(jarFile.getDisplayName());
			}
		}

		return result;
	}

	/**
	 * Get the resolved Maven artifact for the given JAR file, or <code>null</code>
	 * if the coordinates are unknown.
	 */
	private static Artifact getArtifact(JarFile jarFile) {

		// prefer artifact coordinates given as command line argument
		String coordinates = jarFile.getCoordinates();
		if (coordinates != null) {
			return new Artifact(coordinates);
		}

		List<Artifact> artifacts = jarFile.getArtifacts();
		if (artifacts == null || artifacts.isEmpty()) {
			return null;
		}
		// use only the "primary" artifact
		return artifacts.get(0);
	}

	private static String getCveInfo(Vulnerability vulnerability) {
		List<String> aliases = vulnerability.getCveAliases();
		if (aliases.isEmpty()) {
			// no CVE assigned (yet): fall back to the advisory ID
			return vulnerability.getAdvisoryId();
		}
		return aliases.stream().map(VulnerabilitiesAnalyzer::toCveLink).collect(StringUtils.joinLines());
	}

	/**
	 * Render a CVE identifier as a link to its NVD detail page. Aliases that are
	 * not CVE identifiers are returned unchanged.
	 */
	private static String toCveLink(String alias) {
		if (CVE_PATTERN.matcher(alias).matches()) {
			return Markdown.link(alias, NVD_URL + alias);
		}
		return alias;
	}

	private static String getSeverityInfo(Vulnerability vulnerability) {
		if (!vulnerability.isScored()) {
			return Markdown.UNKNOWN;
		}
		return String.format("%.1f %s", vulnerability.getCvss3Score(), getSeverityLabel(vulnerability));
	}

	private static String getSeverityLabel(Vulnerability vulnerability) {
		switch (vulnerability.getSeverity()) {
			case CRITICAL:
				return "Critical";
			case HIGH:
				return "High";
			case MEDIUM:
				return "Medium";
			case LOW:
				return "Low";
			case NONE:
				return "None";
			default:
				return Markdown.UNKNOWN;
		}
	}

	private static String getDescription(Vulnerability vulnerability) {
		String title = vulnerability.getTitle();
		if (title == null) {
			return "";
		}
		if (title.length() > MAX_DESCRIPTION_LENGTH) {
			// truncate and append a "more" indicator (total length stays at MAX_DESCRIPTION_LENGTH)
			String suffix = " " + Markdown.MORE;
			return title.substring(0, MAX_DESCRIPTION_LENGTH - suffix.length()) + suffix;
		}
		return title;
	}

	private static String getAdvisoryLink(Vulnerability vulnerability) {
		String advisoryId = vulnerability.getAdvisoryId();
		String url = vulnerability.getUrl();
		if (url == null || url.isEmpty()) {
			return advisoryId;
		}
		return Markdown.link(advisoryId, url);
	}

	/**
	 * Orders the rows of the vulnerabilities table by the value of the first
	 * ("CVE") column:
	 * <ul>
	 * <li>Rows without a CVE come first (assumed to be very new advisories),
	 * sorted alphabetically by their value (the advisory ID).</li>
	 * <li>Rows with a CVE follow, sorted by year (newest first) and then by the
	 * sequence number (numerically, descending).</li>
	 * </ul>
	 * The first CVE found in the value is used as the sort key, so a value
	 * listing multiple CVEs is sorted by its first (top) one.
	 */
	static class CveComparator implements Comparator<String> {

		static final CveComparator INSTANCE = new CveComparator();

		@Override
		public int compare(String value1, String value2) {

			int[] cve1 = parseCve(value1);
			int[] cve2 = parseCve(value2);

			boolean hasCve1 = cve1 != null;
			boolean hasCve2 = cve2 != null;

			// rows without a CVE come first, sorted alphabetically
			if (!hasCve1 && !hasCve2) {
				return value1.compareTo(value2);
			}
			if (!hasCve1) return -1;
			if (!hasCve2) return 1;

			// both have a CVE: sort by year (newest first), then by sequence number (descending)
			if (cve1[0] != cve2[0]) {
				return Integer.compare(cve2[0], cve1[0]);
			}
			return Integer.compare(cve2[1], cve1[1]);
		}

		/**
		 * Parse the first CVE found in the given value into <code>[year, number]</code>,
		 * or return <code>null</code> if it contains no CVE.
		 */
		private static int[] parseCve(String value) {
			Matcher matcher = CVE_PATTERN.matcher(value);
			if (matcher.find()) {
				return new int[] { Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) };
			}
			return null;
		}

	}

}
