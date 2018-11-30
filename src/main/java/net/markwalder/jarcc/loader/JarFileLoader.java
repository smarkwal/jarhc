package net.markwalder.jarcc.loader;

import net.markwalder.jarcc.model.ClassDef;
import net.markwalder.jarcc.model.JarFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

public class JarFileLoader {

	public JarFile load(File file) throws IOException {

		ClassDefLoader classDefLoader = new ClassDefLoader();
		List<ClassDef> classDefs = new ArrayList<>();

		// open JAR file for reading
		try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(file)) {

			// for every entry in the JAR file ...
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();

				// skip directories
				if (entry.isDirectory()) {
					continue;
				}

				// only accept *.class files
				if (!entry.getName().endsWith(".class")) {
					continue;
				}

				// load class file
				InputStream stream = jarFile.getInputStream(entry);
				ClassDef classDef = classDefLoader.load(stream);

				classDefs.add(classDef);
			}
		}

		return new JarFile(file.getName(), classDefs);
	}

}
