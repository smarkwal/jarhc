/*
 * Copyright 2018 Stephan Markwalder
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jarhc.test.AssertUtils;
import org.junit.jupiter.api.Test;

class ExceptionUtilsTest {

	@Test
	void test_ExceptionUtils() {
		AssertUtils.assertUtilityClass(ExceptionUtils.class);
	}

	@Test
	void test_getCause_no_cause() {

		// prepare
		Throwable throwable = new NullPointerException("test");

		// test
		Throwable cause = ExceptionUtils.getRootCause(throwable);

		// assert
		assertSame(throwable, cause);

	}

	@Test
	void test_getCause_single_cause() {

		// prepare
		Throwable root = new IllegalArgumentException("cause");
		Throwable throwable = new RuntimeException("test", root);

		// test
		Throwable cause = ExceptionUtils.getRootCause(throwable);

		// assert
		assertSame(root, cause);

	}

	@Test
	void test_getCause_nested_cause() {

		// prepare
		Throwable root = new IllegalArgumentException("cause");
		Throwable nested = new IllegalStateException("nested", root);
		Throwable throwable = new RuntimeException("test", nested);

		// test
		Throwable cause = ExceptionUtils.getRootCause(throwable);

		// assert
		assertSame(root, cause);

	}

	@Test
	void test_getCause_null() {
		assertThrows(IllegalArgumentException.class, () -> ExceptionUtils.getRootCause(null));
	}

}
