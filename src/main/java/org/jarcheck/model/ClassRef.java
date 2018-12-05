package org.jarcheck.model;

import java.util.Collections;
import java.util.List;

public class ClassRef {

	public static final List<ClassRef> NONE = Collections.emptyList();

	private final String className;

	public ClassRef(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public String toString() {
		return "ClassRef[" + className + "]";
	}

}
