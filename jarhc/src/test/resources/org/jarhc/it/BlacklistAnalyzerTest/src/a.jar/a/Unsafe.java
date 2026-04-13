package a;

public class Unsafe {
	public void m() {
		sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe();
		int addressSize = unsafe.addressSize();
	}
}