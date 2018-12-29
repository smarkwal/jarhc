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

package org.jarhc.analyzer;

public class AnalyzerDescription {

	private final String code;
	private final String name;
	private final Class<? extends Analyzer> analyzerClass;

	AnalyzerDescription(String code, String name, Class<? extends Analyzer> analyzerClass) {
		this.code = code;
		this.name = name;
		this.analyzerClass = analyzerClass;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	Class<? extends Analyzer> getAnalyzerClass() {
		return analyzerClass;
	}

}
