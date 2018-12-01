package org.jarcheck;

import org.jarcheck.loader.ClasspathLoader;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;

import java.io.File;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		System.out.println("JarCheck 1.0-SNAPSHOT");
		System.out.println("---------------------");

		CommandLine commandLine = new CommandLine(args);
		int exitCode = commandLine.parse();
		if (exitCode != 0) {
			System.exit(exitCode);
		}

		Options options = commandLine.getOptions();
		File directory = options.getDirectory();

		try {
			ClasspathLoader loader = new ClasspathLoader();
			Classpath classpath = loader.load(directory, true);

			for (JarFile jarFile : classpath.getJarFiles()) {
				System.out.printf("%s: %d%n", jarFile.getFileName(), jarFile.getClassDefs().size());
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}

	}

}