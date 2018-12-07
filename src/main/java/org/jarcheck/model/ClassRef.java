package org.jarcheck.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		ClassRef classRef = (ClassRef) obj;
		return Objects.equals(className, classRef.className);
	}

	@Override
	public int hashCode() {
		return Objects.hash(className);
	}

}
