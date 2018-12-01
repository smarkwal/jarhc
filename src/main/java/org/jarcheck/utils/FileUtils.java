package org.jarcheck.utils;

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

}
