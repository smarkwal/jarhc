package org.jarcheck.utils;

import java.util.HashSet;
import java.util.Set;

public class JavaUtils {

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

}
