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

package org.jarhc.java;

import org.jarhc.model.ClassDef;

import java.util.Optional;

public abstract class ClassLoader {

	private final String name;
	private final ClassLoader parent;

	public ClassLoader(String name, ClassLoader parent) {
		this.name = name;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public ClassLoader getParent() {
		return parent;
	}

	public Optional<ClassDef> getClassDef(String className) {
		Optional<ClassDef> classDef = findClassDef(className);
		if (classDef.isPresent()) {
			return classDef;
		} else if (parent != null) {
			return parent.getClassDef(className);
		} else {
			return Optional.empty();
		}
	}

	protected abstract Optional<ClassDef> findClassDef(String className);

}
