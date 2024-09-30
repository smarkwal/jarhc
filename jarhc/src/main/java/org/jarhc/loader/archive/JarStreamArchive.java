/*
 * Copyright 2022 Stephan Markwalder
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

package org.jarhc.loader.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.jarhc.model.OSGiBundleInfo;
import org.jarhc.utils.ByteBuffer;

public class JarStreamArchive extends Archive {

	private final ChecksumInputStream checksumInputStream;
	private final JarInputStream jarInputStream;
	private final boolean multiRelease;
	private final String automaticModuleName;
	private final Map<String, String> manifestAttributes;
	private final OSGiBundleInfo osgiBundleInfo;

	public JarStreamArchive(InputStream inputStream) throws IOException {
		this.checksumInputStream = new ChecksumInputStream(inputStream);
		this.jarInputStream = new JarInputStream(checksumInputStream, false);

		// if JAR file contains a manifest ...
		Manifest manifest = jarInputStream.getManifest();
		if (manifest != null) {

			// get manifest attributes
			Attributes attributes = manifest.getMainAttributes();

			// check Multi-Release and Automatic-Module-Name attributes
			String value = attributes.getValue("Multi-Release");
			this.multiRelease = value != null && value.equals("true");
			this.automaticModuleName = attributes.getValue("Automatic-Module-Name");

			// read other manifest attributes and separate OSGi Bundle headers
			this.manifestAttributes = new LinkedHashMap<>();
			Map<String, String> osgiBundleHeaders = new LinkedHashMap<>();
			for (Object key : attributes.keySet()) {
				String attributeName = key.toString();
				String attributeValue = attributes.getValue(attributeName);
				if (OSGiBundleInfo.isBundleHeader(attributeName)) {
					osgiBundleHeaders.put(attributeName, attributeValue);
				} else {
					manifestAttributes.put(attributeName, attributeValue);
				}
			}

			// create OSGi Bundle info
			if (!osgiBundleHeaders.isEmpty()) {
				this.osgiBundleInfo = new OSGiBundleInfo(osgiBundleHeaders);
			} else {
				this.osgiBundleInfo = null;
			}

		} else {
			this.multiRelease = false;
			this.automaticModuleName = null;
			this.manifestAttributes = null;
			this.osgiBundleInfo = null;
		}

	}

	@Override
	public long getFileSize() throws IOException {
		return checksumInputStream.getSize();
	}

	@Override
	public String getFileChecksum() throws IOException {
		return checksumInputStream.getChecksum();
	}

	@Override
	public boolean isMultiRelease() {
		return multiRelease;
	}

	@Override
	public String getAutomaticModuleName() {
		return automaticModuleName;
	}

	@Override
	public Map<String, String> getManifestAttributes() {
		return manifestAttributes;
	}

	@Override
	public OSGiBundleInfo getOSGiBundleInfo() {
		return osgiBundleInfo;
	}

	@Override
	public ArchiveEntry getNextEntry() throws IOException {

		while (true) {

			JarEntry jarEntry = jarInputStream.getNextJarEntry();
			if (jarEntry == null) {
				return null;
			}

			// ignore directories
			if (jarEntry.isDirectory()) {
				continue;
			}

			return new JarArchiveEntry(jarEntry);
		}

	}

	@Override
	public void close() throws IOException {
		jarInputStream.close();
	}

	private class JarArchiveEntry implements ArchiveEntry {

		private final JarEntry jarEntry;

		public JarArchiveEntry(JarEntry jarEntry) {
			this.jarEntry = jarEntry;
		}

		@Override
		public String getName() {
			return jarEntry.getName();
		}

		@Override
		public ByteBuffer getData() throws IOException {
			return loadData(jarInputStream);
		}

	}

}


