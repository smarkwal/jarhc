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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;

public class JavaRuntimeMockBenchmark {

	@Benchmark
	@BenchmarkMode(Mode.SingleShotTime)
	@Fork(warmups = 1, value = 10, jvmArgs = {"-Xms1G", "-Xmx2G"})
	// @Warmup(iterations = 2, time = 2)
	// @Measurement(iterations = 2, time = 2)
	public void createOracleRuntime() {
		JavaRuntimeMock.createOracleRuntime();
	}

}
