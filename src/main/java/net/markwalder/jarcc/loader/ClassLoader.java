package net.markwalder.jarcc.loader;

import net.markwalder.jarcc.model.ClassDef;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassLoader {

	public ClassDef load(String filePath) throws IOException {
		try (InputStream stream = new FileInputStream(filePath)) {
			return load(stream);
		}
	}

	public ClassDef load(InputStream stream) throws IOException {

		// parse class file with ASM
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(stream);
		classReader.accept(classNode, 0);

		// create class definition
		return new ClassDef(classNode.name, classNode.version);

	}

}
