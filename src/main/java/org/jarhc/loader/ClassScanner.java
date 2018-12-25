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

package org.jarhc.loader;

import org.jarhc.model.ClassRef;
import org.jarhc.model.FieldRef;
import org.jarhc.model.MethodRef;
import org.jarhc.utils.JavaUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * A class scanner is used to scan an ASM {@link ClassNode}
 * for references to other classes, fields and methods.
 */
class ClassScanner {

	private ClassNode classNode;

	private final Set<ClassRef> classRefs = new TreeSet<>();
	private final Set<FieldRef> fieldRefs = new TreeSet<>();
	private final Set<MethodRef> methodRefs = new TreeSet<>();

	ClassScanner() {
	}

	Set<ClassRef> getClassRefs() {
		return classRefs;
	}

	Set<FieldRef> getFieldRefs() {
		return fieldRefs;
	}

	Set<MethodRef> getMethodRefs() {
		return methodRefs;
	}

	void scan(ClassNode classNode) {

		this.classNode = classNode;

		// scan superclass and interfaces
		if (classNode.superName != null) {
			classRefs.add(new ClassRef(classNode.superName));
		}
		if (classNode.interfaces != null) {
			classNode.interfaces.forEach(i -> classRefs.add(new ClassRef(i)));
		}

		// scan fields
		for (FieldNode fieldNode : classNode.fields) {
			scanField(fieldNode);
		}

		// scan methods
		for (MethodNode methodNode : classNode.methods) {
			scanMethod(methodNode);
		}

		// scan inner methods
		for (InnerClassNode innerClassNode : classNode.innerClasses) {
			classRefs.add(new ClassRef(innerClassNode.name));
		}

		// scan annotations
		scanAnnotations(classNode.visibleAnnotations, a -> a.desc);
		scanAnnotations(classNode.invisibleAnnotations, a -> a.desc);
		scanAnnotations(classNode.visibleTypeAnnotations, a -> a.desc);
		scanAnnotations(classNode.invisibleTypeAnnotations, a -> a.desc);

	}

	private void scanField(FieldNode fieldNode) {

		// scan field type
		Type fieldType = Type.getType(fieldNode.desc);
		scanType(fieldType);

		// TODO: scan field initializer?
		// TODO: scan generic type?

		// scan annotations
		scanAnnotations(fieldNode.visibleAnnotations, a -> a.desc);
		scanAnnotations(fieldNode.invisibleAnnotations, a -> a.desc);
		scanAnnotations(fieldNode.visibleTypeAnnotations, a -> a.desc);
		scanAnnotations(fieldNode.invisibleTypeAnnotations, a -> a.desc);

	}

	private void scanMethod(MethodNode methodNode) {

		Type methodType = Type.getType(methodNode.desc);
		scanMethodSignature(methodType);

		// scan exceptions ("throws" declarations)
		methodNode.exceptions.forEach(e -> classRefs.add(new ClassRef(e)));

		// scan local variables
		List<LocalVariableNode> localVariables = methodNode.localVariables;
		if (localVariables != null) {
			for (LocalVariableNode localVariable : localVariables) {
				String variableName = localVariable.name;
				if (variableName.equals("this")) continue;

				Type variableType = Type.getType(localVariable.desc);
				scanType(variableType);
			}
		}

		// scan instructions
		InsnList instructions = methodNode.instructions;
		scanInstructions(instructions);

		// scan exception handlers
		List<TryCatchBlockNode> tryCatchBlocks = methodNode.tryCatchBlocks;
		for (TryCatchBlockNode tryCatchBlock : tryCatchBlocks) {
			String exceptionType = tryCatchBlock.type;
			if (exceptionType == null) continue; // finally block
			classRefs.add(new ClassRef(exceptionType));
		}

		// scan annotations
		scanAnnotations(methodNode.visibleAnnotations, a -> a.desc);
		scanAnnotations(methodNode.invisibleAnnotations, a -> a.desc);
		scanAnnotations(methodNode.visibleTypeAnnotations, a -> a.desc);
		scanAnnotations(methodNode.invisibleTypeAnnotations, a -> a.desc);
		if (methodNode.visibleParameterAnnotations != null) {
			for (List<AnnotationNode> parameterAnnotations : methodNode.visibleParameterAnnotations) {
				scanAnnotations(parameterAnnotations, a -> a.desc);
			}
		}
		if (methodNode.invisibleParameterAnnotations != null) {
			for (List<AnnotationNode> parameterAnnotations : methodNode.invisibleParameterAnnotations) {
				scanAnnotations(parameterAnnotations, a -> a.desc);
			}
		}
		scanAnnotations(methodNode.visibleLocalVariableAnnotations, a -> a.desc);
		scanAnnotations(methodNode.invisibleLocalVariableAnnotations, a -> a.desc);

	}

