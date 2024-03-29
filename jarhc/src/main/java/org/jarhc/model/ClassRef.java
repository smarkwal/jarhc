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

package org.jarhc.model;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ClassRef implements Ref, Comparable<ClassRef> {

	private static final ConcurrentHashMap<String, ClassRef> classRefsCache = new ConcurrentHashMap<>();

	private final String className;

	public static ClassRef forClassName(String className) {
		return classRefsCache.computeIfAbsent(className, ClassRef::new);
	}

	public ClassRef(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public String getDisplayName() {
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

	@Override
	public int compareTo(ClassRef classRef) {
		return className.compareTo(classRef.className);
	}

}
