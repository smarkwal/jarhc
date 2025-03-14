/*
 * Copyright 2019 Stephan Markwalder
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileSource implements JarSource {

	private final File file;

	public FileSource(File file) {
		if (file == null) throw new IllegalArgumentException("file");
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getCoordinates() {
		return null; // unknown
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public String toString() {
		return "File " + file.getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		FileSource source = (FileSource) obj;
		return file.equals(source.file);
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

}
