package org.jarhc.test.release.utils;

import java.io.IOException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class JavaContainer<SELF extends JavaContainer<SELF>> extends GenericContainer<SELF> {

	public JavaContainer(String javaImageName) {
		super(DockerImageName.parse(javaImageName));
	}

	public ExecResult execJava(String... arguments) {
		String[] command = CommandBuilder.createJavaCommand(arguments);
		try {
			return execInContainer(command);
		} catch (IOException e) {
			throw new AssertionError("Unexpected I/O error.", e);
		} catch (InterruptedException e) {
			throw new AssertionError("Unexpected interruption.", e);
		}
	}

}
