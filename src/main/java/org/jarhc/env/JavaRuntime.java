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

package org.jarhc.env;

import org.jarhc.model.ClassDef;

import java.util.Optional;

/**
 * An implementation of this interface represents a Java runtime.
 * It is used to check if a given class is part of the Java installation,
 * which usually includes the Java Class Library and extensions,
 * and to get information (a class definition) about such a class.
 * <p>
 * Note: Implementations must ignore classes which are part of the
 * JarHC tool.
 *
 * @see DefaultJavaRuntime
 */
public interface JavaRuntime {

	/**
	 * Get the name of the Java runtime.
	 *
	 * @return Java runtime name
	 */
	String getName();

	/**
	 * The Java version of the Java runtime.
	 *
	 * @return Java version
	 */
	String getJavaVersion();

	/**
	 * The vendor of the Java runtime.
	 *
	 * @return Vendor name
	 */
	String getJavaVendor();

	/**
	 * The path to the installation of the Java runtime.
	 *
	 * @return Java runtime installation path
	 */
	String getJavaHome();

	/**
	 * Get the name of the class loader used to load the given class,
	 * or <code>null</code> if the class cannot be found.
	 *
	 * @param className Class name
	 * @return Class loader name, or <code>null</code> if the class cannot be found.
	 */
	Optional<String> getClassLoaderName(String className);

	/**
	 * Get the class definition for the given class,
	 * or <code>null</code> of the class cannot be found.
	 *
	 * @param className Class name
	 * @return Class definition, or <code>null</code> if the class cannot be found.
	 */
	Optional<ClassDef> getClassDef(String className);

}
