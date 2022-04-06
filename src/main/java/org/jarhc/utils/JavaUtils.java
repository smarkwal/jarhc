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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.objectweb.asm.Type;

public class JavaUtils {

	private static final ConcurrentHashMap<String, String> classNamesCache = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, List<String>> parameterTypesCache = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, String> externalNamesCache = new ConcurrentHashMap<>();

	private JavaUtils() {
		throw new IllegalStateException("utility class");
	}

	public static boolean isPrimitiveType(String type) {
		char firstChar = type.charAt(0);
		switch (firstChar) {
			case 'b':
				return "boolean".equals(type) || "byte".equals(type);
			case 'c':
				return "char".equals(type);
			case 'd':
				return "double".equals(type);
			case 'f':
				return "float".equals(type);
			case 'i':
				return "int".equals(type);
			case 'l':
				return "long".equals(type);
			case 's':
				return "short".equals(type);
			default:
				return false;
		}
	}

	public static boolean isVoidType(String type) {
		return "void".equals(type);
	}

	public static String getPackageName(String className) {
		int pos = className.lastIndexOf('.');
		if (pos >= 0) {
			return className.substring(0, pos);
		} else {
			return ""; // empty package
		}
	}

	public static String getTopLevelClassName(String className) {
		int pos = className.indexOf('$');
		if (pos >= 0) {
			return className.substring(0, pos);
		} else {
			return className;
		}
	}

	public static String getClassName(String descriptor) {
		return classNamesCache.computeIfAbsent(descriptor, d -> Type.getType(d).getClassName());
	}

	public static String getReturnType(String methodDescriptor) {

		int pos = methodDescriptor.indexOf(')');
		String descriptor = methodDescriptor.substring(pos + 1);

		return getClassName(descriptor);
	}

	public static List<String> getParameterTypes(String methodDescriptor) {

		int pos = methodDescriptor.indexOf(')');
		String descriptor = methodDescriptor.substring(1, pos);

		return parameterTypesCache.computeIfAbsent(descriptor, d -> {
			Type[] argumentTypes = Type.getArgumentTypes(methodDescriptor);
			List<String> parameterTypes = new ArrayList<>(argumentTypes.length);
			for (Type argumentType : argumentTypes) {
				parameterTypes.add(argumentType.getClassName());
			}
			return parameterTypes;
		});
	}

	public static String getFieldType(String fieldDescriptor) {
		return getClassName(fieldDescriptor);
	}

	public static String getRecordComponentType(String recordComponentDescriptor) {
		return getClassName(recordComponentDescriptor);
	}

	public static String toExternalName(String name) {
		return externalNamesCache.computeIfAbsent(name, n -> n.replace('/', '.'));
	}

	public static boolean isArrayType(String type) {
		return type.endsWith("[]");
	}

	public static String getArrayElementType(String type) {
		int pos = type.indexOf('[');
		if (pos < 0) throw new IllegalArgumentException("Not an array type: " + type);
		return type.substring(0, pos);
	}

	public static String getSimpleClassName(String className) {
		int pos = className.lastIndexOf('.');
		if (pos < 0) return className;
		return className.substring(pos + 1);
	}

	/**
	 * Get major Java version number.
	 *
	 * @return Major Java version number, for example 8, 11, or 17.
	 */
	public static int getJavaVersion() {
		String value = System.getProperty("java.version");
		if (value == null) {
			throw new JarHcException("System property 'java.version' not found.");
		}
		String[] digits = value.split("\\.");
		int version = Integer.parseInt(digits[0]);
		if (version == 1) { // Java 1.1 - 1.8
			version = Integer.parseInt(digits[1]);
		}
		return version;
	}

	public static File getJavaModuleFile(String moduleName) {
		String javaHome = System.getProperty("java.home");
		return new File(javaHome, "jmods/" + moduleName + ".jmod");
	}

}
