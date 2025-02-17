/*
 * Copyright 2024 Stephan Markwalder
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

package org.jarhc.utils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jarhc.artifacts.Artifact;

public class Markdown {

	// special labels which will be colored in HTML rendering
	public static final String ERROR = "[error]";
	public static final String UNKNOWN = "[unknown]";
	public static final String NONE = "[none]";
	public static final String MORE = "[...]";

	@SuppressWarnings("UnnecessaryUnicodeEscape")
	public static final String BULLET = "\u2022";

	// Regex for parsing Markdown syntax
	private static final Pattern CODE = Pattern.compile("`([^`]+)`");
	private static final Pattern BOLD = Pattern.compile("\\*\\*([^*]+)\\*\\*");
	private static final Pattern ARTIFACT_LINK = Pattern.compile("\\[\\[([^]]+)]]");
	private static final Pattern URL_LINK = Pattern.compile("\\[([^]]+)]\\(([^)]+)\\)");
	private static final Pattern INSERTED = Pattern.compile("\\+\\+\\+\\{([^}]+)}\\+\\+\\+");
	private static final Pattern DELETED = Pattern.compile("---\\{([^}]+)}---");

	// HTML code snippets with placeholders
	private static final String HTML_CODE = "<code>$1</code>";
	private static final String HTML_BOLD = "<strong>$1</strong>";
	private static final String HTML_LINK = "<a href=\"%s\" target=\"_blank\" rel=\"noopener noreferrer\">%s</a>";
	private static final String HTML_INSERTED = "<span class=\"inserted\">$1</span>";
	private static final String HTML_DELETED = "<span class=\"deleted\">$1</span>";

	// URL for artifact links
	// alternative: "https://mvnrepository.com/artifact/%s/%s/%s"
	// TODO: make this configurable?
	private static final String ARTIFACT_URL = "https://central.sonatype.com/artifact/%s/%s/%s";

	public static String code(String text) {
		if (text == null || text.isEmpty()) return text;
		return "`" + text + "`";
	}

	public static String bold(String text) {
		if (text == null || text.isEmpty()) return text;
		return "**" + text + "**";
	}

	public static String link(String text) {
		if (text == null || text.isEmpty()) return text;
		return "[[" + text + "]]";
	}

	public static String link(String text, String url) {
		if (text == null || text.isEmpty()) return text;
		return "[" + text + "](" + url + ")";
	}

	public static String inserted(String text) {
		if (text == null || text.isEmpty()) return text;
		return "+++{" + text + "}+++";
	}

	public static String deleted(String text) {
		if (text == null || text.isEmpty()) return text;
		return "---{" + text + "}---";
	}

	// Text rendering ---------------------------------------------------------

	public static String toText(String text) {
		if (text == null || text.isEmpty()) return text;
		text = CODE.matcher(text).replaceAll("$1");
		text = BOLD.matcher(text).replaceAll("$1");
		text = ARTIFACT_LINK.matcher(text).replaceAll("$1");
		text = URL_LINK.matcher(text).replaceAll("$1");
		text = text.replace("\t", "   ");
		return text;
	}

	// HTML rendering ---------------------------------------------------------

	public static String toHtml(String text) {
		if (text == null || text.isEmpty()) return text;
		text = renderCode(text);
		text = renderBold(text);
		text = renderArtifactLinks(text);
		text = renderUrlLinks(text);
		text = renderLabels(text);
		text = INSERTED.matcher(text).replaceAll(HTML_INSERTED);
		text = DELETED.matcher(text).replaceAll(HTML_DELETED);
		text = text.replace("\t", "&nbsp;&nbsp;&nbsp;");
		text = text.replace("\n", "<br>");
		text = text.replace("\r", "");
		// TODO: convert leading spaces to non-breaking spaces?
		return text;
	}

	private static String renderLabels(String text) {
		text = text.replace(ERROR, "<span style=\"color:red\">[error]</span>");
		text = text.replace(UNKNOWN, "<span style=\"color:orange\">[unknown]</span>");
		text = text.replace(NONE, "<span style=\"color:gray\">[none]</span>");
		text = text.replace(MORE, "<span style=\"color:gray\">[...]</span>");
		return text;
	}

	private static String renderCode(String text) {
		return CODE.matcher(text).replaceAll(HTML_CODE);
	}

	private static String renderBold(String text) {
		return BOLD.matcher(text).replaceAll(HTML_BOLD);
	}

	private static String renderArtifactLinks(String text) {
		return renderLinks(text, ARTIFACT_LINK, Markdown::createArtifactLink);
	}

	private static String renderUrlLinks(String text) {
		return renderLinks(text, URL_LINK, Markdown::createUrlLink);
	}

	private static String renderLinks(String text, Pattern pattern, Function<Matcher, String> creator) {
		Matcher matcher = pattern.matcher(text);

		StringBuilder buffer = null; // lazy initialization
		while (matcher.find()) {
			if (buffer == null) {
				buffer = new StringBuilder(text.length() + 64);
			}

			String replacement = creator.apply(matcher);
			matcher.appendReplacement(buffer, replacement);
		}
		if (buffer == null) {
			return text;
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	private static String createArtifactLink(Matcher matcher) {
		String coordinates = matcher.group(1);
		String url = createUrl(coordinates);
		return createLink(coordinates, url);
	}

	private static String createUrlLink(Matcher matcher) {
		String label = matcher.group(1);
		String url = matcher.group(2);
		// special case: URL is coordinates
		// example: [1.2.3](org.example:artifact:1.2.3)
		if (Artifact.validateCoordinates(url)) {
			url = createUrl(url);
		}
		return createLink(label, url);
	}

	private static String createUrl(String coordinates) {
		Artifact artifact = new Artifact(coordinates);
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		return String.format(ARTIFACT_URL, groupId, artifactId, version);
	}

	private static String createLink(String label, String url) {
		return String.format(HTML_LINK, url, label);
	}

}
