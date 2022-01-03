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

public class RecordComponentDef extends MemberDef {

	private final String name;
	private final String type;

	public RecordComponentDef(String name, String type) {
		super(0);
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	@Override
	@SuppressWarnings("Duplicates")
	public String getModifiers() {
		return "";
	}

	@Override
	public String getDisplayName() {
		String recordComponentOwner = classDef.getClassName();
		return String.format("%s %s.%s", type, recordComponentOwner, name);
	}

	@Override
	public String toString() {
		return String.format("RecordClassDef[%s]", getDisplayName());
	}

}
