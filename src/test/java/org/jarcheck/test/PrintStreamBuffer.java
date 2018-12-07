package org.jarcheck.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class PrintStreamBuffer extends PrintStream {

	public PrintStreamBuffer() throws UnsupportedEncodingException {
		super(new ByteArrayOutputStream(), true, "UTF-8");
	}

	public String getText() {
		flush();
		ByteArrayOutputStream out = (ByteArrayOutputStream) this.out;
		try {
			return out.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
