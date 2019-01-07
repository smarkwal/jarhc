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

import org.jarhc.utils.JavaUtils;

import java.util.ArrayList;
import java.util.List;

public class MethodDef extends AccessFlags {

	private final String methodName;
	private final String methodDescriptor;
	private final String returnType;
	private final List<String> parameterTypes;

	// TODO: exceptions?
	// TODO: annotations? e.g. @Deprecated or @VisibleForTesting
	private ClassDef classDef;

	public MethodDef(int access, String methodName, String methodDescriptor) {
		super(access);
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;

		// create return type and parameter types from descriptor
		this.returnType = JavaUtils.getReturnType(methodDescriptor);
		this.parameterTypes = JavaUtils.getParameterTypes(methodDescriptor);
	}

	public String getMethodName() {
		return methodName;
	}

	public String getMethodDescriptor() {
		return methodDescriptor;
	}

	public String getReturnType() {
		return returnType;
	}

	public List<String> getParameterTypes() {
		return parameterTypes;
	}

	public ClassDef getClassDef() {
		return classDef;
	}

	void setClassDef(ClassDef classDef) {
		this.classDef = classDef;
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
		String modifiers = getModifiers();
		if (modifiers.isEmpty()) {
			return String.format("%s %s(%s)", returnType, methodName, String.join(",", parameterTypes));
		} else {
			return String.format("%s %s %s(%s)", modifiers, returnType, methodName, String.join(",", parameterTypes));
		}
	}

	@Override
	public String toString() {
		return String.format("MethodDef[%s]", getDisplayName());
	}

}
