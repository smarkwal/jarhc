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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command {

	private final List<String> javaArguments = new ArrayList<>();
	private final List<String> jarHcArguments = new ArrayList<>();

	public static Command java(String... arguments) {
		Command command = new Command();
		command.javaArguments.addAll(Arrays.asList(arguments));
		return command;
	}

	public static Command jarHc(String... arguments) {
		Command command = new Command();
		command.jarHcArguments.add("-jar");
		command.jarHcArguments.add("jarhc-with-deps.jar");
		command.jarHcArguments.addAll(Arrays.asList(arguments));
		return command;
	}

	private Command() {
	}

	@SuppressWarnings("UnusedReturnValue")
	public void addJavaArguments(String... arguments) {
		javaArguments.addAll(Arrays.asList(arguments));
	}

	@SuppressWarnings("UnusedReturnValue")
	public void addJarHcArguments(String... arguments) {
		jarHcArguments.addAll(Arrays.asList(arguments));
	}

	public boolean isJarHcCommand() {
		return !jarHcArguments.isEmpty();
	}

	public String[] build() {
		List<String> command = new ArrayList<>();
		command.add("java");
		command.addAll(javaArguments);
		command.addAll(jarHcArguments);
		return command.toArray(new String[0]);
	}

	@Override
	public String toString() {
		return String.join(" ", build());
	}

}
