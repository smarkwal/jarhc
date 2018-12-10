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

package org.jarhc.app;

import java.io.File;
import java.util.List;

class Options {

	private final int errorCode;
	private final List<File> files;

	public Options(int errorCode) {
		this.errorCode = errorCode;
		this.files = null;
	}

	Options(List<File> files) {
		if (files == null) throw new IllegalArgumentException("files");
		this.errorCode = 0;
		this.files = files;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public List<File> getFiles() {
		return files;
	}

}
