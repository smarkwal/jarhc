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

package org.jarhc.loader;

import org.jarhc.model.ModuleInfo;
import org.objectweb.asm.ClassReader;

public class ModuleInfoLoader {

	ModuleInfoLoader() {
	}

	public ModuleInfo load(byte[] data) {
		if (data == null) throw new IllegalArgumentException("data");

		ModuleInfoBuilder classVisitor = new ModuleInfoBuilder();

		ClassReader classReader = new ClassReader(data);
		classReader.accept(classVisitor, 0);

		// create module definition
		return classVisitor.getModuleInfo();
	}

}
