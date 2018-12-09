package org.jarhc.model;

import jdk.internal.org.objectweb.asm.Opcodes;

abstract class AccessFlags {

	private final int flags;

	AccessFlags(int flags) {
		this.flags = flags;
	}

	public final int getAccess() {
		return flags;
	}

	public abstract String getModifiers();

	public final boolean isPublic() {
		return hasFlag(Opcodes.ACC_PUBLIC);
	}

	public final boolean isPrivate() {
		return hasFlag(Opcodes.ACC_PRIVATE);
	}

	public final boolean isProtected() {
		return hasFlag(Opcodes.ACC_PROTECTED);
	}

	public final boolean isStatic() {
		return hasFlag(Opcodes.ACC_STATIC);
	}

	public final boolean isFinal() {
		return hasFlag(Opcodes.ACC_FINAL);
	}

	public final boolean isSuper() {
		return hasFlag(Opcodes.ACC_SUPER);
	}

	public final boolean isSynchronized() {
		return hasFlag(Opcodes.ACC_SYNCHRONIZED);
	}

	public final boolean isBridge() {
		return hasFlag(Opcodes.ACC_BRIDGE);
	}

	public final boolean isVolatile() {
		return hasFlag(Opcodes.ACC_VOLATILE);
	}

	public final boolean isVarargs() {
		return hasFlag(Opcodes.ACC_VARARGS);
	}

	public final boolean isTransient() {
		return hasFlag(Opcodes.ACC_TRANSIENT);
	}

	public final boolean isNative() {
		return hasFlag(Opcodes.ACC_NATIVE);
	}

	public final boolean isInterface() {
		return hasFlag(Opcodes.ACC_INTERFACE);
	}

	public final boolean isAbstract() {
		return hasFlag(Opcodes.ACC_ABSTRACT);
	}

	public final boolean isStrict() {
		return hasFlag(Opcodes.ACC_STRICT);
	}

	public final boolean isSynthetic() {
		return hasFlag(Opcodes.ACC_SYNTHETIC);
	}

	public final boolean isAnnotation() {
		return hasFlag(Opcodes.ACC_ANNOTATION);
	}

	public final boolean isEnum() {
		return hasFlag(Opcodes.ACC_ENUM);
	}

	public final boolean isDeprecated() {
		return hasFlag(Opcodes.ACC_DEPRECATED);
	}

	private final boolean hasFlag(int flag) {
		return (flags & flag) != 0;
	}

}
