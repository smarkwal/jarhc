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

import java.util.ArrayList;
import java.util.List;

public class FieldDef extends Def {

	private final String fieldName;
	private final String fieldType;
	// TODO: initial value? e.g. constant string containing a class name
	private ClassDef classDef;

	public FieldDef(int access, String fieldName, String fieldType) {
		super(access);
		this.fieldName = fieldName;
		this.fieldType = fieldType;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getFieldType() {
		return fieldType;
	}

	@Override
	public ClassDef getClassDef() {
		return classDef;
	}

	void setClassDef(ClassDef classDef) {
		this.classDef = classDef;
	}

	@Override
	@SuppressWarnings("Duplicates")
	public String getModifiers() {
		List<String> parts = new ArrayList<>();

		// access flags
		if (isPublic()) parts.add("public");
		if (isProtected()) parts.add("protected");
		if (isPrivate()) parts.add("private");

		// modifiers
		if (isStatic()) parts.add("static");
		if (isFinal()) parts.add("final");
		if (isVolatile()) parts.add("volatile");
		if (isTransient()) parts.add("transient");

		// special flags
		if (isSynthetic()) parts.add("(synthetic)");
		// if (isEnum()) parts.add("(enum)");
		// if (isDeprecated()) parts.add("@Deprecated");

		return String.join(" ", parts);
	}

	@Override
	public String getDisplayName() {
		String modifiers = getModifiers();
		String fieldOwner = classDef.getClassName();
		if (modifiers.isEmpty()) {
			return String.format("%s %s.%s", fieldType, fieldOwner, fieldName);
		} else {
			return String.format("%s %s %s.%s", modifiers, fieldType, fieldOwner, fieldName);
		}
	}

	@Override
	public String toString() {
		return String.format("FieldDef[%s]", getDisplayName());
	}

}
