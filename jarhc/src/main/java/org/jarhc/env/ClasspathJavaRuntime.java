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

package org.jarhc.env;

import java.util.function.Predicate;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.utils.Markdown;

public class ClasspathJavaRuntime extends JavaRuntime {

	private final Classpath classpath;

	public ClasspathJavaRuntime(Classpath classpath) {
		this.classpath = classpath;
	}

	@Override
	public String getName() {
		return Markdown.UNKNOWN; // TODO: can we get this from the runtime classes somehow?
	}

	@Override
	public String getJavaVersion() {
		return Markdown.UNKNOWN;
	}

	@Override
	public String getJavaVendor() {
		return Markdown.UNKNOWN;
	}

	@Override
	public String getJavaHome() {
		return Markdown.NONE;
	}

	@Override
	public JarFile findJarFile(Predicate<JarFile> predicate) {
		return classpath.findJarFile(predicate);
	}

	@Override
	protected boolean findPackage(String packageName) {
		return classpath.containsPackage(packageName);
	}

	@Override
	protected ClassDef findClassDef(String className) {
		return classpath.getClassDef(className);
	}

}
