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

import static org.jarhc.model.AnnotationRef.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Def extends AccessFlags {

	/**
	 * List of annotations.
	 */
	private List<AnnotationRef> annotationRefs = null;

	Def(int flags) {
		super(flags);
	}

	public abstract ClassDef getClassDef();

	public abstract String getDisplayName();

	public List<AnnotationRef> getAnnotationRefs() {
		if (annotationRefs == null) {
			return Collections.emptyList();
		}
		return annotationRefs;
	}

	public boolean hasAnnotationRef(String className, Target target) {
		if (annotationRefs == null) {
			return false;
		}
		for (AnnotationRef annotationRef : annotationRefs) {
			if (annotationRef.getClassName().equals(className) && annotationRef.getTarget() == target) {
				return true;
			}
		}
		return false;
	}

	public Def addAnnotationRef(AnnotationRef annotationRef) {
		if (annotationRefs == null) {
			annotationRefs = new ArrayList<>(1);
		}
		annotationRefs.add(annotationRef);
		return this;
	}

	/**
	 * Checks if this definition comes from the same JAR file as the given definition.
	 *
	 * @param def Class, method, or field definition.
	 * @return <code>true</code> if both definitions come from the same JAR file.
	 */
	public boolean isFromSameJarFileAs(Def def) {
		JarFile jarFile1 = this.getClassDef().getJarFile();
		JarFile jarFile2 = def.getClassDef().getJarFile();
		return jarFile1 == jarFile2;
	}

}
