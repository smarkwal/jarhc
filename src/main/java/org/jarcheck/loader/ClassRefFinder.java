package org.jarcheck.loader;

import org.jarcheck.model.ClassRef;
import org.jarcheck.utils.JavaUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

class ClassRefFinder {

	static List<ClassRef> findClassRefs(ClassNode classNode) {
		ClassRefFinder finder = new ClassRefFinder();
		finder.collectClassRefs(classNode);
		Set<String> classRefs = finder.getClassRefs();
		return classRefs.stream().map(ClassRef::new).collect(Collectors.toList());
	}

	private final Set<String> classRefs = new TreeSet<>();

	private ClassRefFinder() {
	}

	private Set<String> getClassRefs() {
		return classRefs;
	}

	private void collectClassRefs(ClassNode classNode) {

		// scan superclass and interfaces
		classRefs.add(classNode.superName);
		classRefs.addAll(classNode.interfaces);

		// TODO: scan annotations

		// scan fields
		for (FieldNode fieldNode : classNode.fields) {
			collectClassRefs(fieldNode);
		}

		// scan methods
		for (MethodNode methodNode : classNode.methods) {
			collectClassRefs(methodNode);
		}
	}

	private void collectClassRefs(FieldNode fieldNode) {
		// TODO: scan annotations

		// scan field type
		Type fieldType = Type.getType(fieldNode.desc);
		collectClassRefs(fieldType);

		// TODO: scan field initializer?
		// TODO: scan generic type?
	}

	private void collectClassRefs(MethodNode methodNode) {
		// TODO: annotations

		Type methodType = Type.getType(methodNode.desc);

		// scan return type
		Type returnType = methodType.getReturnType();
		collectClassRefs(returnType);

		// scan parameter types
		Type[] argumentTypes = methodType.getArgumentTypes();
		for (Type argumentType : argumentTypes) {
			collectClassRefs(argumentType);
		}

		// scan exceptions ("throws" declarations)
		classRefs.addAll(methodNode.exceptions);

		// scan local variables
		List<LocalVariableNode> localVariables = methodNode.localVariables;
		if (localVariables != null) {
			for (LocalVariableNode localVariable : localVariables) {
				String variableName = localVariable.name;
				if (variableName.equals("this")) continue;

				Type variableType = Type.getType(localVariable.desc);
				collectClassRefs(variableType);
			}
		}

		// scan instructions
		InsnList instructions = methodNode.instructions;
		collectClassRefs(instructions);

		// scan exception handlers
		List<TryCatchBlockNode> tryCatchBlocks = methodNode.tryCatchBlocks;
		for (TryCatchBlockNode tryCatchBlock : tryCatchBlocks) {
			String exceptionType = tryCatchBlock.type;
			if (exceptionType == null) continue; // finally block
			classRefs.add(exceptionType);
		}

	}

	private void collectClassRefs(InsnList instructions) {

		// for every instruction ...
		for (int i = 0; i < instructions.size(); i++) {

			AbstractInsnNode node = instructions.get(i);
			if (node instanceof MethodInsnNode) {
				MethodInsnNode methodInsnNode = (MethodInsnNode) node;
				collectClassRefs(methodInsnNode);
			} else if (node instanceof FieldInsnNode) {
				FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
				collectClassRefs(fieldInsnNode);
			}
			// TODO: scan more instructions?

		}

	}

	private void collectClassRefs(MethodInsnNode methodInsnNode) {
		classRefs.add(methodInsnNode.owner);

		Type methodType = Type.getType(methodInsnNode.desc);

		// scan return type
		Type returnType = methodType.getReturnType();
		collectClassRefs(returnType);

		// scan parameter types
		Type[] argumentTypes = methodType.getArgumentTypes();
		for (Type argumentType : argumentTypes) {
			collectClassRefs(argumentType);
		}
	}

	private void collectClassRefs(FieldInsnNode fieldInsnNode) {
		classRefs.add(fieldInsnNode.owner);

		// scan field type
		Type fieldType = Type.getType(fieldInsnNode.desc);
		collectClassRefs(fieldType);
	}

	private void collectClassRefs(Type type) {
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

		classRefs.add(className);
	}

}
