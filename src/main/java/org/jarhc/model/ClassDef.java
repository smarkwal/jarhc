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

import org.apache.commons.codec.digest.DigestUtils;
import org.jarhc.utils.JavaVersion;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

/**
 * Class definition representing a single Java class file.
 */
public class ClassDef extends AccessFlags implements Comparable<ClassDef> {

	/**
	 * ASM class definition.
	 */
	private final ClassNode classNode;

	/**
	 * Name of the class loader.
	 */
	private final String classLoader;

	/**
	 * Class file checksum.
	 */
	private final String classFileChecksum;

	/**
	 * List of field definitions.
	 */
	private final List<FieldDef> fieldDefs;


	/**
	 * Fast lookup map for field definition given the field name.
	 */
	private final Map<String, FieldDef> fieldDefsMap = new HashMap<>();

	/**
	 * List of method definitions.
	 */
	private final List<MethodDef> methodDefs;

	/**
	 * List with references to other classes.
	 */
	private final List<ClassRef> classRefs;

	/**
	 * List with references to fields.
	 */
	private final List<FieldRef> fieldRefs;

	/**
	 * List with references to methods.
	 */
	private final List<MethodRef> methodRefs;

	/**
	 * Reference to parent JAR file.
	 */
	private JarFile jarFile;

	/**
	 * Create a class definition for the given class and class references.
	 *
	 * @param classNode   ASM class definition
	 * @param classLoader Name of class loader
	 * @param classRefs   References to other classes
	 * @throws IllegalArgumentException If <code>classNode</code> or <code>classRefs</code> is <code>null</code>
	 */
	private ClassDef(ClassNode classNode, String classLoader, String classFileChecksum, List<FieldDef> fieldDefs, List<MethodDef> methodDefs, List<ClassRef> classRefs, List<FieldRef> fieldRefs, List<MethodRef> methodRefs) {
		super(classNode.access);
		if (classNode == null) throw new IllegalArgumentException("classNode");
		if (classRefs == null) throw new IllegalArgumentException("classRefs");
		this.classNode = classNode;
		this.classLoader = classLoader;
		this.classFileChecksum = classFileChecksum;
		this.fieldDefs = new ArrayList<>(fieldDefs);
		this.methodDefs = new ArrayList<>(methodDefs);
		this.classRefs = new ArrayList<>(classRefs);
		this.fieldRefs = new ArrayList<>(fieldRefs);
		this.methodRefs = new ArrayList<>(methodRefs);
		// TODO: remove unused information from class node?

		// for every field definition ...
		this.fieldDefs.forEach(fieldDef -> {

			// set reference to this class definition in field definition
			fieldDef.setClassDef(this);

			// add field definition to fast lookup map
			String fieldName = fieldDef.getFieldName();
			fieldDefsMap.put(fieldName, fieldDef);
		});

		// for every method definition ...
		this.methodDefs.forEach(methodDef -> {

			// set reference to this class definition in method definition
			methodDef.setClassDef(this);

			// TODO: add method definition to fast lookup map?
			// String methodName = methodDef.getMethodName();
			// methodDefsMap.put(methodName, methodDef);
		});

	}

	public String getClassName() {
		return classNode.name;
	}

	public String getSuperName() {
		return classNode.superName;
	}

	public List<String> getInterfaceNames() {
		return Collections.unmodifiableList(classNode.interfaces);
	}

	public int getMajorClassVersion() {
		return classNode.version & 0xFF;
	}

