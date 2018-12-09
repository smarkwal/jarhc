package org.jarhc.loader;

import org.jarhc.model.ClassRef;
import org.jarhc.utils.JavaUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
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
		if (classNode.superName != null) {
			classRefs.add(classNode.superName);
		}
		if (classNode.interfaces != null) {
			classRefs.addAll(classNode.interfaces);
		}

		// scan fields
		for (FieldNode fieldNode : classNode.fields) {
			collectClassRefs(fieldNode);
		}

		// scan methods
		for (MethodNode methodNode : classNode.methods) {
			collectClassRefs(methodNode);
		}

		// scan inner methods
		for (InnerClassNode innerClassNode : classNode.innerClasses) {
			classRefs.add(innerClassNode.name);
		}

		// scan annotations
		collectClassRefs(classNode.visibleAnnotations, a -> a.desc);
		collectClassRefs(classNode.invisibleAnnotations, a -> a.desc);
		collectClassRefs(classNode.visibleTypeAnnotations, a -> a.desc);
		collectClassRefs(classNode.invisibleTypeAnnotations, a -> a.desc);

	}

	private void collectClassRefs(FieldNode fieldNode) {

		// scan field type
		Type fieldType = Type.getType(fieldNode.desc);
		collectClassRefs(fieldType);

		// TODO: scan field initializer?
		// TODO: scan generic type?

		// scan annotations
		collectClassRefs(fieldNode.visibleAnnotations, a -> a.desc);
		collectClassRefs(fieldNode.invisibleAnnotations, a -> a.desc);
		collectClassRefs(fieldNode.visibleTypeAnnotations, a -> a.desc);
		collectClassRefs(fieldNode.invisibleTypeAnnotations, a -> a.desc);

	}

	private void collectClassRefs(MethodNode methodNode) {

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

		// scan annotations
		collectClassRefs(methodNode.visibleAnnotations, a -> a.desc);
		collectClassRefs(methodNode.invisibleAnnotations, a -> a.desc);
		collectClassRefs(methodNode.visibleTypeAnnotations, a -> a.desc);
		collectClassRefs(methodNode.invisibleTypeAnnotations, a -> a.desc);
		if (methodNode.visibleParameterAnnotations != null) {
			for (List<AnnotationNode> parameterAnnotations : methodNode.visibleParameterAnnotations) {
				collectClassRefs(parameterAnnotations, a -> a.desc);
			}
		}
		if (methodNode.invisibleParameterAnnotations != null) {
			for (List<AnnotationNode> parameterAnnotations : methodNode.invisibleParameterAnnotations) {
				collectClassRefs(parameterAnnotations, a -> a.desc);
			}
		}
		collectClassRefs(methodNode.visibleLocalVariableAnnotations, a -> a.desc);
		collectClassRefs(methodNode.invisibleLocalVariableAnnotations, a -> a.desc);

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

		String owner = methodInsnNode.owner;
		if (owner.startsWith("[")) {
			Type ownerType = Type.getType(owner);
			collectClassRefs(ownerType);
		} else {
			classRefs.add(owner);
		}

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

		String owner = fieldInsnNode.owner;
		if (owner.startsWith("[")) {
			Type ownerType = Type.getType(owner);
			collectClassRefs(ownerType);
		} else {
			classRefs.add(owner);
		}

		// scan field type
		Type fieldType = Type.getType(fieldInsnNode.desc);
		collectClassRefs(fieldType);
	}

	private <T> void collectClassRefs(List<T> annotationNodes, Function<T, String> function) {
		if (annotationNodes == null) return;
		for (T annotationNode : annotationNodes) {
			String desc = function.apply(annotationNode);
			Type type = Type.getType(desc);
			collectClassRefs(type);
			// TODO: scan annotation values?
		}
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
