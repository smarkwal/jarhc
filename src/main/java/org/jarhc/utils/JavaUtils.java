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

package org.jarhc.utils;

import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaUtils {

	private static final Set<String> primitiveTypes = new HashSet<>();

	static {
		primitiveTypes.add("byte");
		primitiveTypes.add("short");
		primitiveTypes.add("int");
		primitiveTypes.add("long");
		primitiveTypes.add("float");
		primitiveTypes.add("double");
		primitiveTypes.add("char");
		primitiveTypes.add("boolean");
	}

	public static boolean isPrimitiveType(String type) {
		return primitiveTypes.contains(type);
	}

	public static boolean isVoidType(String type) {
		return "void".equals(type);
	}

	public static String getPackageName(String className) {
		if (className.contains(".")) {
			int pos = className.lastIndexOf('.');
			return className.substring(0, pos);
		} else {
			return ""; // empty package
		}
	}

	public static String getReturnType(String methodDescriptor) {
		Type methodType = Type.getType(methodDescriptor);
		return methodType.getReturnType().getClassName();
	}

	public static List<String> getParameterTypes(String methodDescriptor) {
		Type methodType = Type.getType(methodDescriptor);
		return Arrays.stream(methodType.getArgumentTypes()).map(Type::getClassName).collect(Collectors.toList());
	}

	public static String toExternalName(String name) {
		return name.replace('/', '.');
	}

}
