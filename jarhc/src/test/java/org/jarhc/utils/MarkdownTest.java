/*
 * Copyright 2025 Stephan Markwalder
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MarkdownTest {

	@Test
	void toHTML() {

		assertNull(Markdown.toHtml(null));
		assertEquals("", Markdown.toHtml(""));
		assertEquals("&nbsp;&nbsp;&nbsp;Indented with tab.", Markdown.toHtml("\tIndented with tab."));
		assertEquals("This is <strong>bold</strong>!", Markdown.toHtml("This is " + Markdown.bold("bold") + "!"));
		assertEquals("This is <code>code</code>.", Markdown.toHtml("This is " + Markdown.code("code") + "."));
		assertEquals("Line 1<br>Line2<br>&nbsp;&nbsp;&nbsp;Line 3", Markdown.toHtml("Line 1\nLine2\n   Line 3"));
		assertEquals("<a href=\"https://jarhc.org\" target=\"_blank\" rel=\"noopener noreferrer\">JarHC</a>", Markdown.toHtml(Markdown.link("JarHC", "https://jarhc.org")));
		assertEquals("<a href=\"https://central.sonatype.com/artifact/org.jarhc/jarhc/2.0.0\" target=\"_blank\" rel=\"noopener noreferrer\">2.0.0</a>", Markdown.toHtml(Markdown.link("2.0.0", "org.jarhc:jarhc:2.0.0")));
		assertEquals("<a href=\"https://central.sonatype.com/artifact/org.jarhc/jarhc/2.0.0\" target=\"_blank\" rel=\"noopener noreferrer\">org.jarhc:jarhc:2.0.0</a>", Markdown.toHtml(Markdown.link("org.jarhc:jarhc:2.0.0")));

	}

}
