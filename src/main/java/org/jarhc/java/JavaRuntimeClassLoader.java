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

import org.jarhc.env.JavaRuntime;
import org.jarhc.model.ClassDef;

import java.util.Optional;

public class JavaRuntimeClassLoader extends ClassLoader {

	private final JavaRuntime javaRuntime;

	public JavaRuntimeClassLoader(JavaRuntime javaRuntime) {
		super("Runtime", null);
		this.javaRuntime = javaRuntime;
	}

	@Override
	protected Optional<ClassDef> findClassDef(String className) {
		return javaRuntime.getClassDef(className);
	}

}
