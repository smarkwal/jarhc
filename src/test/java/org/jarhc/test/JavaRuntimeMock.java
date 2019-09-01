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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jarhc.TestUtils;
import org.jarhc.env.JavaRuntime;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.utils.JavaUtils;

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

	/**
	 * Create a fake Java runtime using the class names loaded from the given resource.
	 *
	 * @param resource Resource with class names
	 */
	private JavaRuntimeMock(String resource) {

		// open test resource for reading
		try (InputStream is = TestUtils.getResourceAsStream(resource)) {
			if (is == null) throw new RuntimeException("Resource not found: " + resource);

			// read compressed class definitions
			DataInputStream dis = new DataInputStream(new GZIPInputStream(is));
			int numClassDefs = dis.readInt();
			for (int c = 0; c < numClassDefs; c++) {
				ClassDef classDef = ClassDefUtils.read(dis);
				String className = classDef.getClassName();
				classDefs.put(className, classDef);
				String packageName = JavaUtils.getPackageName(className);
				packageNames.add(packageName);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
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
	protected boolean findPackage(String packageName) {
		return packageNames.contains(packageName);
	}

	@Override
	protected Optional<ClassDef> findClassDef(String className) {
		ClassDef classDef = classDefs.get(className);
		return Optional.ofNullable(classDef);
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

		// open compressed output stream to test resource file
		String resourcePath = "./src/test/resources/classes-oracle-jdk-1.8.0_144.dat.gz";
		try (FileOutputStream fos = new FileOutputStream(new File(resourcePath))) {
			try (GZIPOutputStream zos = new GZIPOutputStream(fos)) {
				try (DataOutputStream dos = new DataOutputStream(zos)) {

					// write all class definitions
					dos.writeInt(classDefs.size());
					for (ClassDef classDef : classDefs) {
						ClassDefUtils.write(classDef, dos);
					}

				}
			}
		}

	}

}
