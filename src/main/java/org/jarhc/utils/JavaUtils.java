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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.objectweb.asm.Type;

public class JavaUtils {

	private JavaUtils() {
		throw new IllegalStateException("utility class");
	}

	private static final Set<String> primitiveTypes = new HashSet<>();

	static {
		primitiveTypes.add("byte");
		primitiveTypes.add("short");
		primitiveTypes.add("int");
		primitiveTypes.add("long");
		primitiveTypes.add("float");
		primitiveTypes.add("double");
		primitiveTypes.add("char");
		primitiveTypes.add("boolean");
	}

	public static boolean isPrimitiveType(String type) {
		return primitiveTypes.contains(type);
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

	public static String getReturnType(String methodDescriptor) {
		Type methodType = Type.getType(methodDescriptor);
		return methodType.getReturnType().getClassName();
	}

	public static List<String> getParameterTypes(String methodDescriptor) {
		Type methodType = Type.getType(methodDescriptor);
		return Arrays.stream(methodType.getArgumentTypes()).map(Type::getClassName).collect(Collectors.toList());
	}

	public static String getFieldType(String fieldDescriptor) {
		return Type.getType(fieldDescriptor).getClassName();
	}

	public static String getRecordComponentType(String recordComponentDescriptor) {
		return Type.getType(recordComponentDescriptor).getClassName();
	}

	public static String toExternalName(String name) {
		return name.replace('/', '.');
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
