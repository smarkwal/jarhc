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

package org.jarhc.report.writer;

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface for report writers.
 */
public interface ReportWriter extends Closeable {

	void print(String text);

	default void print(String format, Object... args) {
		String text = String.format(format, args);
		print(text);
	}

	default void println() {
		print(System.lineSeparator());
	}

	default void println(String text) {
		print(text);
		println();
	}

	default void println(String format, Object... args) {
		String text = String.format(format, args);
		println(text);
	}

	@Override
	default void close() throws IOException {
		// implementations may override this method to close a file stream
	}

}
