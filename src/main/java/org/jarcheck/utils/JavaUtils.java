package org.jarcheck.utils;

import java.util.HashSet;
import java.util.Set;

public class JavaUtils {

	private static final ClassLoader BOOTSTRAP_CLASSLOADER = ClassLoader.getSystemClassLoader().getParent();

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

	public static boolean isBootstrapClass(String className) {
		Class javaClass = loadBootstrapClass(className);
		return javaClass != null;
	}

	public static Class loadBootstrapClass(String className) {
		className = className.replace('/', '.');
		try {
			return Class.forName(className, false, BOOTSTRAP_CLASSLOADER);
		} catch (ClassNotFoundException e) {
			return null;
		} catch (Throwable t) {
			// TODO: ignore ?
			return null;
		}
	}

}
