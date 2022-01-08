/*
 * Copyright 2019 Stephan Markwalder
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

public class AnnotationRef implements Ref {

	public enum Target {
		TYPE,
		FIELD,
		METHOD,
		PARAMETER,
		CONSTRUCTOR,
		LOCAL_VARIABLE,
		ANNOTATION_TYPE,
		PACKAGE,
		TYPE_PARAMETER,
		TYPE_USE,
		MODULE,
		RECORD_COMPONENT
	}

	private final String className;

	private final Target target;

	public AnnotationRef(String className, Target target) {
		this.className = className;
		this.target = target;
	}

	public String getClassName() {
		return className;
	}

	public Target getTarget() {
		return target;
	}

	@Override
	public String getDisplayName() {
		return "@" + className;
	}

	@Override
	public String toString() {
		return "AnnotationRef[" + className + "," + target + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		AnnotationRef annotationRef = (AnnotationRef) obj;
		if (target != annotationRef.target) return false;
		return Objects.equals(className, annotationRef.className);
	}

	@Override
	public int hashCode() {
		return Objects.hash(className, target);
	}

}
