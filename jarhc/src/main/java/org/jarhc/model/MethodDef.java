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

import java.util.List;
import org.jarhc.utils.JavaUtils;

public class MethodDef extends MemberDef {

	private final String methodName;
	private final String methodDescriptor;
	// TODO: exceptions?

	public MethodDef(int access, String methodName, String methodDescriptor) {
		super(access);
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;
	}

	public boolean isConstructor() {
		return methodName.equals("<init>");
	}

	public String getMethodName() {
		return methodName;
	}

	public String getMethodDescriptor() {
		return methodDescriptor;
	}

	@Override
	@SuppressWarnings("Duplicates")
	public String getModifiers() {
		List<String> parts = getDefaultModifiers();

		// modifiers
		if (isFinal()) parts.add("final");
		if (isSynchronized()) parts.add("synchronized");
		if (isNative()) parts.add("native");
		if (isAbstract()) parts.add("abstract");
		if (isStrict()) parts.add("strict");

		// special flags
		if (isSynthetic()) parts.add("(synthetic)");
		if (isBridge()) parts.add("(bridge)");
		// if (isVarargs()) parts.add("(varargs)");
		// if (isDeprecated()) parts.add("@Deprecated");

		return String.join(" ", parts);
	}

	@Override
	public String getDisplayName() {
		String modifiers = getModifiers();
		String methodOwner = classDef.getClassName();
		// TODO: improve performance
		String returnType = JavaUtils.getReturnType(methodDescriptor);
		String[] parameterTypes = JavaUtils.getParameterTypes(methodDescriptor);
		if (modifiers.isEmpty()) {
			return String.format("%s %s.%s(%s)", returnType, methodOwner, methodName, String.join(",", parameterTypes));
		} else {
			return String.format("%s %s %s.%s(%s)", modifiers, returnType, methodOwner, methodName, String.join(",", parameterTypes));
		}
	}

	@Override
	public String toString() {
		return String.format("MethodDef[%s]", getDisplayName());
	}

}
