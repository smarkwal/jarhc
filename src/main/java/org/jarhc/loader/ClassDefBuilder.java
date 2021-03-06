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

import static org.jarhc.utils.JavaUtils.getArrayElementType;
import static org.jarhc.utils.JavaUtils.getFieldType;
import static org.jarhc.utils.JavaUtils.getParameterTypes;
import static org.jarhc.utils.JavaUtils.getReturnType;
import static org.jarhc.utils.JavaUtils.isArrayType;
import static org.jarhc.utils.JavaUtils.isPrimitiveType;
import static org.jarhc.utils.JavaUtils.isVoidType;
import static org.jarhc.utils.JavaUtils.toExternalName;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.jarhc.model.AnnotationRef;
import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.Def;
import org.jarhc.model.FieldDef;
import org.jarhc.model.FieldRef;
import org.jarhc.model.MethodDef;
import org.jarhc.model.MethodRef;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

class ClassDefBuilder extends ClassVisitor {

	private final boolean scanForReferences;

	private final ClassDef classDef = new ClassDef("java.lang.Object");

	private final Set<ClassRef> classRefs = new TreeSet<>();
	private final Set<FieldRef> fieldRefs = new TreeSet<>();
	private final Set<MethodRef> methodRefs = new TreeSet<>();

	private final AnnotationVisitor annotationVisitor = new AnnotationBuilder();

	ClassDefBuilder(boolean scanForReferences) {
		super(Opcodes.ASM7);
		this.scanForReferences = scanForReferences;
	}

	public ClassDef getClassDef() {
		classRefs.forEach(classDef::addClassRef);
		fieldRefs.forEach(classDef::addFieldRef);
		methodRefs.forEach(classDef::addMethodRef);
		return classDef;
	}

	// -------------------------------------------------------------------------------------------------------

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

		classDef.setMajorClassVersion(version & 0xFF);
		classDef.setMinorClassVersion(version >> 16);

		classDef.setAccess(access);

		classDef.setClassName(toExternalName(name));

		// TODO: how to use signature?

		if (superName == null) {
			classDef.setSuperName(null);
		} else {
			superName = toExternalName(superName);
			classDef.setSuperName(superName);

			if (scanForReferences) {
				addClassRef(superName);
			}
		}

