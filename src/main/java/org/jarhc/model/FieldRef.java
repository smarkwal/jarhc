package org.jarhc.model;

import org.objectweb.asm.Type;

import java.util.Objects;

public class FieldRef {

	private final String fieldOwner;
	private final String fieldDescriptor;
	private final String fieldName;
	private final boolean staticAccess;

	public FieldRef(String fieldOwner, String fieldDescriptor, String fieldName, boolean staticAccess) {
		this.fieldOwner = fieldOwner;
		this.fieldDescriptor = fieldDescriptor;
		this.fieldName = fieldName;
		this.staticAccess = staticAccess;
	}

	public String getFieldOwner() {
		return fieldOwner;
	}

	public String getFieldDescriptor() {
		return fieldDescriptor;
	}

	public String getFieldName() {
		return fieldName;
	}

	public boolean isStaticAccess() {
		return staticAccess;
	}

	public String getDisplayName() {
		String className = this.fieldOwner.replace('/', '.');
		String fieldType = Type.getType(fieldDescriptor).getClassName();
		if (staticAccess) {
			return String.format("static %s %s.%s", fieldType, className, fieldName);
		} else {
			return String.format("%s %s.%s", fieldType, className, fieldName);
		}
	}

	@Override
	public String toString() {
		return String.format("FieldRef[%s]", getDisplayName());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		FieldRef fieldRef = (FieldRef) obj;
		return staticAccess == fieldRef.staticAccess &&
				Objects.equals(fieldOwner, fieldRef.fieldOwner) &&
				Objects.equals(fieldDescriptor, fieldRef.fieldDescriptor) &&
				Objects.equals(fieldName, fieldRef.fieldName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fieldOwner, fieldDescriptor, fieldName, staticAccess);
	}

}
