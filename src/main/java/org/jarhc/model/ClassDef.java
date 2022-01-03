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

package org.jarhc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jarhc.utils.DigestUtils;
import org.jarhc.utils.JavaVersion;

/**
 * Class definition representing a single Java class file.
 */
public class ClassDef extends Def implements Comparable<ClassDef> {

	/**
	 * Class name.
	 */
	private String className;

	/**
	 * Version branch in multi-release JAR file.
	 */
	private int release;

	/**
	 * Name of superclass.
	 */
	private String superName = "java.lang.Object";

	/**
	 * List of interfaces implemented by this class.
	 */
	private final List<String> interfaceNames = new ArrayList<>();

	/**
	 * List of permitted subclasses of this class.
	 * Used for sealed classes introduced in Java 17.
	 */
	private final List<String> permittedSubclassNames = new ArrayList<>();

	/**
	 * Major class file version.
	 */
	private int majorClassVersion = 52;

	/**
	 * Minor class file version.
	 */
	private int minorClassVersion = 0;

	/**
	 * Name of the class loader used to load this class.
	 */
	private String classLoader = "Classpath";

	/**
	 * Class file checksum.
	 */
	private String classFileChecksum = null;

	/**
	 * List of record component definitions.
	 */
	private final List<RecordComponentDef> recordComponentDefs = new ArrayList<>();

	/**
	 * List of field definitions.
	 */
	private final List<FieldDef> fieldDefs = new ArrayList<>();

	/**
	 * Fast lookup map for field definition given the field name.
	 */
	private final Map<String, FieldDef> fieldDefsMap = new HashMap<>();

	/**
	 * List of method definitions.
	 */
	private final List<MethodDef> methodDefs = new ArrayList<>();

	/**
	 * List with references to other classes.
	 */
	private final List<ClassRef> classRefs = new ArrayList<>();

	/**
	 * List with references to fields.
	 */
	private final List<FieldRef> fieldRefs = new ArrayList<>();

	/**
	 * List with references to methods.
	 */
	private final List<MethodRef> methodRefs = new ArrayList<>();

	/**
	 * Reference to parent JAR file.
	 */
	private JarFile jarFile;

	/**
	 * Create a class definition for the given class name.
	 *
	 * @param className Class name
	 */
	public ClassDef(String className) {
		super(0);
		this.className = className;
	}

	public static ClassDef forClassName(String className) {
		return new ClassDef(className);
	}

	public String getClassName() {
		return className;
	}

	public ClassDef setClassName(String className) {
		this.className = className;
		return this;
	}

	public ClassDef withAccess(int flags) {
		setAccess(flags);
		return this;
	}

	public boolean isRegularClass() {
		if (className.equals("module-info")) {
			return false;
		} else if (className.endsWith(".package-info")) {
			return false;
		} else {
			return true;
		}
	}

	public int getRelease() {
		return release;
	}

	public ClassDef setRelease(int release) {
		this.release = release;
		return this;
	}

	public String getSuperName() {
		return superName;
	}

	public ClassDef setSuperName(String superName) {
		this.superName = superName;
		return this;
	}

	public List<String> getInterfaceNames() {
		return Collections.unmodifiableList(interfaceNames);
	}

	public ClassDef addInterfaceNames(List<String> interfaceNames) {
		interfaceNames.forEach(this::addInterfaceName);
		return this;
	}

	public ClassDef addInterfaceName(String interfaceName) {
		this.interfaceNames.add(interfaceName);
		return this;
	}

	public List<String> getPermittedSubclassNames() {
		return Collections.unmodifiableList(permittedSubclassNames);
	}

	public ClassDef addPermittedSubclassNames(List<String> permittedSubclassNames) {
		permittedSubclassNames.forEach(this::addPermittedSubclassName);
		return this;
	}

	public ClassDef addPermittedSubclassName(String permittedSubclassName) {
		this.permittedSubclassNames.add(permittedSubclassName);
		return this;
	}

	public boolean isSealed() {
		return !permittedSubclassNames.isEmpty();
	}

	@Override
	public ClassDef getClassDef() {
		return this;
	}

	public int getMajorClassVersion() {
		return majorClassVersion;
	}

	public ClassDef setMajorClassVersion(int majorClassVersion) {
		this.majorClassVersion = majorClassVersion;
		return this;
	}

	public int getMinorClassVersion() {
		return minorClassVersion;
	}

	public ClassDef setMinorClassVersion(int minorClassVersion) {
		this.minorClassVersion = minorClassVersion;
		return this;
	}

	/**
	 * Get a human readable Java version string based on the class version.
	 *
	 * @return Java version string (examples: "Java 1.4", "Java 8")
	 * @see JavaVersion#fromClassVersion(int)
	 */
	public String getJavaVersion() {
		return JavaVersion.fromClassVersion(getMajorClassVersion());
	}

	public String getClassLoader() {
		return classLoader;
	}

	public ClassDef setClassLoader(String classLoader) {
		this.classLoader = classLoader;
		return this;
	}

	public String getClassFileChecksum() {
		return classFileChecksum;
	}

	public ClassDef setClassFileChecksum(String classFileChecksum) {
		this.classFileChecksum = classFileChecksum;
		return this;
	}

	/**
	 * Calculate a checksum over all non-private API elements
	 * (modifiers, superclass, interfaces, fields, methods, ...)
	 *
	 * @return SHA-1 checksum
	 */
	public String getApiChecksum() {
		String code = getApiDescription();
		return DigestUtils.sha1Hex(code);
	}

