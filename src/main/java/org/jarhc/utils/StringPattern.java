/*
 * Copyright 2022 Stephan Markwalder
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

import java.util.ArrayList;
import java.util.List;

public class StringPattern {

	private final Test[] tests;
	private final boolean caseInsensitive;

	public StringPattern(String pattern, boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;

		if (caseInsensitive) {
			pattern = pattern.toLowerCase();
		}

		List<Test> list = new ArrayList<>();

		if (pattern.contains("*")) {

			String[] parts = pattern.split("\\*");

			int start = 0;
			if (!pattern.startsWith("*")) {
				list.add(new StartsWith(parts[0]));
				start = 1;
			}

			int end = parts.length;
			if (!pattern.endsWith("*")) {
				end--;
			}

			for (int i = start; i < end; i++) {
				String part = parts[i];
				if (part.length() > 0) {
					list.add(new Contains(part));
				}
			}

			if (!pattern.endsWith("*")) {
				list.add(new EndsWith(parts[parts.length - 1]));
			}

		} else {

			list.add(new Equals(pattern));

		}

		this.tests = list.toArray(new Test[0]);
	}

	public boolean matches(String text) {
		String input = caseInsensitive ? text.toLowerCase() : text;
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < tests.length; i++) {
			Test test = tests[i];
			if (!test.matches(input)) {
				return false;
			}
		}
		return true;
	}

	abstract static class Test {

		protected final String pattern;

		protected Test(String pattern) {
			this.pattern = pattern;
		}

		abstract boolean matches(String text);

	}

	static class Equals extends Test {
		public Equals(String pattern) {
			super(pattern);
		}

		public boolean matches(String text) {
			return text.equals(pattern);
		}
	}

	static class StartsWith extends Test {
		public StartsWith(String pattern) {
			super(pattern);
		}

		public boolean matches(String text) {
			return text.startsWith(pattern);
		}
	}

	static class EndsWith extends Test {
		public EndsWith(String pattern) {
			super(pattern);
		}

		public boolean matches(String text) {
			return text.endsWith(pattern);
		}
	}

	static class Contains extends Test {
		public Contains(String pattern) {
			super(pattern);
		}

		public boolean matches(String text) {
			return text.contains(pattern);
		}
	}

}
