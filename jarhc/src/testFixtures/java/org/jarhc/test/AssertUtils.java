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

package org.jarhc.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockingDetails;
import org.mockito.Mockito;

public class AssertUtils {

	public static void assertUtilityClass(Class<?> clazz) {

		// class must have only static public methods
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			int modifiers = method.getModifiers();
			if (Modifier.isPublic(modifiers)) {
				Assertions.assertTrue(Modifier.isStatic(modifiers), "Public method is not static: " + method);
			}
		}

		// class must have a private no-arg constructor
		try {
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			int modifiers = constructor.getModifiers();
			Assertions.assertTrue(Modifier.isPrivate(modifiers), "No-arg constructor is not private: " + constructor);

			// constructor must throw IllegalStateException
			constructor.setAccessible(true);
			try {
				constructor.newInstance();
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
				if (cause instanceof IllegalStateException) {
					Assertions.assertEquals("utility class", cause.getMessage());
				} else {
					Assertions.fail(e);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				Assertions.fail(e);
			}

		} catch (NoSuchMethodException e) {
			Assertions.fail("No-arg constructor not found");
		}

	}

	/**
	 * Assert that the given object is a Mockito mock.
	 *
	 * @param object Object
	 */
	public static void assertMock(Object object) {
		MockingDetails details = Mockito.mockingDetails(object);
		Assertions.assertTrue(details.isMock());
	}

}