	/**
	 * Get a description of the non-private API of this class.
	 *
	 * @return API description
	 */
	public String getApiDescription() {
		StringBuilder api = new StringBuilder();
		api.append(getModifiers()).append(" ").append(className);
		// add superclass and interfaces
		api.append("\nextends: ").append(superName);
		api.append("\nimplements: ").append(interfaceNames);
		api.append("\npermits: ").append(permittedSubclassNames);
		// TODO: add annotations for class, recorc components, fields, and methods?
		// add all record components
		recordComponentDefs.stream()
				.map(RecordComponentDef::getDisplayName) // get component description
				.sorted() // sort components
				.forEach(f -> api.append("\nrecord component: ").append(f));
		// add all fields
		fieldDefs.stream()
				.filter(f -> !f.isPrivate()) // ignore private fields
				.filter(f -> !f.isSynthetic()) // ignore synthetic fields
				.map(FieldDef::getDisplayName) // get field description
				.sorted() // sort fields
				.forEach(f -> api.append("\nfield: ").append(f));
		// add all methods (including constructors)
		methodDefs.stream()
				.filter(m -> !m.isPrivate()) // ignore private methods
				.filter(m -> !m.isSynthetic()) // ignore synthetic methods
				.map(MethodDef::getDisplayName) // get method description
				// TODO: add throws exceptions?
				.sorted() // sort methods
				.forEach(m -> api.append("\nmethod: ").append(m));
		return api.toString();
	}

	public List<RecordComponentDef> getRecordComponentDefs() {
		return Collections.unmodifiableList(recordComponentDefs);
	}

	public ClassDef addRecordComponentDef(RecordComponentDef recordComponentDef) {
		this.recordComponentDefs.add(recordComponentDef);
		recordComponentDef.setClassDef(this);
		return this;
	}

	public List<FieldDef> getFieldDefs() {
		return Collections.unmodifiableList(fieldDefs);
	}

	public Optional<FieldDef> getFieldDef(String fieldName) {
		// TODO: what if there is more than one field with the same name?
		FieldDef fieldDef = fieldDefsMap.get(fieldName);
		return Optional.ofNullable(fieldDef);
	}

	public ClassDef addFieldDef(FieldDef fieldDef) {
		this.fieldDefs.add(fieldDef);
		fieldDef.setClassDef(this);
		this.fieldDefsMap.put(fieldDef.getFieldName(), fieldDef);
		return this;
	}

	public List<MethodDef> getMethodDefs() {
		return Collections.unmodifiableList(methodDefs);
	}

	public Optional<MethodDef> getMethodDef(String methodName, String methodDescriptor) {
		// TODO: use methodDefsMap for fast look-up
		return methodDefs.stream()
				.filter(m -> m.getMethodName().equals(methodName))
				.filter(m -> m.getMethodDescriptor().equals(methodDescriptor))
				.findFirst();
	}

	public ClassDef addMethodDef(MethodDef methodDef) {
		this.methodDefs.add(methodDef);
		methodDef.setClassDef(this);
		return this;
	}

	public List<ClassRef> getClassRefs() {
		return Collections.unmodifiableList(classRefs);
	}

	public ClassDef addClassRef(ClassRef classRef) {
		this.classRefs.add(classRef);
		return this;
	}

	public List<FieldRef> getFieldRefs() {
		return Collections.unmodifiableList(fieldRefs);
	}

	public ClassDef addFieldRef(FieldRef fieldRef) {
		this.fieldRefs.add(fieldRef);
		return this;
	}

	public List<MethodRef> getMethodRefs() {
		return Collections.unmodifiableList(methodRefs);
	}

	public ClassDef addMethodRef(MethodRef methodRef) {
		this.methodRefs.add(methodRef);
		return this;
	}

	public JarFile getJarFile() {
		return jarFile;
	}

	void setJarFile(JarFile jarFile) {
		this.jarFile = jarFile;
	}

	public ModuleInfo getModuleInfo() {
		if (jarFile == null) {
			return ModuleInfo.UNNAMED;
		}
		return jarFile.getModuleInfo();
	}

	@Override
	@SuppressWarnings("Duplicates")
	public String getModifiers() {
		List<String> parts = getDefaultModifiers();

		// modifiers
		if (isFinal() && !isEnum() && !isRecord()) parts.add("final");
		if (isVolatile()) parts.add("volatile");
		if (isTransient()) parts.add("transient");
		if (isAbstract() && !isInterface()) parts.add("abstract");
		if (isSealed()) parts.add("sealed");

		// special flags
		if (isSynthetic()) parts.add("(synthetic)");
		// if (isSuper()) parts.add("(super)");
		// if (isDeprecated()) parts.add("@Deprecated");

		// type
		if (isAnnotation()) {
			parts.add("@interface");
		} else if (isEnum()) {
			parts.add("enum");
		} else if (isInterface()) {
			parts.add("interface");
		} else if (isRecord()) {
			parts.add("record");
		} else {
			parts.add("class");
		}

		return String.join(" ", parts);
	}

	public String getType() {
		if (isAnnotation()) {
			return "annotation";
		} else if (isEnum()) {
			return "enum";
		} else if (isInterface()) {
			return "interface";
		} else if (isRecord()) {
			return "record";
		} else if (isAbstract()) {
			return "abstract class";
		} else {
			return "class";
		}
	}

	@Override
	public String getDisplayName() {
		String modifiers = getModifiers();
		return String.format("%s %s", modifiers, className);
	}

	@Override
	public String toString() {
		return String.format("ClassDef[%s,%d.%d]", getDisplayName(), majorClassVersion, minorClassVersion);
	}

	@Override
	public int compareTo(ClassDef classDef) {
		int diff = this.getClassName().compareTo(classDef.getClassName());
		if (diff != 0) return diff;
		return System.identityHashCode(this) - System.identityHashCode(classDef);
	}

}
