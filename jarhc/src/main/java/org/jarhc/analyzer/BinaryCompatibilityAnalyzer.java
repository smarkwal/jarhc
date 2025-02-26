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

import static org.jarhc.utils.Markdown.code;
import static org.jarhc.utils.StringUtils.joinLines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jarhc.app.Options;
import org.jarhc.java.AccessCheck;
import org.jarhc.java.ClassLoader;
import org.jarhc.model.AnnotationRef;
import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.Classpath;
import org.jarhc.model.Def;
import org.jarhc.model.FieldDef;
import org.jarhc.model.FieldRef;
import org.jarhc.model.JarFile;
import org.jarhc.model.MethodDef;
import org.jarhc.model.MethodRef;
import org.jarhc.model.ModuleInfo;
import org.jarhc.model.RecordComponentDef;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.JavaVersion;
import org.jarhc.utils.Markdown;
import org.jarhc.utils.Pool;
import org.jarhc.utils.StringUtils;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public class BinaryCompatibilityAnalyzer implements Analyzer {

	// object pools
	private final Pool<List<MethodDef>> arrayListPool = new Pool<>(ArrayList::new, List::clear);
	private final Pool<Set<String>> hashSetPool = new Pool<>(HashSet::new, Set::clear);
	private final Pool<List<String>> stringListPool = new Pool<>(ArrayList::new, List::clear);
	private final Pool<Set<String>> linkedHashSetPool = new Pool<>(LinkedHashSet::new, Set::clear);
	private final Pool<FieldSearchResult> fieldSearchResultPool = new Pool<>(FieldSearchResult::new, FieldSearchResult::reset);
	private final Pool<MethodSearchResult> methodSearchResultPool = new Pool<>(MethodSearchResult::new, MethodSearchResult::reset);

	private final boolean ignoreMissingAnnotations;
	private final boolean reportOwnerClassNotFound;

	public BinaryCompatibilityAnalyzer(Options options) {
		this(options.isIgnoreMissingAnnotations(), false);
	}

	public BinaryCompatibilityAnalyzer(boolean ignoreMissingAnnotations, boolean reportOwnerClassNotFound) {
		this.ignoreMissingAnnotations = ignoreMissingAnnotations;
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

		ReportTable table = new ReportTable("Artifact", "Issues");

		AccessCheck accessCheck = new AccessCheck(classpath);

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// issues found in JAR file (sorted by class name)
			final Set<String> jarIssues = Collections.synchronizedSet(new TreeSet<>(TextComparator.INSTANCE));

			// validate attributes in META-INF/MANIFEST.MF
			collectManifestIssues(jarFile, classpath, jarIssues);

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			classDefs.parallelStream().forEach(classDef -> collectClassIssues(classDef, classpath, accessCheck, jarIssues));

			if (!jarIssues.isEmpty()) {
				String lines = joinLines(jarIssues).trim();
				table.addRow(jarFile.getDisplayName(), lines);
			}
		}

		return table;
	}

	private void collectManifestIssues(JarFile jarFile, Classpath classpath, Set<String> jarIssues) {

		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		if (manifestAttributes == null) return;

		// validate Main-Class attribute (if present)
		String mainClass = manifestAttributes.get("Main-Class");
		if (mainClass != null) {
			collectMainClassIssues(mainClass, jarFile, jarIssues);
		}

		// validate Class-Path attribute (if present)
		String classPath = manifestAttributes.get("Class-Path");
		if (classPath != null) {
			collectClassPathIssues(classPath, classpath, jarIssues);
		}

	}

	private void collectMainClassIssues(String mainClass, JarFile jarFile, Set<String> jarIssues) {

		Set<String> issues = linkedHashSetPool.doBorrow();

		// check if main class exists
		ClassDef classDef = jarFile.getClassDef(mainClass);
		if (classDef == null) {
			issues.add("Class not found: " + code(mainClass));
		} else {

			// check if main method exists
			MethodDef mainMethodDef = classDef.getMethodDef("main", "([Ljava/lang/String;)V");
			if (mainMethodDef == null) {
				issues.add("Main method not found: " + code("public static void main(String[])"));
			} else {

				// check if main method is public and static
				if (!mainMethodDef.isPublic()) {
					issues.add("Main method is not public: " + code(mainMethodDef.getDisplayName()));
				}
				if (!mainMethodDef.isStatic()) {
					issues.add("Main method is not static: " + code(mainMethodDef.getDisplayName()));
				}
			}
		}

		if (!issues.isEmpty()) {
			String issue = createParentIssue("Main-Class: " + code(mainClass), issues);
			jarIssues.add(issue);
		}

		linkedHashSetPool.doReturn(issues);
	}

	private void collectClassPathIssues(String classPath, Classpath classpath, Set<String> jarIssues) {

		Set<String> issues = linkedHashSetPool.doBorrow();

		// for every class path element ...
		String[] classPathElements = classPath.split(" ");
		for (String classPathElement : classPathElements) {

			// if class path element is a JAR file ...
			if (classPathElement.endsWith(".jar")) {
				JarFile classPathJarFile = classpath.getJarFileByFileName(classPathElement);
				if (classPathJarFile == null) {
					issues.add("JAR file not found: " + code(classPathElement));
				}
			} else { // class path element is a directory path
				issues.add("Element is not a JAR file: " + code(classPathElement));
			}
		}

		if (!issues.isEmpty()) {
			String issue = createParentIssue("Class-Path: " + code(classPath), issues);
			jarIssues.add(issue);
		}

		linkedHashSetPool.doReturn(issues);
	}

	private void collectClassIssues(ClassDef classDef, Classpath classpath, AccessCheck accessCheck, Set<String> jarIssues) {

		// issues found in class definition (in order of appearance)
		Set<String> classIssues = linkedHashSetPool.doBorrow();

		// validate class definition
		validateClassFile(classDef, classIssues);
		validateClassHierarchy(classDef, classpath, accessCheck, classIssues);
		validateAbstractMethods(classDef, classpath, classIssues);
		validateClassRefs(classDef, classpath, accessCheck, classIssues);
		validateMethodRefs(classDef, classpath, accessCheck, classIssues);
		validateFieldRefs(classDef, classpath, accessCheck, classIssues);
		validateAnnotationRefs(classDef, classpath, accessCheck, classIssues);

		if (!classIssues.isEmpty()) {
			String issue = createParentIssue("" + code(classDef.getClassName()), classIssues);
			jarIssues.add(issue);
		}

		linkedHashSetPool.doReturn(classIssues);
	}

	private void validateClassFile(ClassDef classDef, Set<String> classIssues) {
		int release = classDef.getRelease();
		if (release > 8) { // class has been loaded from META-INF/versions/<release>
			int majorClassVersion = classDef.getMajorClassVersion();
			int javaVersion = JavaVersion.getJavaVersionNumber(majorClassVersion);
			if (javaVersion > release) {
				String issue = String.format("Compiled for Java %d, but bundled for Java %d.", javaVersion, release);
				classIssues.add(issue);
			}
		}
	}

	private String createParentIssue(String parentName, Set<String> issues) {
		String lines = issues.stream().map(i -> Markdown.BULLET + " " + i).collect(StringUtils.joinLines());
		return parentName + "\n" + lines + "\n";
	}

	// -----------------------------------------------------------------------------------------------------
	// class hierarchy (superclasses and interfaces)

	private void validateClassHierarchy(ClassDef classDef, Classpath classpath, AccessCheck accessCheck, Set<String> classIssues) {

		// if class has a superclass ...
		String superName = classDef.getSuperName();
		if (superName != null) {
			// check if superclass exists
			ClassDef superClassDef = classpath.getClassDef(superName);
			if (superClassDef == null) {
				classIssues.add("Superclass not found: " + code(superName));
			} else {
				validateSuperclass(superClassDef, classDef, accessCheck, classIssues);
			}
		}

		// for every interface ...
		List<String> interfaceNames = classDef.getInterfaceNames();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < interfaceNames.size(); i++) {
			String interfaceName = interfaceNames.get(i);
			// check if interface exists
			ClassDef interfaceClassDef = classpath.getClassDef(interfaceName);
			if (interfaceClassDef == null) {
				classIssues.add("Interface not found: " + code(interfaceName));
			} else {
				validateInterface(interfaceClassDef, classDef, accessCheck, classIssues);
			}
		}

		// if class is sealed ...
		if (classDef.isSealed()) {

			// for every permitted subclass ...
			List<String> permittedSubclassNames = classDef.getPermittedSubclassNames();
			//noinspection ForLoopReplaceableByForEach (performance)
			for (int i = 0; i < permittedSubclassNames.size(); i++) {
				String permittedSubclassName = permittedSubclassNames.get(i);
				// check if subclass exists
				ClassDef permittedSubclassDef = classpath.getClassDef(permittedSubclassName);
				if (permittedSubclassDef == null) {
					classIssues.add("Permitted subclass not found: " + code(permittedSubclassName));
				} else {
					validatePermittedSubclass(permittedSubclassDef, classDef, accessCheck, classIssues);
				}
			}
		}

	}

	@SuppressWarnings("StatementWithEmptyBody")
	private void validateSuperclass(ClassDef superClass, ClassDef classDef, AccessCheck accessCheck, Set<String> classIssues) {

		// check if class is final
		if (superClass.isFinal()) {
			classIssues.add("Superclass is final: " + code(superClass.getDisplayName()));
		}

		// check if superclass is sealed
		if (superClass.isSealed()) {
			// check if class is permitted subclass
			if (!superClass.getPermittedSubclassNames().contains(classDef.getClassName())) {
				classIssues.add("Class is not a permitted subclass of sealed superclass: " + code(superClass.getDisplayName()));
			} else {
				// check if classes are in same module/package
				validateSealedClassModuleConstraint(classDef, "Sealed superclass", superClass, classIssues);
			}
		}

		// check if class is an annotation, interface, or enum
		if (superClass.isAnnotation()) {
			classIssues.add("Superclass is an annotation: " + code(superClass.getDisplayName()));
		} else if (superClass.isInterface()) {
			classIssues.add("Superclass is an interface: " + code(superClass.getDisplayName()));
		} else if (superClass.isEnum()) {
			if (classDef.isEnum() && classDef.getClassName().startsWith(superClass.getClassName() + "$")) {
				// superclass is outer enum class,
				// current class is inner anonymous class implementing the abstract enum class
			} else {
				classIssues.add("Superclass is an enum: " + code(superClass.getDisplayName()));
			}
		} else if (superClass.isRecord()) {
			classIssues.add("Superclass is a record class: " + code(superClass.getDisplayName()));
		} else {
			// OK (regular class or abstract class)
		}

		// check access to superclass
		boolean access = accessCheck.hasAccess(classDef, superClass);
		if (!access) {
			classIssues.add("Superclass is not accessible: " + code(superClass.getDisplayName()));
		}

	}

	private void validateSealedClassModuleConstraint(ClassDef classDef, String otherClassType, ClassDef otherClassDef, Set<String> classIssues) {

		// sealed superclass and permitted subclass must be
		// - in same named module or
		// - in same package in unnamed module
		ModuleInfo moduleInfo = classDef.getModuleInfo();
		ModuleInfo otherModuleInfo = otherClassDef.getModuleInfo();
		if (otherModuleInfo.isNamed()) {
			if (moduleInfo.isNamed()) {
				// check if both classes are in same module
				if (!otherModuleInfo.isSame(moduleInfo)) {
					classIssues.add(otherClassType + " is not in same module: " + code(otherClassDef.getDisplayName()));
				}
			} else {
				classIssues.add(otherClassType + " is in a named module: " + code(otherClassDef.getDisplayName()));
			}
		} else {
			if (moduleInfo.isNamed()) {
				classIssues.add(otherClassType + " is in unnamed module: " + code(otherClassDef.getDisplayName()));
			} else {
				// check if both classes are in same package
				String className = classDef.getClassName();
				String otherClassName = otherClassDef.getClassName();
				if (!JavaUtils.inSamePackage(otherClassName, className)) {
					classIssues.add(otherClassType + " is not in same package: " + code(otherClassDef.getDisplayName()));
				}
			}
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
			classIssues.add("Interface is an enum: " + code(interfaceClass.getDisplayName()));
		} else if (interfaceClass.isAbstract()) {
			classIssues.add("Interface is an abstract class: " + code(interfaceClass.getDisplayName()));
		} else {
			classIssues.add("Interface is a class: " + code(interfaceClass.getDisplayName()));
		}

		// check access to interface class
		boolean access = accessCheck.hasAccess(classDef, interfaceClass);
		if (!access) {
			classIssues.add("Interface is not accessible: " + code(interfaceClass.getDisplayName()));
		}

	}

	private void validatePermittedSubclass(ClassDef subclassDef, ClassDef classDef, AccessCheck accessCheck, Set<String> classIssues) {

		String superName = subclassDef.getSuperName();
		if (!superName.equals(classDef.getClassName())) {
			classIssues.add("Permitted subclass does not extend sealed class: " + code(subclassDef.getDisplayName()));
		}

		// check if classes are in same module/package
		validateSealedClassModuleConstraint(classDef, "Permitted subclass", subclassDef, classIssues);

		// check access to permitted subclass class
		boolean access = accessCheck.hasAccess(classDef, subclassDef);
		if (!access) {
			classIssues.add("Permitted subclass is not accessible: " + code(subclassDef.getDisplayName()));
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
		List<MethodDef> concreteMethods = arrayListPool.doBorrow();
		List<MethodDef> abstractMethods = arrayListPool.doBorrow();
		List<String> visitedClasses = stringListPool.doBorrow();
		collectMethodDefs(classDef, classpath, concreteMethods, abstractMethods, visitedClasses);

		// for every abstract method ...
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < abstractMethods.size(); i++) {
			MethodDef abstractMethod = abstractMethods.get(i);

			// check if abstract method is implemented
			boolean implemented = false;
			//noinspection ForLoopReplaceableByForEach (performance)
			for (int j = 0; j < concreteMethods.size(); j++) {
				MethodDef concreteMethod = concreteMethods.get(j);
				if (isImplementedBy(abstractMethod, concreteMethod)) {
					implemented = true;
					break;
				}
			}

			if (!implemented) {
				classIssues.add("Abstract method not implemented: " + code(abstractMethod.getDisplayName()));
			}
		}

		// return all collections to pools
		arrayListPool.doReturn(concreteMethods);
		arrayListPool.doReturn(abstractMethods);
		stringListPool.doReturn(visitedClasses);
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
	private void collectMethodDefs(ClassDef classDef, Classpath classpath, List<MethodDef> concreteMethods, List<MethodDef> abstractMethods, List<String> visitedClasses) {

		// do not visit the same class twice
		String className = classDef.getClassName();
		if (visitedClasses.contains(className)) {
			return;
		}
		visitedClasses.add(className);

		// collect methods from superclass
		String superName = classDef.getSuperName();
		if (superName != null) {
			ClassDef superClass = classpath.getClassDef(superName);
			if (superClass != null) {
				collectMethodDefs(superClass, classpath, concreteMethods, abstractMethods, visitedClasses);
			}
		}

		// collect methods from interfaces
		List<String> interfaceNames = classDef.getInterfaceNames();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < interfaceNames.size(); i++) {
			String interfaceName = interfaceNames.get(i);
			ClassDef interfaceDef = classpath.getClassDef(interfaceName);
			if (interfaceDef != null) {
				collectMethodDefs(interfaceDef, classpath, concreteMethods, abstractMethods, visitedClasses);
			}
		}

		// categorize methods in class definition
		List<MethodDef> methodDefs = classDef.getMethodDefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < methodDefs.size(); i++) {
			MethodDef methodDef = methodDefs.get(i);

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
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < classRefs.size(); i++) {
			ClassRef classRef = classRefs.get(i);
			String className = classRef.getClassName();

			// check if class exists
			ClassDef targetClassDef = classLoader.getClassDef(className);
			if (targetClassDef == null) {
				// check if package of class exists (there is at least one class on the classpath)
				validateClassRefPackage("Class", className, classLoader, classIssues);
			} else {
				// check if access to class is allowed
				validateClassRefAccess(classDef, targetClassDef, accessCheck, classIssues);
			}

		}

	}

	private void validateClassRefPackage(String classType, String className, ClassLoader classLoader, Set<String> classIssues) {
		String packageName = JavaUtils.getPackageName(className);
		boolean found = classLoader.containsPackage(packageName);
		if (!found) {
			classIssues.add(classType + " not found: " + code(className) + " (package not found)");
		} else {
			classIssues.add(classType + " not found: " + code(className) + " (package found)");
		}
	}

	private void validateClassRefAccess(ClassDef classDef, ClassDef targetClassDef, AccessCheck accessCheck, Set<String> classIssues) {

		// check access to class
		boolean access = accessCheck.hasAccess(classDef, targetClassDef);
		if (!access) {

			// check if a similar issue has already been reported (for superclass or interface declaration)
			String targetClassDisplayName = targetClassDef.getDisplayName();
			boolean similarIssueFound = classIssues.contains("Superclass is not accessible: " + code(targetClassDisplayName)) ||
					classIssues.contains("Interface is not accessible: " + code(targetClassDisplayName));

			if (!similarIssueFound) {
				if (targetClassDef.isAnnotation()) {
					classIssues.add("Annotation is not accessible: " + code(targetClassDisplayName));
				} else {
					classIssues.add("Class is not accessible: " + code(targetClassDisplayName));
				}
			}
		}

		// if target class is in a different named non-automatic module
		ModuleInfo moduleInfo = classDef.getModuleInfo();
		ModuleInfo targetModuleInfo = targetClassDef.getModuleInfo();
		if (targetModuleInfo.isNamed() && !targetModuleInfo.isAutomatic() && !targetModuleInfo.isSame(moduleInfo)) {

			// check if package of target class is exported to module of source class
			String moduleName = moduleInfo.getModuleName();
			String targetPackageName = targetClassDef.getPackageName();
			if (!targetModuleInfo.isExported(targetPackageName, moduleName)) {
				String targetClassDisplayName = targetClassDef.getDisplayName();
				classIssues.add("Class is not exported by module " + code(targetModuleInfo.getModuleName()) + ": " + code(targetClassDisplayName));
			}

		}

	}

	// -----------------------------------------------------------------------------------------------------
	// methods

	private void validateMethodRefs(ClassDef classDef, Classpath classpath, AccessCheck accessCheck, Set<String> classIssues) {

		// for every method reference ...
		List<MethodRef> methodRefs = classDef.getMethodRefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < methodRefs.size(); i++) {
			MethodRef methodRef = methodRefs.get(i);

			// validate field reference
			MethodSearchResult result = validateMethodRef(classDef, methodRef, accessCheck, classpath);
			if (!result.isIgnoreResult()) {
				String text = result.getResult();
				if (text != null) {
					classIssues.add(text);
				}
			}

			methodSearchResultPool.doReturn(result);
		}
	}

	private MethodSearchResult validateMethodRef(ClassDef classDef, MethodRef methodRef, AccessCheck accessCheck, ClassLoader classLoader) {

		MethodSearchResult searchResult = methodSearchResultPool.doBorrow();

		// try to find owner class
		String targetClassName = methodRef.getMethodOwner();
		ClassDef ownerClassDef = classLoader.getClassDef(targetClassName);
		if (ownerClassDef == null) {
			// owner class not found
			searchResult.addErrorMessage("Method not found: " + code(methodRef.getDisplayName()));
			if (reportOwnerClassNotFound) {
				searchResult.addSearchInfo(targetClassName, "owner class not found");
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
			searchResult.addErrorMessage("Illegal access from " + code(className) + " to class: " + code(targetClassName));
			return searchResult;
		}

		// find target method definition
		MethodDef methodDef = classLoader.getMethodDef(methodRef, searchResult);

		// if method has not been found ...
		if (methodDef == null) {

			// special handling for methods of class MethodHandle or VarHandle
			if (targetClassName.equals("java.lang.invoke.MethodHandle") || targetClassName.equals("java.lang.invoke.VarHandle")) {
				// "rewrite" the descriptor of the method
				MethodRef handleRef = getHandleRef(methodRef);
				// try to find the method again
				methodDef = classLoader.getMethodDef(handleRef, searchResult);
			}

			if (methodDef == null) {
				searchResult.addErrorMessage("Method not found: " + code(methodRef.getDisplayName()));
				return searchResult;
			}
		}

		// check compatibility between method reference and method definition
		validateMethodDef(classDef, methodRef, methodDef, accessCheck, searchResult);

		return searchResult;
	}

	/**
	 * "Rewrite" method reference for MethodHandle and VarHandle objects.
	 *
	 * @param methodRef Method reference
	 * @return Method reference
	 */
	private MethodRef getHandleRef(MethodRef methodRef) {
		String methodOwner = methodRef.getMethodOwner();
		String methodName = methodRef.getMethodName();
		if (methodOwner.equals("java.lang.invoke.MethodHandle")) {
			String methodDescriptor = "([Ljava/lang/Object;)Ljava/lang/Object;";
			return new MethodRef(methodOwner, methodDescriptor, methodName, methodRef.isInterfaceMethod(), methodRef.isStaticAccess());
		} else {
			String methodDescriptor;
			if (methodName.equals("set")
					|| methodName.equals("setOpaque")
					|| methodName.equals("setRelease")
					|| methodName.equals("setVolatile")
			) {
				methodDescriptor = "([Ljava/lang/Object;)V";
			} else if (methodName.equals("compareAndSet")
					|| methodName.equals("weakCompareAndSet")
					|| methodName.equals("weakCompareAndSetPlain")
					|| methodName.equals("weakCompareAndSetAcquire")
					|| methodName.equals("weakCompareAndSetRelease")
			) {
				methodDescriptor = "([Ljava/lang/Object;)Z";
			} else {
				methodDescriptor = "([Ljava/lang/Object;)Ljava/lang/Object;";
			}
			return new MethodRef(methodOwner, methodDescriptor, methodName, methodRef.isInterfaceMethod(), methodRef.isStaticAccess());
		}
	}

	private void validateMethodDef(ClassDef classDef, MethodRef methodRef, MethodDef methodDef, AccessCheck accessCheck, MethodSearchResult searchResult) {

		// check access to method
		boolean access = accessCheck.hasAccess(classDef, methodDef);
		if (!access) {
			String className = classDef.getClassName();
			searchResult.addErrorMessage("Illegal access from " + code(className) + ": " + code(methodRef.getDisplayName()) + " -> " + code(methodDef.getDisplayName()));
		}

		// check static/instance
		if (methodDef.isStatic()) {
			if (!methodRef.isStaticAccess()) {
				searchResult.addErrorMessage("Instance access to static method: " + code(methodRef.getDisplayName()) + " -> " + code(methodDef.getDisplayName()));
			}
		} else {
			if (methodRef.isStaticAccess()) {
				searchResult.addErrorMessage("Static access to instance method: " + code(methodRef.getDisplayName()) + " -> " + code(methodDef.getDisplayName()));
			}
		}

	}

	// -----------------------------------------------------------------------------------------------------
	// fields

	private void validateFieldRefs(ClassDef classDef, Classpath classpath, AccessCheck accessCheck, Set<String> classIssues) {

		// for every field reference ...
		List<FieldRef> fieldRefs = classDef.getFieldRefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < fieldRefs.size(); i++) {
			FieldRef fieldRef = fieldRefs.get(i);

			// validate field reference
			FieldSearchResult result = validateFieldRef(classDef, fieldRef, accessCheck, classpath);
			if (!result.isIgnoreResult()) {
				String text = result.getResult();
				if (text != null) {
					classIssues.add(text);
				}
			}

			fieldSearchResultPool.doReturn(result);
		}
	}

	private FieldSearchResult validateFieldRef(ClassDef classDef, FieldRef fieldRef, AccessCheck accessCheck, ClassLoader classLoader) {

		FieldSearchResult searchResult = fieldSearchResultPool.doBorrow();

		// try to find owner class
		String targetClassName = fieldRef.getFieldOwner();
		ClassDef ownerClassDef = classLoader.getClassDef(targetClassName);
		if (ownerClassDef == null) {
			// owner class not found
			searchResult.addErrorMessage("Field not found: " + code(fieldRef.getDisplayName()));
			if (reportOwnerClassNotFound) {
				searchResult.addSearchInfo(targetClassName, "owner class not found");
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
			searchResult.addErrorMessage("Illegal access from " + code(className) + " to class: " + code(targetClassName));
			return searchResult;
		}

		// find target field definition
		FieldDef fieldDef = classLoader.getFieldDef(fieldRef, searchResult);
		if (fieldDef == null) {
			searchResult.addErrorMessage("Field not found: " + code(fieldRef.getDisplayName()));
			return searchResult;
		}

		// check compatibility between field reference and field definition
		validateFieldDef(classDef, fieldRef, fieldDef, accessCheck, searchResult);

		return searchResult;
	}

	private void validateFieldDef(ClassDef classDef, FieldRef fieldRef, FieldDef fieldDef, AccessCheck accessCheck, FieldSearchResult searchResult) {

		// check access to field
		boolean access = accessCheck.hasAccess(classDef, fieldDef);
		if (!access) {
			String className = classDef.getClassName();
			searchResult.addErrorMessage("Illegal access from " + code(className) + ": " + code(fieldRef.getDisplayName()) + " -> " + code(fieldDef.getDisplayName()));
		}

		// check field type
		if (!fieldDef.getFieldType().equals(fieldRef.getFieldType())) {
			searchResult.addErrorMessage("Incompatible field type: " + code(fieldRef.getDisplayName()) + " -> " + code(fieldDef.getDisplayName()));
		}

		// check static/instance
		if (fieldDef.isStatic()) {
			if (!fieldRef.isStaticAccess()) {
				searchResult.addErrorMessage("Instance access to static field: " + code(fieldRef.getDisplayName()) + " -> " + code(fieldDef.getDisplayName()));
			}
		} else {
			if (fieldRef.isStaticAccess()) {
				searchResult.addErrorMessage("Static access to instance field: " + code(fieldRef.getDisplayName()) + " -> " + code(fieldDef.getDisplayName()));
			}
		}

		// check access to final fields
		if (fieldDef.isFinal()) {
			if (fieldRef.isWriteAccess()) {
				searchResult.addErrorMessage("Write access to final field: " + code(fieldRef.getDisplayName()) + " -> " + code(fieldDef.getDisplayName()));
			}
		}

	}

	// -----------------------------------------------------------------------------------------------------
	// annotations

	private void validateAnnotationRefs(ClassDef classDef, Classpath classpath, AccessCheck accessCheck, Set<String> classIssues) {

		Set<String> annotationIssues = hashSetPool.doBorrow();

		// validate annotations on class
		validateAnnotationRefs(classDef, classDef, classpath, accessCheck, annotationIssues);

		// validate annotations on record components
		List<RecordComponentDef> recordComponentDefs = classDef.getRecordComponentDefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < recordComponentDefs.size(); i++) {
			RecordComponentDef recordComponentDef = recordComponentDefs.get(i);
			validateAnnotationRefs(classDef, recordComponentDef, classpath, accessCheck, annotationIssues);
		}

		// validate annotations on methods
		List<MethodDef> methodDefs = classDef.getMethodDefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < methodDefs.size(); i++) {
			MethodDef methodDef = methodDefs.get(i);
			validateAnnotationRefs(classDef, methodDef, classpath, accessCheck, annotationIssues);
		}

		// validate annotations on fields
		List<FieldDef> fieldDefs = classDef.getFieldDefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < fieldDefs.size(); i++) {
			FieldDef fieldDef = fieldDefs.get(i);
			validateAnnotationRefs(classDef, fieldDef, classpath, accessCheck, annotationIssues);
		}

		// if any annotation issues have been found ...
		if (!annotationIssues.isEmpty()) {
			// sort annotation issues and add them to class issues
			annotationIssues.stream().sorted().forEach(classIssues::add);
		}

		hashSetPool.doReturn(annotationIssues);
	}

	private void validateAnnotationRefs(ClassDef ownerClassDef, Def def, Classpath classpath, AccessCheck accessCheck, Set<String> classIssues) {

		// TODO: get valid target types for concrete Def

		List<AnnotationRef> annotationRefs = def.getAnnotationRefs();
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < annotationRefs.size(); i++) {
			AnnotationRef annotationRef = annotationRefs.get(i);

			String annotationName = annotationRef.getClassName();

			// check if annotation exists
			ClassDef annotationDef = classpath.getClassDef(annotationName);
			if (annotationDef == null) {
				// check if missing annotations should be reported
				if (!ignoreMissingAnnotations) {
					// check if package of annotation exists (there is at least one class on the classpath)
					validateClassRefPackage("Annotation", annotationName, classpath, classIssues);
				}
			} else {
				// check if access to class is allowed
				validateClassRefAccess(ownerClassDef, annotationDef, accessCheck, classIssues);
				// check if target class is an annotation
				if (annotationDef.isAnnotation()) {
					// TODO: check if class is valid annotation target
				} else {
					classIssues.add("Class is not an annotation: " + code(annotationDef.getDisplayName()));
				}
			}

		}
	}

	// -----------------------------------------------------------------------------------------------------
	// helper classes

	private abstract static class MemberSearchResult implements ClassLoader.Callback {

		private final StringBuilder errorMessages = new StringBuilder();
		private final StringBuilder searchInfos = new StringBuilder();
		private boolean ignoreResult = false;

		void addErrorMessage(String message) {
			if (errorMessages.length() > 0) {
				errorMessages.append("\n");
			}
			errorMessages.append(message);
		}

		void addSearchInfo(String className, String message) {
			if (searchInfos.length() > 0) {
				searchInfos.append("\n");
			}
			searchInfos.append("> ").append(code(className)).append(" (").append(message).append(")");
		}

		String getResult() {
			if (errorMessages.length() == 0) {
				return null;
			} else {
				if (searchInfos.length() > 0) {
					errorMessages.append("\n").append(searchInfos);
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
			addSearchInfo(className, "class not found");
		}

		/**
		 * Reset state of this search result object so it can be reused.
		 */
		public void reset() {
			errorMessages.setLength(0);
			searchInfos.setLength(0);
			ignoreResult = false;
		}

	}

	private static class FieldSearchResult extends MemberSearchResult {

		@Override
		public void memberNotFound(String className) {
			addSearchInfo(className, "field not found");
		}

		@Override
		public void memberFound(String className) {
			addSearchInfo(className, "field found");
		}

	}

	private static class MethodSearchResult extends MemberSearchResult {

		@Override
		public void memberNotFound(String className) {
			addSearchInfo(className, "method not found");
		}

		@Override
		public void memberFound(String className) {
			addSearchInfo(className, "method found");
		}

	}

}