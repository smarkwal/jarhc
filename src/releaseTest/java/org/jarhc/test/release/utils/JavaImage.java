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

package org.jarhc.test.release.utils;

public class JavaImage {

	private final String vendor;
	private final String product;
	private final String version;
	private final String imageName;

	public JavaImage(String vendor, String product, String version, String imageName) {
		this.vendor = vendor;
		this.product = product;
		this.version = version;
		this.imageName = imageName;
	}

	public String getVendor() {
		return vendor;
	}

	public String getProduct() {
		return product;
	}

	public String getVersion() {
		return version;
	}

	public String getImageName() {
		return imageName;
	}

	public String getPath() {
		return String.format("%s-%s/%s", vendor, product, version);
	}

	public String getReportPath(String resourceName) {
		return String.format("reports/%s-%s/%s/%s", vendor, product, version, resourceName);
	}

}
