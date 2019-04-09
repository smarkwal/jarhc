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

import org.jarhc.model.*;

import java.util.List;
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

	public Optional<ClassDef> getClassDef(ClassRef classRef) {
		String className = classRef.getClassName();
		return getClassDef(className);
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

	public Optional<FieldDef> getFieldDef(FieldRef fieldRef) {
		String fieldOwner = fieldRef.getFieldOwner();
		String fieldName = fieldRef.getFieldName();
		return findFieldDef(fieldOwner, fieldName, this);
	}

	private Optional<FieldDef> findFieldDef(String className, String fieldName, ClassLoader classLoader) {

		// try to find class
		Optional<ClassDef> classDef = getClassDef(className);
		if (classDef.isPresent()) {

			// try to find field in class
			Optional<FieldDef> fieldDef = classDef.get().getFieldDef(fieldName);
			if (fieldDef.isPresent()) {
				return fieldDef;
			}

			// field not found in class

			// try to find field in interfaces first
			// (see: https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-5.html#jvms-5.4.3.2)
			List<String> interfaceNames = classDef.get().getInterfaceNames();
			for (String interfaceName : interfaceNames) {
				fieldDef = findFieldDef(interfaceName, fieldName, classLoader); // TODO: use class loader of class definition
				if (fieldDef.isPresent()) {
					return fieldDef;
				}
			}

			// try to find field in superclass
			String superName = classDef.get().getSuperName();
			if (superName != null) {
				fieldDef = findFieldDef(superName, fieldName, classLoader); // TODO: use class loader of class definition
				if (fieldDef.isPresent()) {
					return fieldDef;
				}
			}

		}

		// field not found in class, superclass, or interfaces
		return Optional.empty();
	}

	public Optional<MethodDef> getMethodDef(MethodRef methodRef) {
		String methodOwner = methodRef.getMethodOwner();
		String methodName = methodRef.getMethodName();
		String methodDescriptor = methodRef.getMethodDescriptor();
		return findMethodDef(methodOwner, methodName, methodDescriptor, this);
	}

	private Optional<MethodDef> findMethodDef(String className, String methodName, String methodDescriptor, ClassLoader classLoader) {

		// try to find class
		Optional<ClassDef> classDef = getClassDef(className);
		if (classDef.isPresent()) {

			// try to find method in class
			Optional<MethodDef> methodDef = classDef.get().getMethodDef(methodName, methodDescriptor);
			if (methodDef.isPresent()) {
				return methodDef;
			}

			// method not found in class

			// TODO: check method resolution strategy

			// try to find method in interfaces
			List<String> interfaceNames = classDef.get().getInterfaceNames();
			for (String interfaceName : interfaceNames) {
				methodDef = findMethodDef(interfaceName, methodName, methodDescriptor, classLoader); // TODO: use class loader of class definition
				if (methodDef.isPresent()) {
					return methodDef;
				}
			}

			// try to find method in superclass
			String superName = classDef.get().getSuperName();
			if (superName != null) {
				methodDef = findMethodDef(superName, methodName, methodDescriptor, classLoader); // TODO: use class loader of class definition
				if (methodDef.isPresent()) {
					return methodDef;
				}
			}

		}

		// method not found in class, superclass, or interfaces
		return Optional.empty();
	}

}
