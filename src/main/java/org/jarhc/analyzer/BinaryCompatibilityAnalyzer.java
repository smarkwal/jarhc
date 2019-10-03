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

package org.jarhc.analyzer;

import static org.jarhc.utils.StringUtils.joinLines;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.jarhc.java.AccessCheck;
import org.jarhc.java.ClassLoader;
import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.Classpath;
import org.jarhc.model.FieldDef;
import org.jarhc.model.FieldRef;
import org.jarhc.model.JarFile;
import org.jarhc.model.MethodDef;
import org.jarhc.model.MethodRef;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.StringUtils;

public class BinaryCompatibilityAnalyzer implements Analyzer {

	private final boolean reportOwnerClassNotFound;

	public BinaryCompatibilityAnalyzer() {
		this(false);
	}

	public BinaryCompatibilityAnalyzer(boolean reportOwnerClassNotFound) {
		this.reportOwnerClassNotFound = reportOwnerClassNotFound;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Binary Compatibility", "Compatibility issues between JAR files.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Issues");

		AccessCheck accessCheck = new AccessCheck(classpath);

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// issues found in JAR file (sorted by class name)
			final Set<String> jarIssues = new TreeSet<>();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			classDefs.parallelStream().forEach(classDef -> {

				// issues found in class definition (in order or appearance)
				Set<String> classIssues = new LinkedHashSet<>();

				// validate class definition
				validateClassHierarchy(classDef, classpath, accessCheck, classIssues);
				validateAbstractMethods(classDef, classpath, classIssues);
				validateClassRefs(classDef, classpath, accessCheck, classIssues);
				validateMethodRefs(classDef, classpath, accessCheck, classIssues);
				validateFieldRefs(classDef, classpath, accessCheck, classIssues);

				if (!classIssues.isEmpty()) {
					String issue = createJarIssue(classDef, classIssues);
					synchronized (jarIssues) {
						jarIssues.add(issue);
					}
				}

			});

			if (!jarIssues.isEmpty()) {
				String lines = joinLines(jarIssues).trim();
				table.addRow(jarFile.getFileName(), lines);
			}
		}

