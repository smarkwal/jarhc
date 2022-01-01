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
