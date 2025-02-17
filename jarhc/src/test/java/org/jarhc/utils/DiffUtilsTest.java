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

import static org.jarhc.utils.Markdown.deleted;
import static org.jarhc.utils.Markdown.inserted;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DiffUtilsTest {

	@Test
	public void diff() {

		// prepare
		List<String> lines1 = List.of("a.jar 1.0", "b.jar 1.0", "x.jar 3.0");
		List<String> lines2 = List.of("a.jar 1.1", "c.jar 2.0", "x.jar 3.0");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of(deleted("a.jar 1.0"), inserted("a.jar 1.1"), deleted("b.jar 1.0"), inserted("c.jar 2.0"), "x.jar 3.0");
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_duplicateValuesAtStart() {

		// prepare: lists with duplicate values at start
		List<String> lines1 = List.of("a", "a", "a");
		List<String> lines2 = List.of("a", "a", "x");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of(deleted("a"), "a", "a", inserted("x"));
		// TODO: changes should stay together:
		//  List<String> expectedLines = List.of("a", "a", deleted("a"), inserted("x"));
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_duplicateValuesAtEnd() {

		// prepare
		List<String> lines1 = List.of("a", "a", "a");
		List<String> lines2 = List.of("x", "a", "a");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of(deleted("a"), inserted("x"), "a", "a");
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_keepSimilarValuesTogether() {

		// prepare
		List<String> lines1 = List.of("org.ow2.asm:asm:9.2 (runtime)", "org.json:json:20211205 (runtime)", "org.eclipse.aether:aether-impl:1.1.0 (runtime)");
		List<String> lines2 = List.of("org.slf4j:slf4j-api:2.0.16", "org.ow2.asm:asm:9.7 (runtime)", "org.json:json:20240303 (runtime)", "org.eclipse.aether:aether-impl:1.1.0 (runtime)");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of(
				deleted("org.ow2.asm:asm:9.2 (runtime)"),
				inserted("org.slf4j:slf4j-api:2.0.16"),
				inserted("org.ow2.asm:asm:9.7 (runtime)"),
				deleted("org.json:json:20211205 (runtime)"),
				inserted("org.json:json:20240303 (runtime)"),
				"org.eclipse.aether:aether-impl:1.1.0 (runtime)"
		);
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_sameLists() {

		// prepare
		List<String> lines1 = List.of("a", "b", "c");
		List<String> lines2 = List.of("a", "b", "c");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of("a", "b", "c");
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_samePrefix() {

		// prepare
		List<String> lines1 = List.of("a", "b", "x");
		List<String> lines2 = List.of("a", "b", "y");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of("a", "b", deleted("x"), inserted("y"));
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_leftIsPrefix() {

		// prepare
		List<String> lines1 = List.of("a", "b");
		List<String> lines2 = List.of("a", "b", "x");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of("a", "b", inserted("x"));
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_rightIsPrefix() {

		// prepare
		List<String> lines1 = List.of("a", "b", "x");
		List<String> lines2 = List.of("a", "b");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of("a", "b", deleted("x"));
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_samePostfix() {

		// prepare
		List<String> lines1 = List.of("x", "a", "b");
		List<String> lines2 = List.of("y", "a", "b");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of(deleted("x"), inserted("y"), "a", "b");
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_leftIsPostfix() {

		// prepare
		List<String> lines1 = List.of("a", "b");
		List<String> lines2 = List.of("x", "a", "b");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of(inserted("x"), "a", "b");
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_rightIsPostfix() {

		// prepare
		List<String> lines1 = List.of("x", "a", "b");
		List<String> lines2 = List.of("a", "b");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of(deleted("x"), "a", "b");
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_deleteBeforeInsert() {

		// prepare
		List<String> lines1 = List.of("a", "x", "c");
		List<String> lines2 = List.of("a", "y", "c");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of("a", deleted("x"), inserted("y"), "c");
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_allDifferent() {

		// prepare
		List<String> lines1 = List.of("a", "b", "c");
		List<String> lines2 = List.of("x", "y", "z");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of(deleted("a"), deleted("b"), deleted("c"), inserted("x"), inserted("y"), inserted("z"));
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_emptyLists() {

		// prepare
		List<String> lines1 = List.of();
		List<String> lines2 = List.of();

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		Assertions.assertEquals(List.of(), lines);
	}

	@Test
	public void diff_leftIsEmpty() {

		// prepare
		List<String> lines1 = List.of();
		List<String> lines2 = List.of("x", "y", "z");

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of(inserted("x"), inserted("y"), inserted("z"));
		Assertions.assertEquals(expectedLines, lines);
	}

	@Test
	public void diff_rightIsEmpty() {

		// prepare
		List<String> lines1 = List.of("a", "b", "c");
		List<String> lines2 = List.of();

		// test
		List<String> lines = DiffUtils.diff(lines1, lines2);

		// assert
		List<String> expectedLines = List.of(deleted("a"), deleted("b"), deleted("c"));
		Assertions.assertEquals(expectedLines, lines);
	}

}
