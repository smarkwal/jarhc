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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.sequence.CommandVisitor;
import org.apache.commons.collections4.sequence.SequencesComparator;

public class DiffUtils {

	private DiffUtils() {
		throw new IllegalStateException("utility class");
	}

	public static List<String> diff(List<String> lines1, List<String> lines2) {

		// TODO: implement a better strategy which takes into account
		//  the similarity of inserted and deleted lines,
		//  especially if they have a long common prefix.
		// Example:
		// - a.jar 1.0
		// + a.jar 1.1
		// - b.jar 1.0
		//   c.jar 2.0

		// TODO: do not depend on Markdown as output format

		// TODO: get rid of dependency on Apache Commons Collections
		SequencesComparator<String> comparator = new SequencesComparator<>(lines1, lines2);
		LineDiffVisitor visitor = new LineDiffVisitor();
		comparator.getScript().visit(visitor);
		return visitor.getLines();
	}

	private static class LineDiffVisitor implements CommandVisitor<String> {

		private final List<String> lines = new ArrayList<>();
		private final List<String> insertedBuffer = new ArrayList<>();

		@Override
		public void visitInsertCommand(String line) {
			insertedBuffer.add(Markdown.inserted(line));
		}

		@Override
		public void visitKeepCommand(String line) {
			emptyInsertedBuffer();
			lines.add(line);
		}

		@Override
		public void visitDeleteCommand(String line) {
			lines.add(Markdown.deleted(line));
		}

		public List<String> getLines() {
			emptyInsertedBuffer();
			return lines;
		}

		private void emptyInsertedBuffer() {
			lines.addAll(insertedBuffer);
			insertedBuffer.clear();
		}

	}

}
