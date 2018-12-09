package org.jarhc.model;

import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodDef extends AccessFlags {

	private final String methodName;
	private final String methodDescriptor;
	// TODO: exceptions?
	// TODO: annotations? e.g. @Deprecated or @VisibleForTesting

	public MethodDef(int access, String methodName, String methodDescriptor) {
		super(access);
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getMethodDescriptor() {
		return methodDescriptor;
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
		if (isSynchronized()) parts.add("synchronized");
		if (isNative()) parts.add("native");
		if (isAbstract()) parts.add("abstract");
		if (isStrict()) parts.add("strict");

		// special flags
		if (isSynthetic()) parts.add("(synthetic)");
		if (isBridge()) parts.add("(bridge)");
		if (isVarargs()) parts.add("(varargs)");
		if (isDeprecated()) parts.add("@Deprecated");

		return String.join(" ", parts);
	}

	public String getDisplayName() {
		Type methodType = Type.getType(methodDescriptor);
		String returnType = methodType.getReturnType().getClassName();
		String argumentTypes = "(" + Arrays.stream(methodType.getArgumentTypes()).map(Type::getClassName).collect(Collectors.joining(",")) + ")";
		String modifiers = getModifiers();
		if (modifiers.isEmpty()) {
			return String.format("%s %s%s", returnType, methodName, argumentTypes);
		} else {
			return String.format("%s %s %s%s", modifiers, returnType, methodName, argumentTypes);
		}
	}

	@Override
	public String toString() {
		return String.format("MethodDef[%s]", getDisplayName());
	}

}