	public int getMinorClassVersion() {
		return classNode.version >> 16;
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

	public String getClassFileChecksum() {
		return classFileChecksum;
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
	private String getApiDescription() {
		StringBuilder api = new StringBuilder();
		api.append(getModifiers()).append(" ").append(classNode.name);
		// add superclass and interfaces
		api.append("\nextends: ").append(classNode.superName);
		api.append("\nimplements: ").append(classNode.interfaces);
		// TODO: add annotations for class, fields and methods?
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

	public List<FieldDef> getFieldDefs() {
		return Collections.unmodifiableList(fieldDefs);
	}

	public Optional<FieldDef> getFieldDef(String fieldName) {
		// TODO: what if there is more than one field with the same name?
		FieldDef fieldDef = fieldDefsMap.get(fieldName);
		return Optional.ofNullable(fieldDef);
	}

	public List<MethodDef> getMethodDefs() {
		return Collections.unmodifiableList(methodDefs);
	}

	public List<ClassRef> getClassRefs() {
		return Collections.unmodifiableList(classRefs);
	}

	public List<FieldRef> getFieldRefs() {
		return Collections.unmodifiableList(fieldRefs);
	}

	public List<MethodRef> getMethodRefs() {
		return Collections.unmodifiableList(methodRefs);
	}

	public JarFile getJarFile() {
		return jarFile;
	}

	void setJarFile(JarFile jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String getModifiers() {
		List<String> parts = new ArrayList<>();

		// access flags
		if (isPublic()) parts.add("public");
		if (isProtected()) parts.add("protected");
		if (isPrivate()) parts.add("private");

		// modifiers
		if (isStatic()) parts.add("static");
		if (isFinal()) parts.add("final");
		if (isVolatile()) parts.add("volatile");
		if (isTransient()) parts.add("transient");
		if (isAbstract()) parts.add("abstract");

		// special flags
		if (isSynthetic()) parts.add("(synthetic)");
		if (isSuper()) parts.add("(super)");
		if (isDeprecated()) parts.add("@Deprecated");

		// type
		if (isInterface()) parts.add("interface");
		if (isAnnotation()) parts.add("@interface");
		if (isEnum()) parts.add("enum");
		if (!isInterface() && !isEnum() && !isAnnotation()) parts.add("class");

		return String.join(" ", parts);
	}

	public String getDisplayName() {
		String className = classNode.name.replace('/', '.');
		String modifiers = getModifiers();
		return String.format("%s %s", modifiers, className);
	}

	@Override
	public String toString() {
		return String.format("ClassDef[%s,%d.%d]", getDisplayName(), getMajorClassVersion(), getMinorClassVersion());
	}

	@Override
	public int compareTo(ClassDef classDef) {
		int diff = this.getClassName().compareTo(classDef.getClassName());
		if (diff != 0) return diff;
		return System.identityHashCode(this) - System.identityHashCode(classDef);
	}

	// BUILDER --------------------------------------------------------------------------------------

	public static Builder forClassNode(ClassNode classNode) {
		return new Builder(classNode);
	}

	public static Builder forClassName(String className) {
		return new Builder(className);
	}

	public static class Builder {

		private final ClassNode classNode;
		private String classLoader = "Classpath";
		private String classFileChecksum;
		private final List<FieldDef> fieldDefs = new ArrayList<>();
		private final List<MethodDef> methodDefs = new ArrayList<>();
		private final List<ClassRef> classRefs = new ArrayList<>();
		private final List<FieldRef> fieldRefs = new ArrayList<>();
		private final List<MethodRef> methodRefs = new ArrayList<>();

		private Builder(ClassNode classNode) {
			this.classNode = classNode;
		}

		private Builder(String className) {
			this.classNode = new ClassNode();
			this.classNode.name = className;
			this.classNode.version = 52; // Java 8
		}

		public Builder withAccess(int access) {
			this.classNode.access = access;
			return this;
		}

		public Builder withVersion(int majorClassVersion, int minorClassVersion) {
			this.classNode.version = majorClassVersion + (minorClassVersion << 16);
			return this;
		}

		public Builder withSuperName(String superName) {
			this.classNode.superName = superName;
			return this;
		}

		public Builder withInterfaceNames(List<String> interfaces) {
			this.classNode.interfaces.addAll(interfaces);
			return this;
		}

		public Builder withClassFileChecksum(String classFileChecksum) {
			this.classFileChecksum = classFileChecksum;
			return this;
		}

		public Builder withClassLoader(String classLoader) {
			this.classLoader = classLoader;
			return this;
		}

		public Builder withFieldDefs(List<FieldDef> fieldDefs) {
			this.fieldDefs.addAll(fieldDefs);
			return this;
		}

		public Builder withMethodDefs(List<MethodDef> methodDefs) {
			this.methodDefs.addAll(methodDefs);
			return this;
		}

		public Builder withClassRefs(List<ClassRef> classRefs) {
			this.classRefs.addAll(classRefs);
			return this;
		}

		public Builder withClassRef(ClassRef classRef) {
			this.classRefs.add(classRef);
			return this;
		}

		public Builder withFieldRefs(List<FieldRef> fieldRefs) {
			this.fieldRefs.addAll(fieldRefs);
			return this;
		}

		public Builder withMethodRefs(List<MethodRef> methodRefs) {
			this.methodRefs.addAll(methodRefs);
			return this;
		}

		public ClassDef build() {
			return new ClassDef(classNode, classLoader, classFileChecksum, fieldDefs, methodDefs, classRefs, fieldRefs, methodRefs);
		}

	}

}
