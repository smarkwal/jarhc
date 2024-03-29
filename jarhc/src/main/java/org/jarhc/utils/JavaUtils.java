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
import java.util.concurrent.ConcurrentHashMap;

public class JavaUtils {

	private static final ConcurrentHashMap<String, String> classNamesCache = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, String> returnTypesCache = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, String[]> parameterTypesCache = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, String> arrayElementTypesCache = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, String> externalNamesCache = new ConcurrentHashMap<>();

	private static final String[] NO_PARAMETERS = new String[0];

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

	public static String getParentPackageName(String packageName, int length) {
		if (length == 0) {
			return "";
		}
		int pos = 0;
		while (length-- > 0) {
			pos = packageName.indexOf('.', pos + 1);
			if (pos < 0) {
				return packageName;
			}
		}
		return packageName.substring(0, pos);
	}

	public static boolean inSamePackage(String sourceClassName, String targetClassName) {
		int pos1 = sourceClassName.lastIndexOf('.');
		int pos2 = targetClassName.lastIndexOf('.');
		if (pos1 != pos2) return false;
		for (int i = 0; i < pos1; i++) {
			char chr1 = sourceClassName.charAt(i);
			char chr2 = targetClassName.charAt(i);
			if (chr1 != chr2) return false;
		}
		return true;
	}

	public static String getTopLevelClassName(String className) {
		int pos = className.indexOf('$');
		if (pos >= 0) {
			return className.substring(0, pos);
		} else {
			return className;
		}
	}

	public static boolean inSameTopLevelClass(String sourceClassName, String targetClassName) {
		int pos1 = sourceClassName.indexOf('$');
		int pos2 = targetClassName.indexOf('$');
		if (pos1 < 0) pos1 = sourceClassName.length();
		if (pos2 < 0) pos2 = targetClassName.length();
		if (pos1 != pos2) return false;
		for (int i = 0; i < pos1; i++) {
			char chr1 = sourceClassName.charAt(i);
			char chr2 = targetClassName.charAt(i);
			if (chr1 != chr2) return false;
		}
		return true;
	}

	public static String getClassName(String typeDescriptor) {
		return classNamesCache.computeIfAbsent(typeDescriptor, JavaUtils::getClassName_Internal);
	}

	private static String getClassName_Internal(String typeDescriptor) {
		int start = 0;
		int end = typeDescriptor.length();
		return toTypeName(typeDescriptor, start, end);
	}

	public static String getReturnType(String methodDescriptor) {
		return returnTypesCache.computeIfAbsent(methodDescriptor, JavaUtils::getReturnType_Internal);
	}

	private static String getReturnType_Internal(String methodDescriptor) {
		int pos = methodDescriptor.indexOf(')');
		int start = pos + 1;
		int end = methodDescriptor.length();
		return toTypeName(methodDescriptor, start, end);
	}

	public static String[] getParameterTypes(String methodDescriptor) {
		return parameterTypesCache.computeIfAbsent(methodDescriptor, JavaUtils::getParameterTypes_Internal);
	}

	private static String[] getParameterTypes_Internal(String methodDescriptor) {

		int parameterCount = getParameterCount(methodDescriptor);
		if (parameterCount == 0) {
			return NO_PARAMETERS;
		}

		String[] parameterTypes = new String[parameterCount];
		int pos = 1;
		int index = 0;
		while (methodDescriptor.charAt(pos) != ')') {
			int start = pos;
			while (methodDescriptor.charAt(pos) == '[') {
				pos++;
			}
			if (methodDescriptor.charAt(pos) == 'L') {
				pos = methodDescriptor.indexOf(';', pos);
			}
			pos++;
			String parameterType = toTypeName(methodDescriptor, start, pos);
			parameterTypes[index++] = parameterType;
		}

		return parameterTypes;
	}

	private static int getParameterCount(String methodDescriptor) {
		int parameterCount = 0;
		int pos = 1;
		while (methodDescriptor.charAt(pos) != ')') {
			while (methodDescriptor.charAt(pos) == '[') {
				pos++;
			}
			if (methodDescriptor.charAt(pos) == 'L') {
				pos = methodDescriptor.indexOf(';', pos);
			}
			pos++;
			parameterCount++;
		}
		return parameterCount;
	}

	public static String getFieldType(String fieldDescriptor) {
		return getClassName(fieldDescriptor);
	}

	public static String getRecordComponentType(String recordComponentDescriptor) {
		return getClassName(recordComponentDescriptor);
	}

	private static String toTypeName(String descriptor, int start, int end) {

		StringBuilder typeName;

		int pos = start;
		while (descriptor.charAt(pos) == '[') {
			pos++;
		}
		int arrayDimensions = pos - start;

		char type = descriptor.charAt(pos);
		if (type == 'L') {
			pos++;

			int length = end - start;
			typeName = new StringBuilder(length + arrayDimensions);
			while (pos < end) {
				char chr = descriptor.charAt(pos);
				if (chr == ';') {
					break;
				} else if (chr == '/') {
					typeName.append('.');
				} else {
					typeName.append(chr);
				}
				pos++;
			}

		} else {

			String primitiveTypeName = toPrimitiveTypeName(type);
			if (arrayDimensions == 0) {
				return primitiveTypeName;
			}

			typeName = new StringBuilder(primitiveTypeName.length() + arrayDimensions * 2);
			typeName.append(primitiveTypeName);

		}

		//noinspection StringRepeatCanBeUsed (performance)
		for (int i = 0; i < arrayDimensions; i++) {
			typeName.append("[]");
		}

		return typeName.toString();
	}

	private static String toPrimitiveTypeName(char type) {
		if (type == 'V') {
			return "void";
		} else if (type == 'Z') {
			return "boolean";
		} else if (type == 'B') {
			return "byte";
		} else if (type == 'C') {
			return "char";
		} else if (type == 'S') {
			return "short";
		} else if (type == 'I') {
			return "int";
		} else if (type == 'J') {
			return "long";
		} else if (type == 'F') {
			return "float";
		} else if (type == 'D') {
			return "double";
		} else {
			throw new IllegalArgumentException("unknown type: " + type);
		}
	}

	public static String toExternalName(String name) {
		return externalNamesCache.computeIfAbsent(name, n -> n.replace('/', '.'));
	}

	public static boolean isArrayType(String type) {
		return type.endsWith("[]");
	}

	public static String getArrayElementType(String type) {
		return arrayElementTypesCache.computeIfAbsent(type, JavaUtils::getArrayElementType_Internal);
	}

	private static String getArrayElementType_Internal(String type) {
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
