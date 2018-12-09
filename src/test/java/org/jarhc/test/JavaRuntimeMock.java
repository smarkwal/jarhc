package org.jarhc.test;

import org.jarhc.TestUtils;
import org.jarhc.env.JavaRuntime;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaRuntimeMock implements JavaRuntime {

	private final Set<String> classNames = new HashSet<>();

	/**
	 * Create a fake Java runtime using the class names loaded from the given resource.
	 *
	 * @param resource Resource with class names
	 */
	public JavaRuntimeMock(String resource) {
		try {
			List<String> lines = TestUtils.getResourceAsLines(resource, "UTF-8");
			for (String line : lines) {
				if (line.startsWith("#")) continue;
				classNames.add(line);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

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
		if (classNames.contains(className)) {
			return "Bootstrap";
		} else {
			return null;
		}
	}

}
