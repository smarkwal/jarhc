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

import java.util.ArrayList;
import java.util.List;
import org.jarhc.model.ClassDef;
import org.jarhc.test.log.LoggerBuilder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Loads ~ 100 classes from package java.lang using a fresh {@link DefaultJavaRuntime} instance.
 */
@BenchmarkMode(Mode.SingleShotTime)
@Fork(warmups = 1, value = 10)
@State(Scope.Benchmark)
public class DefaultJavaRuntimeBenchmark {

	/**
	 * List of class names
	 */
	private static final List<String> classNames = new ArrayList<>();

	/**
	 * Java runtime used in benchmark (re-created for every benchmark run)
	 */
	private JavaRuntime javaRuntime;

	@Setup
	public void setUp() {
		javaRuntime = new DefaultJavaRuntime(LoggerBuilder.noop());
	}

	@Benchmark
	public void getClassDef() {
		for (String className : classNames) {
			ClassDef classDef = javaRuntime.getClassDef(className);
			if (classDef == null) {
				throw new AssertionError("Class not found: " + className);
			}
		}
	}

	static {
		classNames.add("java.lang.AbstractMethodError");
		classNames.add("java.lang.Appendable");
		classNames.add("java.lang.ArithmeticException");
		classNames.add("java.lang.ArrayIndexOutOfBoundsException");
		classNames.add("java.lang.ArrayStoreException");
		classNames.add("java.lang.AssertionError");
		classNames.add("java.lang.AutoCloseable");
		classNames.add("java.lang.Boolean");
		classNames.add("java.lang.BootstrapMethodError");
		classNames.add("java.lang.Byte");
		classNames.add("java.lang.CharSequence");
		classNames.add("java.lang.Character");
		classNames.add("java.lang.Character$Subset");
		classNames.add("java.lang.Character$UnicodeBlock");
		classNames.add("java.lang.Character$UnicodeScript");
		classNames.add("java.lang.Class");
		classNames.add("java.lang.ClassCastException");
		classNames.add("java.lang.ClassCircularityError");
		classNames.add("java.lang.ClassFormatError");
		classNames.add("java.lang.ClassLoader");
		classNames.add("java.lang.ClassNotFoundException");
		classNames.add("java.lang.ClassValue");
		classNames.add("java.lang.CloneNotSupportedException");
		classNames.add("java.lang.Cloneable");
		classNames.add("java.lang.Comparable");
		classNames.add("java.lang.Compiler");
		classNames.add("java.lang.Deprecated");
		classNames.add("java.lang.Double");
		classNames.add("java.lang.Enum");
		classNames.add("java.lang.EnumConstantNotPresentException");
		classNames.add("java.lang.Error");
		classNames.add("java.lang.Exception");
		classNames.add("java.lang.ExceptionInInitializerError");
		classNames.add("java.lang.Float");
		classNames.add("java.lang.IllegalAccessError");
		classNames.add("java.lang.IllegalAccessException");
		classNames.add("java.lang.IllegalArgumentException");
		classNames.add("java.lang.IllegalMonitorStateException");
		classNames.add("java.lang.IllegalStateException");
		classNames.add("java.lang.IllegalThreadStateException");
		classNames.add("java.lang.IncompatibleClassChangeError");
		classNames.add("java.lang.IndexOutOfBoundsException");
		classNames.add("java.lang.InheritableThreadLocal");
		classNames.add("java.lang.InstantiationError");
		classNames.add("java.lang.InstantiationException");
		classNames.add("java.lang.Integer");
		classNames.add("java.lang.InternalError");
		classNames.add("java.lang.InterruptedException");
		classNames.add("java.lang.Iterable");
		classNames.add("java.lang.LinkageError");
		classNames.add("java.lang.Long");
		classNames.add("java.lang.Math");
		classNames.add("java.lang.NegativeArraySizeException");
		classNames.add("java.lang.NoClassDefFoundError");
		classNames.add("java.lang.NoSuchFieldError");
		classNames.add("java.lang.NoSuchFieldException");
		classNames.add("java.lang.NoSuchMethodError");
		classNames.add("java.lang.NoSuchMethodException");
		classNames.add("java.lang.NullPointerException");
		classNames.add("java.lang.Number");
		classNames.add("java.lang.NumberFormatException");
		classNames.add("java.lang.Object");
		classNames.add("java.lang.OutOfMemoryError");
		classNames.add("java.lang.Override");
		classNames.add("java.lang.Package");
		classNames.add("java.lang.Process");
		classNames.add("java.lang.ProcessBuilder");
		classNames.add("java.lang.ProcessBuilder$Redirect");
		classNames.add("java.lang.ProcessBuilder$Redirect$Type");
		classNames.add("java.lang.Readable");
		classNames.add("java.lang.ReflectiveOperationException");
		classNames.add("java.lang.Runnable");
		classNames.add("java.lang.Runtime");
		classNames.add("java.lang.RuntimeException");
		classNames.add("java.lang.RuntimePermission");
		classNames.add("java.lang.SafeVarargs");
		classNames.add("java.lang.SecurityException");
		classNames.add("java.lang.SecurityManager");
		classNames.add("java.lang.Short");
		classNames.add("java.lang.StackOverflowError");
		classNames.add("java.lang.StackTraceElement");
		classNames.add("java.lang.StrictMath");
		classNames.add("java.lang.String");
		classNames.add("java.lang.StringBuffer");
		classNames.add("java.lang.StringBuilder");
		classNames.add("java.lang.StringIndexOutOfBoundsException");
		classNames.add("java.lang.SuppressWarnings");
		classNames.add("java.lang.System");
		classNames.add("java.lang.Thread");
		classNames.add("java.lang.Thread$State");
		classNames.add("java.lang.Thread$UncaughtExceptionHandler");
		classNames.add("java.lang.ThreadDeath");
		classNames.add("java.lang.ThreadGroup");
		classNames.add("java.lang.ThreadLocal");
		classNames.add("java.lang.Throwable");
		classNames.add("java.lang.TypeNotPresentException");
		classNames.add("java.lang.UnknownError");
		classNames.add("java.lang.UnsatisfiedLinkError");
		classNames.add("java.lang.UnsupportedClassVersionError");
		classNames.add("java.lang.UnsupportedOperationException");
		classNames.add("java.lang.VerifyError");
		classNames.add("java.lang.VirtualMachineError");
		classNames.add("java.lang.Void");
	}

}
