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

package org.jarhc.loader;

import org.jarhc.model.ModuleInfo;
import org.jarhc.utils.JavaUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

class ModuleInfoBuilder extends ClassVisitor {

	private final ModuleInfo moduleInfo = new ModuleInfo();

	private final ModuleVisitor moduleVisitor = new ModuleInfoVisitor();

	ModuleInfoBuilder() {
		super(Opcodes.ASM9);
	}

	ModuleInfo getModuleInfo() {
		return moduleInfo;
	}

	@Override
	public ModuleVisitor visitModule(String name, int access, String version) {
		// TODO: add access and version ???

		String moduleName = JavaUtils.toExternalName(name);
		moduleInfo.setModuleName(moduleName);

		return moduleVisitor;
	}

	private class ModuleInfoVisitor extends ModuleVisitor {

		ModuleInfoVisitor() {
			super(Opcodes.ASM9);
		}

		@Override
		public void visitMainClass(String mainClass) {
			// TODO: ???
		}

		@Override
		public void visitPackage(String packageName) {
			packageName = JavaUtils.toExternalName(packageName);
			moduleInfo.addPackage(packageName);
		}

		@Override
		public void visitRequire(String moduleName, int access, String version) {
			// TODO: add access and version ???
			moduleInfo.addRequires(moduleName);
		}

		@Override
		public void visitExport(String packageName, int access, String... modules) {
			// TODO: add access ???
			packageName = JavaUtils.toExternalName(packageName);
			moduleInfo.addExports(packageName, modules);
		}

		@Override
		public void visitOpen(String packageName, int access, String... modules) {
			// TODO: add access ???
			packageName = JavaUtils.toExternalName(packageName);
			moduleInfo.addOpens(packageName, modules);
		}

		@Override
		public void visitUse(String service) {
			// TODO: add service ???
		}

		@Override
		public void visitProvide(String service, String... providers) {
			// TODO: add service and providers ???
		}

		@Override
		public void visitEnd() {
			// nothing to do
		}

	}

}
