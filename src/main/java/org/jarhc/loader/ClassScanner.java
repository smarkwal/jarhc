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

import org.jarhc.model.*;
import org.jarhc.utils.JavaUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

class ClassScanner {

	private final List<FieldDef> fieldDefs = new ArrayList<>();
	private final List<MethodDef> methodDefs = new ArrayList<>();

	private final Set<ClassRef> classRefs = new TreeSet<>();
	private final Set<FieldRef> fieldRefs = new TreeSet<>();
	private final Set<MethodRef> methodRefs = new TreeSet<>();

	ClassScanner() {
	}

	List<FieldDef> getFieldDefs() {
		return fieldDefs;
	}

	List<MethodDef> getMethodDefs() {
		return methodDefs;
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

		// scan superclass and interfaces
		if (classNode.superName != null) {
			classRefs.add(new ClassRef(classNode.superName));
		}
		if (classNode.interfaces != null) {
			classNode.interfaces.forEach(i -> classRefs.add(new ClassRef(i)));
		}

		// scan fields
		for (FieldNode fieldNode : classNode.fields) {
			scan(fieldNode);
		}

		// scan methods
		for (MethodNode methodNode : classNode.methods) {
			scan(methodNode);
		}

		// scan inner methods
		for (InnerClassNode innerClassNode : classNode.innerClasses) {
			classRefs.add(new ClassRef(innerClassNode.name));
		}

		// scan annotations
		scan(classNode.visibleAnnotations, a -> a.desc);
		scan(classNode.invisibleAnnotations, a -> a.desc);
		scan(classNode.visibleTypeAnnotations, a -> a.desc);
		scan(classNode.invisibleTypeAnnotations, a -> a.desc);

	}

	private void scan(FieldNode fieldNode) {

		// create field definition
		FieldDef fieldDef = new FieldDef(fieldNode.access, fieldNode.name, fieldNode.desc);
		fieldDefs.add(fieldDef);

		// scan field type
		Type fieldType = Type.getType(fieldNode.desc);
		scan(fieldType);

		// TODO: scan field initializer?
		// TODO: scan generic type?

		// scan annotations
		scan(fieldNode.visibleAnnotations, a -> a.desc);
		scan(fieldNode.invisibleAnnotations, a -> a.desc);
		scan(fieldNode.visibleTypeAnnotations, a -> a.desc);
		scan(fieldNode.invisibleTypeAnnotations, a -> a.desc);

	}

	private void scan(MethodNode methodNode) {

		// create method definition
		MethodDef methodDef = new MethodDef(methodNode.access, methodNode.name, methodNode.desc);
		methodDefs.add(methodDef);

		Type methodType = Type.getType(methodNode.desc);

		// scan return type
		Type returnType = methodType.getReturnType();
		scan(returnType);

		// scan parameter types
		Type[] argumentTypes = methodType.getArgumentTypes();
		for (Type argumentType : argumentTypes) {
			scan(argumentType);
		}

		// scan exceptions ("throws" declarations)
		methodNode.exceptions.forEach(e -> classRefs.add(new ClassRef(e)));

		// scan local variables
		List<LocalVariableNode> localVariables = methodNode.localVariables;
		if (localVariables != null) {
			for (LocalVariableNode localVariable : localVariables) {
				String variableName = localVariable.name;
				if (variableName.equals("this")) continue;

				Type variableType = Type.getType(localVariable.desc);
				scan(variableType);
			}
		}

		// scan instructions
		InsnList instructions = methodNode.instructions;
		scan(instructions);

		// scan exception handlers
		List<TryCatchBlockNode> tryCatchBlocks = methodNode.tryCatchBlocks;
		for (TryCatchBlockNode tryCatchBlock : tryCatchBlocks) {
			String exceptionType = tryCatchBlock.type;
			if (exceptionType == null) continue; // finally block
			classRefs.add(new ClassRef(exceptionType));
		}

		// scan annotations
		scan(methodNode.visibleAnnotations, a -> a.desc);
		scan(methodNode.invisibleAnnotations, a -> a.desc);
		scan(methodNode.visibleTypeAnnotations, a -> a.desc);
		scan(methodNode.invisibleTypeAnnotations, a -> a.desc);
		if (methodNode.visibleParameterAnnotations != null) {
			for (List<AnnotationNode> parameterAnnotations : methodNode.visibleParameterAnnotations) {
				scan(parameterAnnotations, a -> a.desc);
			}
		}
		if (methodNode.invisibleParameterAnnotations != null) {
			for (List<AnnotationNode> parameterAnnotations : methodNode.invisibleParameterAnnotations) {
				scan(parameterAnnotations, a -> a.desc);
			}
		}
		scan(methodNode.visibleLocalVariableAnnotations, a -> a.desc);
		scan(methodNode.invisibleLocalVariableAnnotations, a -> a.desc);

	}

	private void scan(InsnList instructions) {

		// for every instruction ...
		for (int i = 0; i < instructions.size(); i++) {

			AbstractInsnNode node = instructions.get(i);
			if (node instanceof MethodInsnNode) {
				MethodInsnNode methodInsnNode = (MethodInsnNode) node;
				scan(methodInsnNode);
			} else if (node instanceof FieldInsnNode) {
				FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
				scan(fieldInsnNode);
			}
			// TODO: scan more instructions?

		}

	}

	private void scan(MethodInsnNode methodInsnNode) {

		String owner = methodInsnNode.owner;
		if (owner.startsWith("[")) {
			Type ownerType = Type.getType(owner);
			scan(ownerType);
		} else {
			classRefs.add(new ClassRef(owner));
		}

		Type methodType = Type.getType(methodInsnNode.desc);

		// scan return type
		Type returnType = methodType.getReturnType();
		scan(returnType);

		// scan parameter types
		Type[] argumentTypes = methodType.getArgumentTypes();
		for (Type argumentType : argumentTypes) {
			scan(argumentType);
		}

		// create reference to method
		int opcode = methodInsnNode.getOpcode();
		boolean staticMethod = opcode == Opcodes.INVOKESTATIC;
		boolean interfaceMethod = opcode == Opcodes.INVOKEINTERFACE || methodInsnNode.itf; // TODO: what has priority?
		MethodRef methodRef = new MethodRef(methodInsnNode.owner, methodInsnNode.desc, methodInsnNode.name, interfaceMethod, staticMethod);
		methodRefs.add(methodRef);

	}

	private void scan(FieldInsnNode fieldInsnNode) {

		String owner = fieldInsnNode.owner;
		if (owner.startsWith("[")) {
			Type ownerType = Type.getType(owner);
			scan(ownerType);
		} else {
			classRefs.add(new ClassRef(owner));
		}

		// scan field type
		Type fieldType = Type.getType(fieldInsnNode.desc);
		scan(fieldType);

		// create reference to field
		int opcode = fieldInsnNode.getOpcode();
		boolean staticField = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC;
		FieldRef fieldRef = new FieldRef(fieldInsnNode.owner, fieldInsnNode.desc, fieldInsnNode.name, staticField);
		fieldRefs.add(fieldRef);

	}

	private <T> void scan(List<T> annotationNodes, Function<T, String> function) {
		if (annotationNodes == null) return;
		for (T annotationNode : annotationNodes) {
			String desc = function.apply(annotationNode);
			Type type = Type.getType(desc);
			scan(type);
			// TODO: scan annotation values?
		}
	}

	private void scan(Type type) {
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
