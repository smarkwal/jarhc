package org.jarhc.test.release.utils;

import java.io.IOException;

public class JarHcContainer extends JavaContainer<JarHcContainer> {

	public JarHcContainer(String javaImageName) {
		super(javaImageName);
	}

	public ExecResult execJarHc(String... arguments) {
		String[] command = CommandBuilder.createJarHcCommand(arguments);
		try {
			return execInContainer(command);
		} catch (IOException e) {
			throw new AssertionError("Unexpected I/O error.", e);
		} catch (InterruptedException e) {
			throw new AssertionError("Unexpected interruption.", e);
		}
	}

}