		return table;
	}

	private String createJarIssue(ClassDef classDef, Set<String> classIssues) {
		String className = classDef.getClassName();
		String lines = classIssues.stream().map(i -> "\u2022 " + i).collect(StringUtils.joinLines());
		return className + System.lineSeparator() + lines + System.lineSeparator();
	}

	// -----------------------------------------------------------------------------------------------------
	// class hierarchy (superclasses and interfaces)

	private void validateClassHierarchy(ClassDef classDef, Classpath classpath, AccessCheck accessCheck, Set<String> classIssues) {

		// if class has a superclass ...
		String superName = classDef.getSuperName();
		if (superName != null) {
			// check if superclass exists
			Optional<ClassDef> superClassDef = classpath.getClassDef(superName);
			if (!superClassDef.isPresent()) {
				classIssues.add("Superclass not found: " + superName);
			} else {
				ClassDef superClass = superClassDef.get();
				validateSuperclass(superClass, classDef, accessCheck, classIssues);
			}
		}

		// for every interface ...
		List<String> interfaceNames = classDef.getInterfaceNames();
		for (String interfaceName : interfaceNames) {
			// check if interface exists
			Optional<ClassDef> interfaceClassDef = classpath.getClassDef(interfaceName);
			if (!interfaceClassDef.isPresent()) {
				classIssues.add("Interface not found: " + interfaceName);
			} else {
				ClassDef interfaceDef = interfaceClassDef.get();
				validateInterface(interfaceDef, classDef, accessCheck, classIssues);
			}
		}

	}

	@SuppressWarnings("StatementWithEmptyBody")
	private void validateSuperclass(ClassDef superClass, ClassDef classDef, AccessCheck accessCheck, Set<String> classIssues) {

		// check if class is final
		if (superClass.isFinal()) {
			classIssues.add("Superclass is final: " + superClass.getDisplayName());
		}

		// check if class is an annotation, interface, or enum
		if (superClass.isAnnotation()) {
			classIssues.add("Superclass is an annotation: " + superClass.getDisplayName());
		} else if (superClass.isInterface()) {
			classIssues.add("Superclass is an interface: " + superClass.getDisplayName());
		} else if (superClass.isEnum()) {
			if (classDef.isEnum() && classDef.getClassName().startsWith(superClass.getClassName() + "$")) {
				// superclass is outer enum class,
				// current class is inner anonymous class implementing the abstract enum class
			} else {
				classIssues.add("Superclass is an enum: " + superClass.getDisplayName());
			}
		} else {
			// OK (regular class or abstract class)
		}

		// check access to superclass
		boolean access = accessCheck.hasAccess(classDef, superClass);
		if (!access) {
			classIssues.add("Superclass is not accessible: " + superClass.getDisplayName());
		}

	}

	@SuppressWarnings("StatementWithEmptyBody")
	private void validateInterface(ClassDef interfaceClass, ClassDef classDef, AccessCheck accessCheck, Set<String> classIssues) {

		// check if class is an interface
		if (interfaceClass.isAnnotation()) {
			// OK (annotation interface)
		} else if (interfaceClass.isInterface()) {
			// OK (regular interface)
		} else if (interfaceClass.isEnum()) {
			classIssues.add("Interface is an enum: " + interfaceClass.getDisplayName());
		} else if (interfaceClass.isAbstract()) {
			classIssues.add("Interface is an abstract class: " + interfaceClass.getDisplayName());
		} else {
			classIssues.add("Interface is a class: " + interfaceClass.getDisplayName());
		}

		// check access to interface class
		boolean access = accessCheck.hasAccess(classDef, interfaceClass);
		if (!access) {
			classIssues.add("Interface is not accessible: " + interfaceClass.getDisplayName());
		}

	}

	// -----------------------------------------------------------------------------------------------------
	// implementation of abstract methods

	private void validateAbstractMethods(ClassDef classDef, Classpath classpath, Set<String> classIssues) {

		// skip check if class is abstract
		if (classDef.isAbstract()) {
			return;
		}

		// class must implement all abstract methods declared in superclasses and interfaces

		// collect all concrete and abstract methods
		List<MethodDef> concreteMethods = new ArrayList<>();
		List<MethodDef> abstractMethods = new ArrayList<>();
		Set<String> visitedClasses = new HashSet<>();
		collectMethodDefs(classDef, classpath, concreteMethods, abstractMethods, visitedClasses);

		// if there is at least one abstract method ...
		if (!abstractMethods.isEmpty()) {

			// discard all abstract methods for which there is a concrete implementation
			for (MethodDef concreteMethod : concreteMethods) {
				abstractMethods.removeIf(abstractMethod -> isImplementedBy(abstractMethod, concreteMethod));
			}

			// report all remaining (unimplemented) abstract methods
			for (MethodDef methodDef : abstractMethods) {
				classIssues.add("Abstract method not implemented: " + methodDef.getDisplayName());
			}

		}

	}

	/**
	 * Collect concrete and abstract methods in the given class, all superclasses, and all interfaces.
	 *
	 * @param classDef        Class definition
	 * @param classpath       Classpath
	 * @param concreteMethods Concrete methods
	 * @param abstractMethods Abstract methods
	 * @param visitedClasses  Already visited classes
	 */
	private void collectMethodDefs(ClassDef classDef, Classpath classpath, List<MethodDef> concreteMethods, List<MethodDef> abstractMethods, Set<String> visitedClasses) {

		// do not visit the same class twice
		String className = classDef.getClassName();
		if (!visitedClasses.add(className)) {
			return;
		}

		// collect methods from superclass
		String superName = classDef.getSuperName();
		if (superName != null) {
			Optional<ClassDef> superClass = classpath.getClassDef(superName);
			superClass.ifPresent(def -> collectMethodDefs(def, classpath, concreteMethods, abstractMethods, visitedClasses));
		}

		// collect methods from interfaces
		List<String> interfaceNames = classDef.getInterfaceNames();
		for (String interfaceName : interfaceNames) {
			Optional<ClassDef> interfaceDef = classpath.getClassDef(interfaceName);
			interfaceDef.ifPresent(def -> collectMethodDefs(def, classpath, concreteMethods, abstractMethods, visitedClasses));
		}

		// categorize methods in class definition
		List<MethodDef> methodDefs = classDef.getMethodDefs();
		for (MethodDef methodDef : methodDefs) {

			// ignore static and private methods
			if (methodDef.isStatic() || methodDef.isPrivate()) {
				continue;
			}

			if (methodDef.isAbstract()) {
				// add method to list of abstract methods
				abstractMethods.add(methodDef);
			} else {
				// add method to list of concrete methods
				concreteMethods.add(methodDef);
			}
		}

	}

	/**
	 * Check if the given abstract method is implemented by the given concrete method.
	 *
	 * @param abstractMethod Abstract method
	 * @param concreteMethod Concrete method
	 * @return <code>true</code> if concrete method implements abstract method, <code>false</code> otherwise.
	 */
	@SuppressWarnings("RedundantIfStatement")
	private boolean isImplementedBy(MethodDef abstractMethod, MethodDef concreteMethod) {
		if (abstractMethod.getMethodName().equals(concreteMethod.getMethodName())) {
			if (abstractMethod.getMethodDescriptor().equals(concreteMethod.getMethodDescriptor())) {
				return true;
			}
		}
		return false;
	}

	// -----------------------------------------------------------------------------------------------------
	// classes (used as method parameter types and return types, field types, annotations, exceptions, ...)

	private void validateClassRefs(ClassDef classDef, ClassLoader classLoader, AccessCheck accessCheck, Set<String> classIssues) {

		// for every class reference ...
		List<ClassRef> classRefs = classDef.getClassRefs();
		for (ClassRef classRef : classRefs) {
			String className = classRef.getClassName();

			// check if class exists
			Optional<ClassDef> targetClassDef = classLoader.getClassDef(className);
			boolean exists = targetClassDef.isPresent();
			if (!exists) {

				String packageName = JavaUtils.getPackageName(className);
				boolean found = classLoader.containsPackage(packageName);
				if (!found) {
					classIssues.add("Class not found: " + className + " (package not found)");
				} else {
					classIssues.add("Class not found: " + className + " (package found)");
				}

			} else {

				// check access to class
				boolean access = accessCheck.hasAccess(classDef, targetClassDef.get());
				if (!access) {

					// check if a similar issue has already been reported (for superclass or interface declaration)
					String targetClassDisplayName = targetClassDef.get().getDisplayName();
					boolean similarIssueFound = classIssues.contains("Superclass is not accessible: " + targetClassDisplayName) ||
							classIssues.contains("Interface is not accessible: " + targetClassDisplayName);

					if (!similarIssueFound) {
						classIssues.add("Class is not accessible: " + targetClassDisplayName);
					}

				}

			}

		}

	}

	// -----------------------------------------------------------------------------------------------------
	// methods

	private void validateMethodRefs(ClassDef classDef, Classpath classpath, AccessCheck accessCheck, Set<String> classIssues) {

		// for every method reference ...
		List<MethodRef> methodRefs = classDef.getMethodRefs();
		for (MethodRef methodRef : methodRefs) {

			// validate field reference
			MethodSearchResult result = validateMethodRef(classDef, methodRef, accessCheck, classpath);
			if (!result.isIgnoreResult()) {
				String text = result.getResult();
				if (text != null) {
					classIssues.add(text);
				}
			}
		}
	}

	private MethodSearchResult validateMethodRef(ClassDef classDef, MethodRef methodRef, AccessCheck accessCheck, ClassLoader classLoader) {

		MethodSearchResult searchResult = new MethodSearchResult();

		// try to find owner class
		String targetClassName = methodRef.getMethodOwner();
		ClassDef ownerClassDef = classLoader.getClassDef(targetClassName).orElse(null);
		if (ownerClassDef == null) {
			// owner class not found
			searchResult.addErrorMessage("Method not found: " + methodRef.getDisplayName());
			if (reportOwnerClassNotFound) {
				searchResult.addSearchInfo("> " + targetClassName + " (owner class not found)");
			} else {
				// ignore result if owner class is not found
				// (already reported in missing classes)
				searchResult.setIgnoreResult();
			}
			return searchResult;
		}

		// check access to owner class
		boolean access = accessCheck.hasAccess(classDef, ownerClassDef);
		if (!access) {
			String className = classDef.getClassName();
			searchResult.addErrorMessage("Illegal access from " + className + " to class: " + targetClassName);
			return searchResult;
		}

		// find target method definition
		Optional<MethodDef> methodDef = classLoader.getMethodDef(methodRef, searchResult);
		if (!methodDef.isPresent()) {
			searchResult.addErrorMessage("Method not found: " + methodRef.getDisplayName());
			return searchResult;
		}

		// check compatibility between method reference and method definition
		validateMethodDef(classDef, methodRef, methodDef.get(), accessCheck, searchResult);

		return searchResult;
	}

	private void validateMethodDef(ClassDef classDef, MethodRef methodRef, MethodDef methodDef, AccessCheck accessCheck, MethodSearchResult searchResult) {

		// check access to method
		boolean access = accessCheck.hasAccess(classDef, methodDef);
		if (!access) {
			String className = classDef.getClassName();
			searchResult.addErrorMessage("Illegal access from " + className + ": " + methodRef.getDisplayName() + " -> " + methodDef.getDisplayName());
		}

		// check static/instance
		if (methodDef.isStatic()) {
			if (!methodRef.isStaticAccess()) {
				searchResult.addErrorMessage("Instance access to static method: " + methodRef.getDisplayName() + " -> " + methodDef.getDisplayName());
			}
		} else {
			if (methodRef.isStaticAccess()) {
				searchResult.addErrorMessage("Static access to instance method: " + methodRef.getDisplayName() + " -> " + methodDef.getDisplayName());
			}
		}

	}

	// -----------------------------------------------------------------------------------------------------
	// fields

	private void validateFieldRefs(ClassDef classDef, Classpath classpath, AccessCheck accessCheck, Set<String> classIssues) {

		// for every field reference ...
		List<FieldRef> fieldRefs = classDef.getFieldRefs();
		for (FieldRef fieldRef : fieldRefs) {

			// validate field reference
			FieldSearchResult result = validateFieldRef(classDef, fieldRef, accessCheck, classpath);
			if (!result.isIgnoreResult()) {
				String text = result.getResult();
				if (text != null) {
					classIssues.add(text);
				}
			}
		}
	}

	private FieldSearchResult validateFieldRef(ClassDef classDef, FieldRef fieldRef, AccessCheck accessCheck, ClassLoader classLoader) {

		FieldSearchResult searchResult = new FieldSearchResult();

		// try to find owner class
		String targetClassName = fieldRef.getFieldOwner();
		ClassDef ownerClassDef = classLoader.getClassDef(targetClassName).orElse(null);
		if (ownerClassDef == null) {
			// owner class not found
			searchResult.addErrorMessage("Field not found: " + fieldRef.getDisplayName());
			if (reportOwnerClassNotFound) {
				searchResult.addSearchInfo("> " + targetClassName + " (owner class not found)");
			} else {
				// ignore result if owner class is not found
				// (already reported in missing classes)
				searchResult.setIgnoreResult();
			}
			return searchResult;
		}

		// check access to owner class
		boolean access = accessCheck.hasAccess(classDef, ownerClassDef);
		if (!access) {
			String className = classDef.getClassName();
			searchResult.addErrorMessage("Illegal access from " + className + " to class: " + targetClassName);
			return searchResult;
		}

		// find target field definition
		Optional<FieldDef> fieldDef = classLoader.getFieldDef(fieldRef, searchResult);
		if (!fieldDef.isPresent()) {
			searchResult.addErrorMessage("Field not found: " + fieldRef.getDisplayName());
			return searchResult;
		}

		// check compatibility between field reference and field definition
		validateFieldDef(classDef, fieldRef, fieldDef.get(), accessCheck, searchResult);

		return searchResult;
	}

	private void validateFieldDef(ClassDef classDef, FieldRef fieldRef, FieldDef fieldDef, AccessCheck accessCheck, FieldSearchResult searchResult) {

		// check access to field
		boolean access = accessCheck.hasAccess(classDef, fieldDef);
		if (!access) {
			String className = classDef.getClassName();
			searchResult.addErrorMessage("Illegal access from " + className + ": " + fieldRef.getDisplayName() + " -> " + fieldDef.getDisplayName());
		}

		// check field type
		if (!fieldDef.getFieldType().equals(fieldRef.getFieldType())) {
			searchResult.addErrorMessage("Incompatible field type: " + fieldRef.getDisplayName() + " -> " + fieldDef.getDisplayName());
		}

		// check static/instance
		if (fieldDef.isStatic()) {
			if (!fieldRef.isStaticAccess()) {
				searchResult.addErrorMessage("Instance access to static field: " + fieldRef.getDisplayName() + " -> " + fieldDef.getDisplayName());
			}
		} else {
			if (fieldRef.isStaticAccess()) {
				searchResult.addErrorMessage("Static access to instance field: " + fieldRef.getDisplayName() + " -> " + fieldDef.getDisplayName());
			}
		}

		// check access to final fields
		if (fieldDef.isFinal()) {
			if (fieldRef.isWriteAccess()) {
				searchResult.addErrorMessage("Write access to final field: " + fieldRef.getDisplayName() + " -> " + fieldDef.getDisplayName());
			}
		}

	}

	// -----------------------------------------------------------------------------------------------------
	// helper classes

	private abstract static class MemberSearchResult implements ClassLoader.Callback {

		private StringBuilder errorMessages;
		private StringBuilder searchInfos;
		private boolean ignoreResult = false;

		void addErrorMessage(String message) {
			if (errorMessages == null) {
				errorMessages = new StringBuilder();
			} else {
				errorMessages.append(System.lineSeparator());
			}
			errorMessages.append(message);
		}

		void addSearchInfo(String info) {
			if (searchInfos == null) {
				searchInfos = new StringBuilder();
			} else {
				searchInfos.append(System.lineSeparator());
			}
			searchInfos.append(info);
		}

		String getResult() {
			if (errorMessages == null) {
				return null;
			} else {
				if (searchInfos != null) {
					errorMessages.append(System.lineSeparator()).append(searchInfos);
				}
				return errorMessages.toString();
			}
		}

		@SuppressWarnings("BooleanMethodIsAlwaysInverted")
		boolean isIgnoreResult() {
			return ignoreResult;
		}

		void setIgnoreResult() {
			this.ignoreResult = true;
		}

		@Override
		public void classNotFound(String className) {
			addSearchInfo("> " + className + " (class not found)");
		}

	}

	private static class FieldSearchResult extends MemberSearchResult {

		@Override
		public void memberNotFound(String className) {
			addSearchInfo("> " + className + " (field not found)");
		}

		@Override
		public void memberFound(String className) {
			addSearchInfo("> " + className + " (field found)");
		}

	}

	private static class MethodSearchResult extends MemberSearchResult {

		@Override
		public void memberNotFound(String className) {
			addSearchInfo("> " + className + " (method not found)");
		}

		@Override
		public void memberFound(String className) {
			addSearchInfo("> " + className + " (method found)");
		}

	}

}