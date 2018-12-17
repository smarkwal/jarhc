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
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.ModuleNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleInfoLoader {

	public ModuleInfo load(InputStream stream) throws IOException {
		if (stream == null) throw new IllegalArgumentException("stream");

		// parse class file with ASM
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(stream);
		classReader.accept(classNode, 0);

		ModuleNode moduleNode = classNode.module;
		List<String> exports = moduleNode.exports.stream().map(e -> e.packaze).collect(Collectors.toList());
		List<String> requires = moduleNode.requires.stream().map(e -> e.module).collect(Collectors.toList());

		// create module definition
		return new ModuleInfo(moduleNode.name, exports, requires);
	}

}
