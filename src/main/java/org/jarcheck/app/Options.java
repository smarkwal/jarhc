package org.jarcheck.app;

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
