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

package org.jarhc.analyzer;

import org.jarhc.env.JavaRuntime;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.model.Classpath;
import org.jarhc.test.JavaRuntimeMock;
import org.jarhc.test.ResolverMock;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.SingleShotTime)
@Fork(warmups = 1, value = 10)
@State(Scope.Benchmark)
// @Warmup(iterations = 5)
// @Measurement(iterations = 10)
public class AnalyzerBenchmarks {

	private Classpath classpath;
	private JavaRuntime javaRuntime;

	@Setup
	public void setUp() {

		List<String> fileNames = new ArrayList<>();
		fileNames.add("spring-aop-5.1.3.RELEASE.jar");
		fileNames.add("spring-beans-5.1.3.RELEASE.jar");
		fileNames.add("spring-context-5.1.3.RELEASE.jar");
		fileNames.add("spring-context-support-5.1.3.RELEASE.jar");
		fileNames.add("spring-core-5.1.3.RELEASE.jar");
		fileNames.add("spring-expression-5.1.3.RELEASE.jar");
		fileNames.add("spring-jdbc-5.1.3.RELEASE.jar");
		fileNames.add("spring-jms-5.1.3.RELEASE.jar");
		fileNames.add("spring-messaging-5.1.3.RELEASE.jar");
		fileNames.add("spring-orm-5.1.3.RELEASE.jar");
		fileNames.add("spring-tx-5.1.3.RELEASE.jar");
		fileNames.add("spring-web-5.1.3.RELEASE.jar");

		List<File> files = fileNames.stream().map(f -> new File("./src/test/resources/Spring5IT", f)).collect(Collectors.toList());
		ClasspathLoader loader = new ClasspathLoader();
		classpath = loader.load(files);
		javaRuntime = JavaRuntimeMock.getOracleRuntime();
	}

	@Benchmark
	public void test_BlacklistAnalyzer() {
		Analyzer analyzer = new BlacklistAnalyzer();
		analyzer.analyze(classpath);
	}

	@Benchmark
	public void test_ClassVersionsAnalyzer() {
		Analyzer analyzer = new ClassVersionsAnalyzer();
		analyzer.analyze(classpath);
	}

	@Benchmark
	public void test_DuplicateClassesAnalyzer() {
		Analyzer analyzer = new DuplicateClassesAnalyzer();
		analyzer.analyze(classpath);
	}

	@Benchmark
	public void test_FieldRefAnalyzer() {
		Analyzer analyzer = new FieldRefAnalyzer(javaRuntime, false);
		analyzer.analyze(classpath);
	}

	@Benchmark
	public void test_JarDependenciesAnalyzer() {
		Analyzer analyzer = new JarDependenciesAnalyzer();
		analyzer.analyze(classpath);
	}

	@Benchmark
	public void test_JarFilesAnalyzer() {
		Analyzer analyzer = new JarFilesAnalyzer(ResolverMock.createNullResolver());
		analyzer.analyze(classpath);
	}

	@Benchmark
	public void test_MissingClassesAnalyzer() {
		Analyzer analyzer = new MissingClassesAnalyzer(javaRuntime);
		analyzer.analyze(classpath);
	}

	@Benchmark
	public void test_PackagesAnalyzer() {
		Analyzer analyzer = new PackagesAnalyzer();
		analyzer.analyze(classpath);
	}

	@Benchmark
	public void test_ShadowedClassesAnalyzer() {
		Analyzer analyzer = new ShadowedClassesAnalyzer(javaRuntime);
		analyzer.analyze(classpath);
	}

	@Benchmark
	public void test_SplitPackagesAnalyzer() {
		Analyzer analyzer = new SplitPackagesAnalyzer();
		analyzer.analyze(classpath);
	}

}
