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

import org.objectweb.asm.Opcodes;

abstract class AccessFlags {

	private int flags;

	AccessFlags(int flags) {
		this.flags = flags;
	}

	public final int getAccess() {
		return flags;
	}

	public void setAccess(int flags) {
		this.flags = flags;
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

	public final boolean isPackagePrivate() {
		return !isPublic() && !isProtected() && !isPrivate();
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
