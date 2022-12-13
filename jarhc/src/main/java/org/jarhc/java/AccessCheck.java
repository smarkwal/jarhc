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

import java.lang.reflect.Modifier;
import java.util.List;
import org.jarhc.model.ClassDef;
import org.jarhc.model.FieldDef;
import org.jarhc.model.MethodDef;
import org.jarhc.model.ModuleInfo;
import org.jarhc.utils.JavaUtils;

/**
 * Access control for classes, fields and methods.
 */
public class AccessCheck {

	/**
	 * Class resolver used to find superclasses and interfaces.
	 */
	private final ClassLoader classLoader;

	public AccessCheck(ClassLoader classLoader) {
		if (classLoader == null) throw new IllegalArgumentException("classResolver");
		this.classLoader = classLoader;
	}

	/**
	 * Check access from source class to target class.
	 *
	 * @param sourceClassDef Source class
	 * @param targetClassDef Target class
	 * @return <code>true</code> if source class has access to target class, <code>false</code> otherwise.
	 */
	public boolean hasAccess(ClassDef sourceClassDef, ClassDef targetClassDef) {

		// classes can only be public or package-private
		if (targetClassDef.isProtected() || targetClassDef.isPrivate()) {
			throw new IllegalArgumentException("targetClassDef");
		}

		// check module constraints
		ModuleInfo sourceModuleInfo = sourceClassDef.getModuleInfo();
		ModuleInfo targetModuleInfo = targetClassDef.getModuleInfo();
		if (sourceModuleInfo.isUnnamed() && targetModuleInfo.isUnnamed()) {
			// both classes in unnamed modules -> continue
		} else if (sourceModuleInfo.isSame(targetModuleInfo)) {
			// both classes in same named module -> continue
		} else {
			// TODO: #68 check module constraints
			// named_1 -> named_2
			// named -> unnamed
			// unnamed -> named
		}

		if (targetClassDef.isPublic()) {
			// allow access to public class
			return true;
		}

		String sourceClassName = sourceClassDef.getClassName();
		String targetClassName = targetClassDef.getClassName();
		if (sourceClassName.equals(targetClassName)) {
			// allow access to same class
			return true;
		}

		// allow access to package-private class in same package
		return JavaUtils.inSamePackage(sourceClassName, targetClassName);
	}

	/**
	 * Check access from source class to target field.
	 *
	 * @param sourceClassDef Source class
	 * @param fieldDef       Target field
	 * @return <code>true</code> if source class has access to target field, <code>false</code> otherwise.
	 */
	public boolean hasAccess(ClassDef sourceClassDef, FieldDef fieldDef) {
		return hasAccess(sourceClassDef, fieldDef.getClassDef(), fieldDef.getAccess());
	}

	/**
	 * Check access from source class to target method.
	 *
	 * @param sourceClassDef Source class
	 * @param methodDef      Target method
	 * @return <code>true</code> if source class has access to target method, <code>false</code> otherwise.
	 */
	public boolean hasAccess(ClassDef sourceClassDef, MethodDef methodDef) {
		return hasAccess(sourceClassDef, methodDef.getClassDef(), methodDef.getAccess());
	}

	private boolean hasAccess(ClassDef sourceClassDef, ClassDef targetClassDef, int memberAccess) {

		// see https://docs.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html

		if (Modifier.isPublic(memberAccess)) {
			// allow access to public member
			return true;
		}

		String sourceClassName = sourceClassDef.getClassName();
		String targetClassName = targetClassDef.getClassName();

		// check if source and target class:
		// - are the same class
		// - have an outer/inner class relation
		// - are both inner classes of the same outer class
		if (JavaUtils.inSameTopLevelClass(sourceClassName, targetClassName)) {
			// allow access to member in same class, outer class, or inner class
			// assumption: outer and inner classes are always compatible
			// because they  have been compiled together.
			return true;
		}

		if (Modifier.isPrivate(memberAccess)) {
			// deny access to private member in other class
			return false;
		}

		// check is source class is in same package as target class
		boolean inSamePackage = JavaUtils.inSamePackage(sourceClassName, targetClassName);
		if (inSamePackage) {
			// allow access to protected or package-private member in same package
			return true;
		}

		if (!Modifier.isProtected(memberAccess)) {
			// deny access to package-private member in different package
			return false;
		}

		// allow access to protected member in subclass
		return isSubclass(sourceClassDef, targetClassName);
	}

	private boolean isSubclass(ClassDef sourceClassDef, String targetClassName) {

		// TODO: prevent endless loops because of a cyclic inheritance structure

		String superName = sourceClassDef.getSuperName();
		if (targetClassName.equals(superName)) return true;

		List<String> interfaceNames = sourceClassDef.getInterfaceNames();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < interfaceNames.size(); i++) {
			String interfaceName = interfaceNames.get(i);
			if (targetClassName.equals(interfaceName)) return true;
		}

		// if source class has a superclass ...
		if (superName != null) {
			if (isSubclass(superName, targetClassName)) return true;
		}

		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < interfaceNames.size(); i++) {
			String interfaceName = interfaceNames.get(i);
			// try to find interface class
			if (isSubclass(interfaceName, targetClassName)) return true;
		}

		return false;
	}

	private boolean isSubclass(String sourceClassName, String targetClassName) {

		// try to find source class
		ClassDef classDef = classLoader.getClassDef(sourceClassName);
		if (classDef == null) {
			// TODO: handle class not found
			return false;
		}

		// check if source class is a subclass of target class
		return isSubclass(classDef, targetClassName);
	}

}
