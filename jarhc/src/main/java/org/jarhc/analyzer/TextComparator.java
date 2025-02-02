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

package org.jarhc.analyzer;

import java.util.Comparator;
import org.jarhc.utils.Markdown;

/**
 * Comparator for text values, ignoring Markdown formatting.
 */
public final class TextComparator implements Comparator<String> {

	public static final TextComparator INSTANCE = new TextComparator();

	private TextComparator() {
	}

	@Override
	public int compare(String value1, String value2) {
		String text1 = Markdown.toText(value1);
		String text2 = Markdown.toText(value2);
		return text1.compareTo(text2);
	}

}