	private void scanMethodSignature(Type methodType) {

		// scan return type
		Type returnType = methodType.getReturnType();
		scanType(returnType);

		// scan parameter types
		Type[] argumentTypes = methodType.getArgumentTypes();
		for (Type argumentType : argumentTypes) {
			scanType(argumentType);
		}

	}

	private void scanInstructions(InsnList instructions) {

		// for every instruction ...
		for (int i = 0; i < instructions.size(); i++) {

			AbstractInsnNode node = instructions.get(i);
			if (node instanceof MethodInsnNode) {
				MethodInsnNode methodInsnNode = (MethodInsnNode) node;
				scanMethodCall(methodInsnNode);
			} else if (node instanceof FieldInsnNode) {
				FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
				scanFieldAccess(fieldInsnNode);
			}
			// TODO: scan more instructions?

		}

	}

	private void scanMethodCall(MethodInsnNode methodInsnNode) {

		String owner = methodInsnNode.owner;
		scanOwner(owner);

		Type methodType = Type.getType(methodInsnNode.desc);
		scanMethodSignature(methodType);

		// create reference to method
		int opcode = methodInsnNode.getOpcode();
		boolean staticMethod = opcode == Opcodes.INVOKESTATIC;
		boolean interfaceMethod = opcode == Opcodes.INVOKEINTERFACE || methodInsnNode.itf; // TODO: what has priority?
		MethodRef methodRef = new MethodRef(methodInsnNode.owner, methodInsnNode.desc, methodInsnNode.name, interfaceMethod, staticMethod);
		methodRefs.add(methodRef);

	}

	private void scanOwner(String owner) {
		if (owner.startsWith("[")) {
			Type ownerType = Type.getType(owner);
			scanType(ownerType);
		} else {
			classRefs.add(new ClassRef(owner));
		}
	}

	private void scanFieldAccess(FieldInsnNode fieldInsnNode) {

		String fieldOwner = fieldInsnNode.owner;
		scanOwner(fieldOwner);

		// scan field type
		Type fieldType = Type.getType(fieldInsnNode.desc);
		scanType(fieldType);

		String fieldName = fieldInsnNode.name;
		int opcode = fieldInsnNode.getOpcode();
		boolean staticField = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC;
		boolean writeAccess = opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC;

		// ignore write access to final fields in declaring class
		// assumption: instruction is used to initialize the final field
		if (writeAccess) {
			if (classNode.name.equals(fieldOwner)) {
				// check if field is present in current class and is final
				FieldNode fieldNode = classNode.fields.stream().filter(f -> f.name.equals(fieldName)).findAny().orElse(null);
				if (fieldNode != null) {
					boolean isFinal = (fieldNode.access & Opcodes.ACC_FINAL) != 0;
					if (isFinal) {
						return;
					}
				}
			}
		}

		// create reference to field
		FieldRef fieldRef = new FieldRef(fieldOwner, fieldInsnNode.desc, fieldName, staticField, writeAccess);
		fieldRefs.add(fieldRef);

	}

	private <T> void scanAnnotations(List<T> annotationNodes, Function<T, String> function) {
		if (annotationNodes == null) return;
		for (T annotationNode : annotationNodes) {
			String desc = function.apply(annotationNode);
			Type type = Type.getType(desc);
			scanType(type);
			// TODO: scan annotation values?
		}
	}

	private void scanType(Type type) {
		String className = type.getClassName();

		// get array element type
		while (className.endsWith("[]")) {
			className = className.substring(0, className.length() - 2);
		}

		// ignore void and primitive types
		if (JavaUtils.isVoidType(className)) {
			return;
		} else if (JavaUtils.isPrimitiveType(className)) {
			return;
		}

		// convert to internal class name
		className = className.replace('.', '/');

		classRefs.add(new ClassRef(className));
	}

}
