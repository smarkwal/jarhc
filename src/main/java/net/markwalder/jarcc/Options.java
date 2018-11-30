package net.markwalder.jarcc;

import java.io.File;

class Options {

	private final File directory;

	Options(File directory) {
		if (directory == null) throw new IllegalArgumentException("directory");
		this.directory = directory;
	}

	File getDirectory() {
		return directory;
	}

}
