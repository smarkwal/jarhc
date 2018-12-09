package org.jarhc.env;

import java.util.Properties;

public class JavaRuntime {

	private static JavaRuntime defaultRuntime = new JavaRuntime();

	public static JavaRuntime getDefault() {
		return defaultRuntime;
	}

	public static void setDefault(JavaRuntime javaRuntime) {
		defaultRuntime = javaRuntime;
	}

	// ---------------------------------------------------------------------------

	private static final ClassLoader CLASSLOADER = ClassLoader.getSystemClassLoader().getParent();

	private final Properties systemProperties;

	protected JavaRuntime() {
		systemProperties = System.getProperties();
	}

	public String getName() {
		return systemProperties.getProperty("java.runtime.name");
	}

	public String getJavaVersion() {
		return systemProperties.getProperty("java.version");
	}

	public String getJavaVendor() {
		return systemProperties.getProperty("java.vendor");
	}

	public String getJavaHome() {
		return systemProperties.getProperty("java.home");
	}

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
