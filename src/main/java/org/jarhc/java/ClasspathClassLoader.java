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

package org.jarhc.java;

import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;

import java.util.Optional;

public class ClasspathClassLoader extends ClassLoader {

	private final Classpath classpath;

	public ClasspathClassLoader(Classpath classpath, String name, ClassLoader parent) {
		super(name, parent);
		this.classpath = classpath;
	}

	@Override
	protected Optional<ClassDef> findClassDef(String className) {
		return classpath.getClassDef(className);
	}

}
