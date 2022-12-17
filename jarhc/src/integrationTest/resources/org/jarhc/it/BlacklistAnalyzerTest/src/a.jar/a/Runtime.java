package a;

import java.io.File;
import java.io.IOException;

public class Runtime {
	public void m() throws IOException {
		java.lang.Runtime.getRuntime().exit(0);
		java.lang.Runtime.getRuntime().halt(0);
		java.lang.Runtime.getRuntime().load("test.dll");
		java.lang.Runtime.getRuntime().loadLibrary("test.dll");
		java.lang.Runtime.getRuntime().exec("ls");
		java.lang.Runtime.getRuntime().exec("ls", new String[]{});
		java.lang.Runtime.getRuntime().exec("ls", new String[]{}, new File("."));
		java.lang.Runtime.getRuntime().exec(new String[]{"ls"});
		java.lang.Runtime.getRuntime().exec(new String[]{"ls"}, new String[]{});
		java.lang.Runtime.getRuntime().exec(new String[]{"ls"}, new String[]{}, new File("."));
	}
}