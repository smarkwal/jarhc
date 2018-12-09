package org.jarhc.env;

public interface JavaRuntime {

	JavaRuntime DEFAULT = new JavaRuntimeImpl();

	static JavaRuntime getDefault() {
		return DEFAULT;
	}

	String getName();

	String getJavaVersion();

	String getJavaVendor();

	String getJavaHome();

	String getClassLoaderName(String className);

	default boolean classExists(String className) {
		String classLoader = getClassLoaderName(className);
		return classLoader != null;
	}

}
