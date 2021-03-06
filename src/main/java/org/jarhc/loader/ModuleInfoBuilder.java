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
		super(Opcodes.ASM7);
	}

	ModuleInfo getModuleInfo() {
		return moduleInfo;
	}

	@Override
	public ModuleVisitor visitModule(String name, int access, String version) {
		// System.out.println("visitModule: " + name + ", " + access + ", " + version);
		// TODO: add access and version ???

		String moduleName = JavaUtils.toExternalName(name);
		moduleInfo.setModuleName(moduleName);

		return moduleVisitor;
	}

	private class ModuleInfoVisitor extends ModuleVisitor {

		ModuleInfoVisitor() {
			super(Opcodes.ASM7);
		}

		@Override
		public void visitMainClass(String mainClass) {
			// System.out.println("visitMainClass: " + mainClass);
			// TODO: ???
		}

		@Override
		public void visitPackage(String packageName) {
			// System.out.println("visitPackage: " + packaze);
			// TODO: ???
		}

		@Override
		public void visitRequire(String moduleName, int access, String version) {
			// System.out.println("visitRequire: " + moduleName + ", " + access + ", " + version);
			// TODO: add access and version ???

			moduleInfo.addRequire(moduleName);
		}

		@Override
		public void visitExport(String packageName, int access, String... modules) {
			// System.out.println("visitExport: " + packageName + ", " + access + ", " + modules);
			// TODO: add access and modules ???

			packageName = JavaUtils.toExternalName(packageName);
			moduleInfo.addExport(packageName);
		}

		@Override
		public void visitOpen(String packageName, int access, String... modules) {
			// System.out.println("visitOpen: " + packageName + ", " + access + ", " + modules);
			// TODO: ???
		}

		@Override
		public void visitUse(String service) {
			// System.out.println("visitUse: " + service);
			// TODO: ???
		}

		@Override
		public void visitProvide(String service, String... providers) {
			// System.out.println("visitProvide: " + service + ", " + providers);
			// TODO: ???
		}

		@Override
		public void visitEnd() {
			// nothing to do
		}

	}

}
