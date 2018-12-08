package org.jarcheck.env;

import java.util.Properties;

class JavaRuntimeImpl implements JavaRuntime {

	private static final ClassLoader CLASSLOADER = ClassLoader.getSystemClassLoader().getParent();

	private final Properties systemProperties;

	JavaRuntimeImpl() {
		systemProperties = System.getProperties();
	}

	@Override
	public String getName() {
		return systemProperties.getProperty("java.runtime.name");
	}

	@Override
	public String getJavaVersion() {
		return systemProperties.getProperty("java.version");
	}

	@Override
	public String getJavaVendor() {
		return systemProperties.getProperty("java.vendor");
	}

	@Override
	public String getJavaHome() {
		return systemProperties.getProperty("java.home");
	}

	@Override
	public String getClassLoaderName(String className) {
		try {
			Class<?> javaClass = Class.forName(className, false, CLASSLOADER);
			ClassLoader classLoader = javaClass.getClassLoader();
			if (classLoader == null) return "Bootstrap";
			return classLoader.toString();
		} catch (ClassNotFoundException e) {
			return null;
		} catch (Throwable t) {
			// TODO: ignore ?
			t.printStackTrace(System.err);
			return null;
		}
	}

}
