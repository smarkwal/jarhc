/*
 * Copyright 2019 Stephan Markwalder
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

package org.jarhc.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class AnnotationRefTest {

	@Test
	void test_AnnotationRef() {

		// test
		AnnotationRef annotationRef = new AnnotationRef("java.lang.Deprecated");

		// assert
		assertEquals("java.lang.Deprecated", annotationRef.getClassName());

	}

	@Test
	void test_toString() {

		// test
		AnnotationRef annotationRef = new AnnotationRef("java.lang.Deprecated");

		// assert
		assertEquals("AnnotationRef[java.lang.Deprecated]", annotationRef.toString());

	}

	@Test
	void test_equals() {

		AnnotationRef annotationRef1 = new AnnotationRef("java.lang.Deprecated");
		AnnotationRef annotationRef2 = new AnnotationRef("java.lang.Deprecated");
		assertEquals(annotationRef1, annotationRef2);

		annotationRef1 = new AnnotationRef("java.lang.Deprecated");
		annotationRef2 = new AnnotationRef("org.junit.jupiter.api.Test");
		assertNotEquals(annotationRef1, annotationRef2);

	}

	@Test
	void test_hashCode() {

		AnnotationRef annotationRef1 = new AnnotationRef("java.lang.Deprecated");
		AnnotationRef annotationRef2 = new AnnotationRef("java.lang.Deprecated");
		assertEquals(annotationRef1.hashCode(), annotationRef2.hashCode());

		annotationRef1 = new AnnotationRef("java.lang.Deprecated");
		annotationRef2 = new AnnotationRef("org.junit.jupiter.api.Test");
		assertNotEquals(annotationRef1.hashCode(), annotationRef2.hashCode());

	}

}