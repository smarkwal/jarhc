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

import java.util.Objects;

public class ArrayUtils {

	private ArrayUtils() {
		throw new IllegalStateException("utility class");
	}

	/**
	 * Check if the given array contains any (at least one) of the given values.
	 *
	 * @param array  Array
	 * @param values Values
	 * @return <code>true</code> if at least one value is found in array, <code>false</code> otherwise.
	 */
	public static boolean containsAny(Object[] array, Object... values) {
		if (array == null || array.length == 0) return false;
		for (Object element : array) {
			for (Object value : values) {
				if (Objects.equals(element, value)) return true;
			}
		}
		return false;
	}

}