		for (String interfaceName : interfaces) {
			interfaceName = toExternalName(interfaceName);
			classDef.addInterfaceName(interfaceName);

			if (scanForReferences) {
				addClassRef(interfaceName);
			}
		}

	}

	@Override
	public void visitOuterClass(String owner, String name, String descriptor) {
		if (scanForReferences) {
			addClassRef(toExternalName(owner));
			// TODO: handle reference to method (name and descriptor) ???
		}
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		if (scanForReferences) {
			addClassRef(toExternalName(name));
			// TODO: what to do with access ???
		}
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		// TODO: what do to with signature and value ???

		String fieldType = getFieldType(descriptor);

		FieldDef fieldDef = new FieldDef(access, name, fieldType);
		classDef.addFieldDef(fieldDef);

		if (scanForReferences) {
			addClassRef(fieldType);
			return new CustomFieldVisitor(fieldDef);
		} else {
			return null;
		}
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		// TODO: what to do with signature ???

		MethodDef methodDef = new MethodDef(access, name, descriptor);
		// TODO: add exceptions to MethodDef
		classDef.addMethodDef(methodDef);

		if (scanForReferences) {
			addMethodSignatureRefs(descriptor, true);
			if (exceptions != null) {
				for (String exceptionClassName : exceptions) {
					exceptionClassName = toExternalName(exceptionClassName);
					addClassRef(exceptionClassName);
				}
			}
			return new CustomMethodVisitor(methodDef);
		} else {
			return null;
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		return addAnnotationRef(descriptor, classDef);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
		return addAnnotationRef(descriptor, null);
	}

	@Override
	public void visitNestHost(String nestHost) {
		// TODO: handle nests ???
		// System.out.println(String.format("ClassVisitor.visitNestHost(%s)", nestHost));
	}

	@Override
	public void visitNestMember(String nestMember) {
		// TODO: handle nests ???
		// System.out.println(String.format("ClassVisitor.visitNestMember(%s)", nestMember));
	}

	@Override
	public void visitAttribute(Attribute attribute) {
		// TODO: handle attribute ???
		// System.out.println(String.format("ClassVisitor.visitAttribute(%s)", attribute));
	}

	@Override
	public void visitSource(String source, String debug) {
		// ignore
	}

	@Override
	public ModuleVisitor visitModule(String name, int access, String version) {
		// ignore
		return null;
	}

	@Override
	public void visitEnd() {
		// nothing to do
	}

	private void addMethodSignatureRefs(String descriptor, boolean isMethodDef) {
		if (isMethodDef) {
			// note: a method definition generates a dependency on the return
			// type, but a method call does not.
			String returnType = getReturnType(descriptor);
			addClassRef(returnType);
		}
		List<String> parameterTypes = getParameterTypes(descriptor);
		for (String parameterType : parameterTypes) {
			addClassRef(parameterType);
		}
	}

	private AnnotationVisitor addAnnotationRef(String descriptor, Def def) {
		if (scanForReferences) {
			String annotationType = Type.getType(descriptor).getClassName();
			addClassRef(annotationType);

			// add annotation to class, method or field definition
			if (def != null) {
				AnnotationRef annotationRef = new AnnotationRef(annotationType);
				def.addAnnotationRef(annotationRef);
			}

			return annotationVisitor;
		} else {
			return null;
		}
	}

	private void addClassRef(String type) {
		if (!isVoidType(type)) {
			if (isArrayType(type)) {
				type = getArrayElementType(type);
			}
			if (!isPrimitiveType(type)) {
				classRefs.add(new ClassRef(type));
			}
		}
	}

	// -------------------------------------------------------------------------------------------------------

	private class CustomFieldVisitor extends FieldVisitor {

		private final FieldDef fieldDef;

		CustomFieldVisitor(FieldDef fieldDef) {
			super(Opcodes.ASM7);
			this.fieldDef = fieldDef;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
			return addAnnotationRef(descriptor, fieldDef);
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
			return addAnnotationRef(descriptor, null);
		}

		@Override
		public void visitAttribute(Attribute attribute) {
			// TODO: handle attribute ???
			// System.out.println(String.format("FieldVisitor.visitAttribute(%s)", attribute));
		}

		@Override
		public void visitEnd() {
			// nothing to do
		}

	}

	// -------------------------------------------------------------------------------------------------------

	private class CustomMethodVisitor extends MethodVisitor {

		private final MethodDef methodDef;

		CustomMethodVisitor(MethodDef methodDef) {
			super(Opcodes.ASM7);
			this.methodDef = methodDef;
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {

			String fieldOwner = toClassName(owner);
			String fieldType = getFieldType(descriptor);
			boolean staticField = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC;
			boolean writeAccess = opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC;

			// ignore write access to final fields in declaring class
			// assumption: instruction is used to initialize the final field
			if (writeAccess) {
				if (classDef.getClassName().equals(fieldOwner)) {
					// check if field is present in current class and is final
					Optional<FieldDef> fieldDef = classDef.getFieldDef(name); // TODO: is it guaranteed that FieldDefs already exist???
					if (fieldDef.isPresent() && fieldDef.get().isFinal()) {
						return;
					}
				}
			}

			addClassRef(fieldOwner);
			addClassRef(fieldType);

			FieldRef fieldRef = new FieldRef(fieldOwner, fieldType, name, staticField, writeAccess);
			fieldRefs.add(fieldRef);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {

			String methodOwner = toClassName(owner);
			addClassRef(methodOwner);
			addMethodSignatureRefs(descriptor, false);

			// create reference to method
			boolean staticMethod = opcode == Opcodes.INVOKESTATIC;
			boolean interfaceMethod = opcode == Opcodes.INVOKEINTERFACE || isInterface; // TODO: what has priority?

			MethodRef methodRef = new MethodRef(methodOwner, descriptor, name, interfaceMethod, staticMethod);
			methodRefs.add(methodRef);
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
			type = toClassName(type);
			addClassRef(type);
		}

		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			if (type == null) {
				// finally block
			} else {
				String exceptionClass = toExternalName(type);
				addClassRef(exceptionClass);
			}
		}

		@Override
		public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
			String variableType = Type.getType(descriptor).getClassName();
			addClassRef(variableType);
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			return annotationVisitor;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
			return addAnnotationRef(descriptor, methodDef);
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
			return addAnnotationRef(descriptor, null);
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
			return addAnnotationRef(descriptor, null);
		}

		@Override
		public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
			return addAnnotationRef(descriptor, null);
		}

		@Override
		public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
			return addAnnotationRef(descriptor, null);
		}

		@Override
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
			return addAnnotationRef(descriptor, null);
		}

		@Override
		public void visitAttribute(Attribute attribute) {
			// TODO: handle attribute ???
			// System.out.println(String.format("MethodVisitor.visitAttribute(%s)", attribute));
		}

		// TODO: override more visit methods?

		@Override
		public void visitEnd() {
			// nothing to do
		}

		private String toClassName(String type) {
			if (type.charAt(0) == '[') {
				type = Type.getType(type).getClassName();
			} else {
				type = toExternalName(type);
			}
			return type;
		}

	}

	private class AnnotationBuilder extends AnnotationVisitor {

		AnnotationBuilder() {
			super(Opcodes.ASM7);
		}

		@Override
		public void visit(String name, Object value) {
			if (scanForReferences) {
				if (name != null) {
					// TODO: create a reference to annotation value
					//  In ClassDef, there is a MethodDef of every field
					/*
					String methodOwner = "";
					String methodDescriptor = "";
					MethodRef methodRef = new MethodRef(methodOwner, methodDescriptor, name, false, false);
					addMethodRef(methodRef);
					*/
				}
				if (value instanceof Type) {
					Type type = (Type) value;
					String className = type.getClassName();
					addClassRef(className);
				}
			}
		}

		@Override
		public void visitEnum(String name, String descriptor, String value) {
			if (scanForReferences) {
				String enumType = Type.getType(descriptor).getClassName();
				addClassRef(enumType);
				// TODO: add a reference to the enum value
			}
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String descriptor) {
			return addAnnotationRef(descriptor, null);
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			return null;
		}

		@Override
		public void visitEnd() {
			// nothing to do
		}

	}

}
