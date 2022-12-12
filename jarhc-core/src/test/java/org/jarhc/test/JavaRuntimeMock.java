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

package org.jarhc.test;

import static org.jarhc.utils.JavaUtils.getPackageName;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jarhc.TestUtils;
import org.jarhc.env.JavaRuntime;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.utils.FileUtils;

public class JavaRuntimeMock extends JavaRuntime {

	private static JavaRuntime oracleRuntime;

	public static JavaRuntime getOracleRuntime() {
		if (oracleRuntime == null) {
			oracleRuntime = createOracleRuntime();
		}
		return oracleRuntime;
	}

	public static JavaRuntime createOracleRuntime() {
		return new JavaRuntimeMock("/classes-oracle-jdk-1.8.0_144.dat.gz");
	}

	private final Map<String, ClassDef> classDefs = new HashMap<>();

	private final Set<String> packageNames = new HashSet<>();

	private final Set<String> excludedClassNames = new HashSet<>();

	/**
	 * Create a fake Java runtime using the class names loaded from the given resource.
	 *
	 * @param resource Resource with class names
	 */
	private JavaRuntimeMock(String resource) {

		// open test resource for reading
		try (InputStream is = TestUtils.getResourceAsStream(resource)) {
			DataInputStream dis = new DataInputStream(new GZIPInputStream(is));

			// read compressed class definitions
			int numClassDefs = dis.readInt();
			for (int c = 0; c < numClassDefs; c++) {
				ClassDef classDef = ClassDefUtils.read(dis);
				String className = classDef.getClassName();
				classDefs.put(className, classDef);
				String packageName = classDef.getPackageName();
				packageNames.add(packageName);
			}

			// read excluded class names
			int numExcludedClassNames = dis.readInt();
			for (int i = 0; i < numExcludedClassNames; i++) {
				String className = dis.readUTF();
				excludedClassNames.add(className);
				String packageName = getPackageName(className);
				packageNames.add(packageName);
			}

		} catch (IOException e) {
			throw new TestDataException(e);
		}
	}

	@Override
	public String getName() {
		return "Java(TM) SE Runtime Environment";
	}

	@Override
	public String getJavaVersion() {
		return "1.8.0_144";
	}

	@Override
	public String getJavaVendor() {
		return "Oracle Corporation";
	}

	@Override
	public String getJavaHome() {
		return "/opt/java/jdk-1.8.0_144";
	}

	@Override
	public JarFile findJarFile(Predicate<JarFile> predicate) {
		return null;
	}

	@Override
	protected boolean findPackage(String packageName) {
		return packageNames.contains(packageName);
	}

	@Override
	protected ClassDef findClassDef(String className) {

		// check if class has been excluded during resource generation
		if (excludedClassNames.contains(className)) {
			String message = String.format("Class definition not found in test data: %s", className);
			throw new TestDataException(message);
		}

		return classDefs.get(className);
	}

	/**
	 * Small Java application to generate the test resource "classes-oracle-jdk-1.8.0_144.dat.gz",
	 * given the path to the rt.jar file of an Oracle JDK/JRE 1.8.0_144.
	 */
	public static void main(String[] args) throws IOException {

		// get path to rt.jar file
		String rtPath = args[0];
		File rtFile = new File(rtPath);
		if (!rtFile.isFile()) {
			System.err.println("File not found: " + rtFile.getAbsolutePath());
			System.exit(1);
		}

		// load all classes from rt.jar file into memory
		ClasspathLoader loader = LoaderBuilder.create().forClassLoader("Runtime").buildClasspathLoader();
		Classpath classpath = loader.load(Collections.singletonList(rtFile));
		JarFile jarFile = classpath.getJarFiles().get(0);
		List<ClassDef> classDefs = jarFile.getClassDefs();

		// list of packages which will be added to resource file
		List<String> includedPackageNames = Arrays.asList(
				"com.sun.net.httpserver", "com.sun.xml.internal.ws.addressing",
				"java.applet", "java.awt", "java.beans", "java.io", "java.lang", "java.math", "java.net", "java.nio", "java.rmi", "java.security", "java.sql", "java.text", "java.time", "java.util",
				"javax.activation", "javax.annotation", "javax.imageio", "javax.jws", "javax.management", "javax.naming", "javax.net", "javax.script", "javax.security.auth", "javax.sql", "javax.transaction", "javax.xml",
				"org.w3c.dom", "org.xml.sax",
				"sun.misc"
		);

		// split into included classes (class defs) and excluded classes (names)
		List<ClassDef> includedClassDefs = new ArrayList<>();
		List<String> excludedClassNames = new ArrayList<>();
		for (ClassDef classDef : classDefs) {
			String className = classDef.getClassName();
			boolean include = includedPackageNames.stream().anyMatch(p -> className.startsWith(p + "."));
			if (include) {
				includedClassDefs.add(classDef);
			} else {
				excludedClassNames.add(className);
			}
		}

		// open compressed output stream to test resource file
		String resourcePath = "./src/test/resources/classes-oracle-jdk-1.8.0_144.dat.gz";
		File resourceFile = new File(resourcePath);
		try (FileOutputStream fos = new FileOutputStream(resourceFile)) {
			try (GZIPOutputStream zos = new GZIPOutputStream(fos)) {
				try (DataOutputStream dos = new DataOutputStream(zos)) {
					// write list of included class definitions
					dos.writeInt(includedClassDefs.size());
					for (ClassDef classDef : includedClassDefs) {
						ClassDefUtils.write(classDef, dos);
					}
					// write list of excluded class names
					dos.writeInt(excludedClassNames.size());
					for (String className : excludedClassNames) {
						dos.writeUTF(className);
					}
				}
			}
		}

		System.out.println("Runtime file       : " + rtFile.getAbsolutePath());
		System.out.println("Total classes found: " + classDefs.size());
		System.out.println("Included classes   : " + includedClassDefs.size());
		System.out.println("Excluded classes   : " + excludedClassNames.size());
		System.out.println("Resource file      : " + resourceFile.getAbsolutePath());
		System.out.println("Final file size    : " + FileUtils.formatFileSize(resourceFile.length()));
	}

}
