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

package org.jarhc.docs;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the report section screenshots used in the documentation by
 * rendering the example HTML report in a headless browser (Chromium, managed
 * by Playwright) and capturing each report section as a separate PNG image.
 * <p>
 * Run via the Gradle task {@code generateDocScreenshots}.
 */
public class GenerateReportScreenshots {

	/**
	 * Mapping from the HTML section id (the {@code id} attribute of the
	 * {@code <section class="report-section">} element) to the screenshot file
	 * name written into the images directory. The order defines the order in
	 * which the screenshots are generated.
	 */
	private static final Map<String, String> SECTIONS = new LinkedHashMap<>();

	static {
		SECTIONS.put("JARFiles", "report-section-jar-files.png");
		SECTIONS.put("Dependencies", "report-section-dependencies.png");
		SECTIONS.put("DuplicateClasses", "report-section-duplicate-classes.png");
		SECTIONS.put("BinaryCompatibility", "report-section-binary-compatibility.png");
		SECTIONS.put("Blacklist", "report-section-blacklist.png");
		SECTIONS.put("JARManifests", "report-section-jar-manifests.png");
		SECTIONS.put("JPMSModules", "report-section-jpms-modules.png");
		SECTIONS.put("OSGiBundles", "report-section-osgi-bundles.png");
		SECTIONS.put("JavaRuntime", "report-section-java-runtime.png");
	}

	// Render at a very generous width initially so that even the widest table
	// is laid out at its natural width (no wrapping); the viewport is then
	// shrunk to the actual content width.
	private static final int INITIAL_VIEWPORT_WIDTH = 4000;
	private static final int VIEWPORT_HEIGHT = 1400;
	private static final int MIN_VIEWPORT_WIDTH = 1280;

	// Capture at 1x scale: the screenshots are embedded in the documentation,
	// where wide images are scaled down to the content column width anyway. At
	// 1x, narrow sections (e.g. Java Runtime) are shown at their natural size
	// instead of being displayed twice as large as intended, which keeps the
	// on-page size difference between the smallest and the widest image small.
	private static final double DEVICE_SCALE_FACTOR = 1.0;

	// JavaScript that measures the tight content width of the whole report:
	// the rightmost edge of any table or text, across all sections.
	private static final String MEASURE_MAX_CONTENT_RIGHT =
			"() => {\n" +
			contentRightFn() +
			"  let right = 0;\n" +
			"  for (const s of document.querySelectorAll('section.report-section')) {\n" +
			"    right = Math.max(right, contentRight(s));\n" +
			"  }\n" +
			"  return Math.ceil(right);\n" +
			"}";

	// JavaScript that returns the tight bounding box (in page coordinates) of a
	// single section: full height, but width clipped to the actual content
	// (tables + text) rather than the full-width section box.
	private static final String MEASURE_SECTION_BOX =
			"(id) => {\n" +
			contentRightFn() +
			"  const s = document.getElementById(id);\n" +
			"  if (!s) return null;\n" +
			"  const sr = s.getBoundingClientRect();\n" +
			"  const right = contentRight(s);\n" +
			"  return {\n" +
			"    x: Math.floor(sr.left + window.scrollX),\n" +
			"    y: Math.floor(sr.top + window.scrollY),\n" +
			"    width: Math.ceil(right - sr.left + 4),\n" +
			"    height: Math.ceil(sr.height)\n" +
			"  };\n" +
			"}";

	/**
	 * Shared JavaScript helper {@code contentRight(section)}: returns the
	 * rightmost pixel (relative to the viewport) occupied by actual content in
	 * the given section. It considers tables (which are shrink-to-fit and may
	 * overflow the section box) and text runs (measured via Range rectangles),
	 * but ignores full-width block backgrounds such as the section title bar.
	 * It is declared inside each {@code page.evaluate(...)} function body so
	 * that every evaluated string is a single, self-contained function.
	 */
	private static String contentRightFn() {
		return "function contentRight(s) {\n" +
				"  let right = s.getBoundingClientRect().left;\n" +
				"  for (const t of s.querySelectorAll('table')) {\n" +
				"    right = Math.max(right, t.getBoundingClientRect().right);\n" +
				"  }\n" +
				"  const walker = document.createTreeWalker(s, NodeFilter.SHOW_TEXT);\n" +
				"  const range = document.createRange();\n" +
				"  let n;\n" +
				"  while ((n = walker.nextNode())) {\n" +
				"    if (!n.nodeValue.trim()) continue;\n" +
				"    range.selectNodeContents(n);\n" +
				"    for (const rc of range.getClientRects()) right = Math.max(right, rc.right);\n" +
				"  }\n" +
				"  return right;\n" +
				"}\n";
	}

