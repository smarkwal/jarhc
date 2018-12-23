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

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

public class FileUtils {

	public static String formatFileSize(long fileSize) {
		if (fileSize < 0) throw new IllegalArgumentException("fileSize");
		if (fileSize < 1024) {
			return String.format("%d B", fileSize);
		}
		double size = fileSize / 1024d;
		if (size < 1024) {
			return String.format("%s KB", formatNumber(size));
		}
		size = size / 1024d;
		return String.format("%s MB", formatNumber(size));
	}

	private static String formatNumber(double number) {
		DecimalFormat format;
		if (number < 10) {
			format = new DecimalFormat("0.00");
		} else if (number < 100) {
			format = new DecimalFormat("0.0");
		} else {
			format = new DecimalFormat("0");
		}
		return format.format(number);
	}

	public static String sha1Hex(File file) throws IOException {
		try (FileInputStream stream = new FileInputStream(file)) {
			return DigestUtils.sha1Hex(stream);
		}
	}

}
