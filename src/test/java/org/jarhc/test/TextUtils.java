package org.jarhc.test;

public class TextUtils {

	/**
	 * Replaces Windows line endings (CRLF) with Unix line endings (LF).
	 */
	public static String toUnixLineSeparators(String text) {
		return text.replace("\r\n", "\n");
	}

}
