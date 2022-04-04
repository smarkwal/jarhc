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
import java.util.Objects;
import org.jarhc.utils.JavaUtils;

public class MethodRef implements Ref, Comparable<MethodRef> {

	private final String methodOwner;
	private final String methodDescriptor;
	private final String methodName;
	private final String returnType;
	private final List<String> parameterTypes;
	private final boolean interfaceMethod;
	private final boolean staticAccess;

	public MethodRef(String methodOwner, String methodDescriptor, String methodName, boolean interfaceMethod, boolean staticAccess) {
		this.methodOwner = methodOwner;
		this.methodDescriptor = methodDescriptor;
		this.methodName = methodName;
		this.interfaceMethod = interfaceMethod;
		this.staticAccess = staticAccess;

		// create return type and parameter types from descriptor
		this.returnType = JavaUtils.getReturnType(methodDescriptor);
		this.parameterTypes = JavaUtils.getParameterTypes(methodDescriptor);
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

	@Override
	public String getDisplayName() {
		int length = (staticAccess ? 7 : 0) + returnType.length() + methodOwner.length() + methodName.length() + 4;
		for (String parameterType : parameterTypes) {
			length += parameterType.length() + 1;
		}
		StringBuilder result = new StringBuilder(length);
		if (staticAccess) {
			result.append("static ");
		}
		result.append(returnType);
		result.append(" ");
		result.append(methodOwner);
		result.append(".");
		result.append(methodName);
		result.append("(");
		boolean first = true;
		for (String parameterType : parameterTypes) {
			if (first) {
				first = false;
			} else {
				result.append(",");
			}
			result.append(parameterType);
		}
		result.append(")");
		return result.toString();
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
