package org.jarcheck.utils;

public class StringUtils {

	public static String repeat(String text, int count) {
		if (count < 0) throw new IllegalArgumentException("count");
		if (count == 0 || text == null) return "";
		if (count == 1) return text;
		StringBuilder buffer = new StringBuilder(text.length() * count);
		while (count > 0) {
			buffer.append(text);
			count--;
		}
		return buffer.toString();
	}

}
