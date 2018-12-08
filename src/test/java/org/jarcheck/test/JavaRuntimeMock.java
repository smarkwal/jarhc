package org.jarcheck.test;

import org.jarcheck.env.JavaRuntime;

public class JavaRuntimeMock implements JavaRuntime {

	@Override
	public String getName() {
		return "Java(TM) SE Runtime Environment";
	}

	@Override
	public String getJavaVersion() {
		return "1.8.0_144";
	}

	@Override
	public String getJavaVendor() {
		return "Oracle Corporation";
	}

	@Override
	public String getJavaHome() {
		return "/opt/java/jdk-1.8.0_144";
	}

	@Override
	public String getClassLoaderName(String className) {
		// delegate to real Java runtime
		// TODO: this can make some tests fail if not run on Oracle JDK/JRE 8
		return JavaRuntime.getDefault().getClassLoaderName(className);
	}

}
