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

	private static final Pattern CODE = Pattern.compile("`([^`]+)`");
	private static final Pattern ARTIFACT_LINK = Pattern.compile("\\[\\[([^]]+)]]");
	private static final Pattern URL_LINK = Pattern.compile("\\[([^]]+)]\\(([^)]+)\\)");

	// TODO: make this configurable?
	private static final String ARTIFACT_URL = "https://central.sonatype.com/artifact/%s/%s/%s"; // alternative: "https://mvnrepository.com/artifact/%s/%s/%s"

	public static String code(String text) {
		if (text == null || text.isEmpty()) return text;
		return "`" + text + "`";
	}

	public static String link(String text) {
		if (text == null || text.isEmpty()) return text;
		return "[[" + text + "]]";
	}

	public static String link(String text, String url) {
		if (text == null || text.isEmpty()) return text;
		return "[" + text + "](" + url + ")";
	}

	public static String toText(String text) {
		if (text == null || text.isEmpty()) return text;
		text = CODE.matcher(text).replaceAll("$1");
		text = ARTIFACT_LINK.matcher(text).replaceAll("$1");
		text = URL_LINK.matcher(text).replaceAll("$1");
		return text;
	}

	public static String toHtml(String text) {
		if (text == null || text.isEmpty()) return text;
		text = CODE.matcher(text).replaceAll("<code>$1</code>");
		text = renderArtifactLinks(text);
		text = renderUrlLinks(text);
		return text;
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
		Artifact artifact = new Artifact(coordinates);
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		String url = String.format(ARTIFACT_URL, groupId, artifactId, version);
		return createLink(coordinates, url);
	}

	private static String createUrlLink(Matcher matcher) {
		String label = matcher.group(1);
		String url = matcher.group(2);
		return createLink(label, url);
	}

	private static String createLink(String label, String url) {
		String target = DigestUtils.sha1Hex(label);
		return String.format("<a href=\"%s\" target=\"%s\" rel=\"noopener\">%s</a>", url, target, label);
	}

}
