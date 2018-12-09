package org.jarhc.model;

import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodRef implements Comparable<MethodRef> {

	private final String methodOwner;
	private final String methodDescriptor;
	private final String methodName;
	private final boolean interfaceMethod;
	private final boolean staticAccess;

	public MethodRef(String methodOwner, String methodDescriptor, String methodName, boolean interfaceMethod, boolean staticAccess) {
		this.methodOwner = methodOwner;
		this.methodDescriptor = methodDescriptor;
		this.methodName = methodName;
		this.interfaceMethod = interfaceMethod;
		this.staticAccess = staticAccess;
	}

	public String getMethodOwner() {
		return methodOwner;
	}

	public String getMethodDescriptor() {
		return methodDescriptor;
	}

	public String getMethodName() {
		return methodName;
	}

	public boolean isInterfaceMethod() {
		return interfaceMethod;
	}

	public boolean isStaticAccess() {
		return staticAccess;
	}

	public String getDisplayName() {
		String className = this.methodOwner.replace('/', '.');
		Type methodType = Type.getType(methodDescriptor);
		String returnType = methodType.getReturnType().getClassName();
		String argumentTypes = "(" + Arrays.stream(methodType.getArgumentTypes()).map(Type::getClassName).collect(Collectors.joining(",")) + ")";
		if (staticAccess) {
			return String.format("static %s %s.%s%s", returnType, className, methodName, argumentTypes);
		} else {
			return String.format("%s %s.%s%s", returnType, className, methodName, argumentTypes);
		}
	}

	@Override
	public String toString() {
		return String.format("MethodRef[%s]", getDisplayName());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		MethodRef methodRef = (MethodRef) obj;
		return interfaceMethod == methodRef.interfaceMethod &&
				staticAccess == methodRef.staticAccess &&
				Objects.equals(methodOwner, methodRef.methodOwner) &&
				Objects.equals(methodDescriptor, methodRef.methodDescriptor) &&
				Objects.equals(methodName, methodRef.methodName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(methodOwner, methodDescriptor, methodName, interfaceMethod, staticAccess);
	}

	@Override
	public int compareTo(MethodRef methodRef) {
		int diff = methodOwner.compareTo(methodRef.methodOwner);
		if (diff != 0) return diff;
		diff = methodName.compareTo(methodRef.methodName);
		if (diff != 0) return diff;
		diff = methodDescriptor.compareTo(methodRef.methodDescriptor);
		if (diff != 0) return diff;
		diff = Boolean.compare(interfaceMethod, methodRef.interfaceMethod);
		if (diff != 0) return diff;
		return Boolean.compare(staticAccess, methodRef.staticAccess);
	}

}
