/*
 * Copyright 2021 Stephan Markwalder
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

package org.jarhc.test.release.utils;

public class CommandBuilder {

	public static String[] createJarHcCommand(String... arguments) {
		String[] command = new String[2 + arguments.length];
		command[0] = "-jar";
		command[1] = "jarhc.jar";
		System.arraycopy(arguments, 0, command, 2, arguments.length);
		return createJavaCommand(command);
	}

	public static String[] createJavaCommand(String... arguments) {
		String[] command = new String[1 + arguments.length];
		command[0] = "java";
		System.arraycopy(arguments, 0, command, 1, arguments.length);
		return command;
	}

}
