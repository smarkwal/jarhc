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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jarhc.utils.DigestUtils;
import org.jarhc.utils.JavaUtils;
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
	 * Package name.
	 */
	private String packageName;

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
	 * Fast lookup map for method definition given the method name and descriptor.
	 */
	private final Map<String, Map<String, MethodDef>> methodDefsMap = new HashMap<>();

	/**
	 * List with references to other classes.
	 * This includes:
	 * <p>
	 * - superclass
	 * - implemented interfaces
	 * - permitted subclasses (sealed class)
	 * - outer class
	 * - inner classes
	 * <p>
	 * - type of declared record component
	 * <p>
	 * - type of declared field
	 * <p>
	 * - return type of declared method
	 * - parameter types of declared method
	 * - exceptions thrown by declared method
	 * <p>
	 * - NOT!: annotation on class
	 * - NOT!: annotation on declared record component
	 * - NOT!: annotation on declared method
	 * - NOT!: annotation on declared field
	 * - nested annotation value of an annotation
	 * - annotations not linked to a class, method, or field (module and package?)
	 * <p>
	 * - owner of accessed field
	 * - type of accessed field
	 * <p>
	 * - owner of invoked method
	 * - parameter types of invoked method
	 * - NOT!: return type of invoked method
	 * <p>
	 * - class in Load Constant instruction
	 * - class in Type instruction
	 * - type of declared local variable
	 * - exception in catch block
	 * <p>
	 * - class used as annotation value
	 * - enum value of an annotation (???)
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
	 * Module info.
	 */
	private ModuleInfo moduleInfo = ModuleInfo.UNNAMED;

	/**
	 * Create a class definition for the given class name.
	 *
	 * @param className Class name
	 */
	public ClassDef(String className) {
		super(0);
		this.className = className;
		this.packageName = JavaUtils.getPackageName(className);
	}

	public static ClassDef forClassName(String className) {
		return new ClassDef(className);
	}

	public String getClassName() {
		return className;
	}

	public ClassDef setClassName(String className) {
		this.className = className;
		this.packageName = JavaUtils.getPackageName(className);
		return this;
	}

	public String getPackageName() {
		return packageName;
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
		return interfaceNames;
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
		return permittedSubclassNames;
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
	 * Get a human-readable Java version string based on the class version.
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
		// TODO: add annotations for class, record components, fields, and methods?
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
		return recordComponentDefs;
	}

	public ClassDef addRecordComponentDef(RecordComponentDef recordComponentDef) {
		this.recordComponentDefs.add(recordComponentDef);
		recordComponentDef.setClassDef(this);
		return this;
	}

	public List<FieldDef> getFieldDefs() {
		return fieldDefs;
	}

	public FieldDef getFieldDef(String fieldName) {
		return fieldDefsMap.get(fieldName);
	}

	public ClassDef addFieldDef(FieldDef fieldDef) {
		this.fieldDefs.add(fieldDef);
		fieldDef.setClassDef(this);
		this.fieldDefsMap.put(fieldDef.getFieldName(), fieldDef);
		return this;
	}

	public List<MethodDef> getMethodDefs() {
		return methodDefs;
	}

	public MethodDef getMethodDef(String methodName, String methodDescriptor) {
		Map<String, MethodDef> map = methodDefsMap.get(methodName);
		if (map == null) {
			return null;
		}
		return map.get(methodDescriptor);
	}

	public ClassDef addMethodDef(MethodDef methodDef) {
		this.methodDefs.add(methodDef);
		methodDef.setClassDef(this);

		// add method def to fast lookup map
		String methodName = methodDef.getMethodName();
		String methodDescriptor = methodDef.getMethodDescriptor();
		Map<String, MethodDef> map = this.methodDefsMap.computeIfAbsent(methodName, name -> new HashMap<>(2));
		map.put(methodDescriptor, methodDef);

		return this;
	}

	public List<ClassRef> getClassRefs() {
		return classRefs;
	}

	public ClassDef addClassRef(ClassRef classRef) {
		this.classRefs.add(classRef);
		return this;
	}

	public List<FieldRef> getFieldRefs() {
		return fieldRefs;
	}

	public ClassDef addFieldRef(FieldRef fieldRef) {
		this.fieldRefs.add(fieldRef);
		return this;
	}

	public List<MethodRef> getMethodRefs() {
		return methodRefs;
	}

	public ClassDef addMethodRef(MethodRef methodRef) {
		this.methodRefs.add(methodRef);
		return this;
	}

	@Override
	public ClassDef addAnnotationRef(AnnotationRef annotationRef) {
		super.addAnnotationRef(annotationRef);
		return this;
	}

	public JarFile getJarFile() {
		return jarFile;
	}

	public void setJarFile(JarFile jarFile) {
		this.jarFile = jarFile;
	}

	public ModuleInfo getModuleInfo() {
		return moduleInfo;
	}

	public void setModuleInfo(ModuleInfo moduleInfo) {
		this.moduleInfo = moduleInfo;
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
	@SuppressWarnings("java:S1210") // "equals(Object obj)" should be overridden along with the "compareTo(T obj)" method
	public int compareTo(ClassDef classDef) {
		int diff = this.getClassName().compareTo(classDef.getClassName());
		if (diff != 0) return diff;
		return System.identityHashCode(this) - System.identityHashCode(classDef);
	}

}