	public static void main(String[] args) {

		if (args.length != 2) {
			System.err.println("Usage: GenerateReportScreenshots <report.html> <output-image-dir>");
			System.exit(1);
		}

		File reportFile = new File(args[0]);
		File outputDir = new File(args[1]);

		if (!reportFile.isFile()) {
			System.err.println("Report file not found: " + reportFile.getAbsolutePath());
			System.exit(1);
		}
		if (!outputDir.isDirectory()) {
			System.err.println("Output directory not found: " + outputDir.getAbsolutePath());
			System.exit(1);
		}

		String reportUrl = reportFile.toURI().toString();
		System.out.println("Rendering report: " + reportUrl);

		// sections mapped in SECTIONS but not found in the report
		List<String> missing = new ArrayList<>();
		// sections found in the report but not mapped in SECTIONS
		List<String> unknown = new ArrayList<>();
		int count = 0;

		try (Playwright playwright = Playwright.create();
				Browser browser = playwright.chromium().launch()) {
			// closing the browser also closes its contexts and pages
			BrowserContext context = browser.newContext(new Browser.NewContextOptions()
					.setViewportSize(INITIAL_VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
					.setDeviceScaleFactor(DEVICE_SCALE_FACTOR));
			Page page = context.newPage();

			page.navigate(reportUrl);
			page.waitForLoadState();

			// expand all sections that are collapsed by default so their full
			// content is visible in the screenshot
			page.evaluate("() => document.querySelectorAll('section.report-section.collapsed')"
					+ ".forEach(s => s.classList.remove('collapsed'))");

			// detect report sections that are not mapped in SECTIONS (e.g. a new
			// section was added to JarHC but not yet to this generator)
			@SuppressWarnings("unchecked")
			List<String> reportSectionIds = (List<String>) page.evaluate(
					"() => Array.from(document.querySelectorAll('section.report-section'), s => s.id)");
			for (String id : reportSectionIds) {
				if (!SECTIONS.containsKey(id)) {
					unknown.add(id);
				}
			}

			// Shrink the viewport width to the actual content width, so that the
			// fixed-width section title bars do not extend far beyond the
			// tables (which would add large empty margins to the screenshots).
			// Set the viewport height to the full page height, so that every
			// section is fully "in view" and can be captured via a clip region
			// (Page.screenshot with a clip only captures within the viewport).
			int contentWidth = ((Number) page.evaluate(MEASURE_MAX_CONTENT_RIGHT)).intValue();
			int viewportWidth = Math.max(MIN_VIEWPORT_WIDTH, contentWidth + 24);
			int pageHeight = ((Number) page.evaluate(
					"() => Math.ceil(document.documentElement.scrollHeight)")).intValue();
			page.setViewportSize(viewportWidth, Math.max(VIEWPORT_HEIGHT, pageHeight));

			for (Map.Entry<String, String> entry : SECTIONS.entrySet()) {
				String sectionId = entry.getKey();
				String fileName = entry.getValue();

				@SuppressWarnings("unchecked")
				Map<String, Object> box = (Map<String, Object>) page.evaluate(MEASURE_SECTION_BOX, sectionId);
				if (box == null) {
					missing.add(sectionId);
					continue;
				}

				Path outputPath = outputDir.toPath().resolve(fileName);
				page.screenshot(new Page.ScreenshotOptions()
						.setClip(
								((Number) box.get("x")).doubleValue(),
								((Number) box.get("y")).doubleValue(),
								((Number) box.get("width")).doubleValue(),
								((Number) box.get("height")).doubleValue())
						.setPath(outputPath));
				System.out.println("Wrote " + outputPath);
				count++;
			}

		}

		// Fail if the generator is out of sync with the report, in either
		// direction. This is checked after the browser has been closed by the
		// try-with-resources above, so that System.exit() does not bypass it.
		// For this controlled example report any mismatch is a genuine defect.
		if (!missing.isEmpty() || !unknown.isEmpty()) {
			if (!missing.isEmpty()) {
				System.err.println("ERROR: sections mapped in SECTIONS but not found in the report: " + missing);
			}
			if (!unknown.isEmpty()) {
				System.err.println("ERROR: sections found in the report but not mapped in SECTIONS: " + unknown);
			}
			System.err.println("Update the SECTIONS mapping in GenerateReportScreenshots"
					+ " (and the documentation) to match " + reportFile.getName() + ".");
			System.exit(1);
		}

		System.out.println("Done. Generated " + count + " screenshot(s).");
	}

}
