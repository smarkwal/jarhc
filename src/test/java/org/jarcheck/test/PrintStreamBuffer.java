package org.jarcheck.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PrintStreamBuffer extends PrintStream {

	public PrintStreamBuffer() {
		super(new ByteArrayOutputStream(), true);
	}

	public String getText() {
		return out.toString();
	}

}
