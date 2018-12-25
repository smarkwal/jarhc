package a;

import b.B;
import b.E;

public class A {

	public A() {

		B b = new B();
		b.publicField = 1;
		b.nonStaticField = 2;
		B.staticField = 3;
		b.nonFinalField = 4;
		b.intField = 5;
		b.existingField = 6;

		b.superField = b.interfaceField;
		b.nonStaticSuperField = 7;
		B.staticSuperField = 8;

		Object x = E.E3;
		boolean y = Boolean.TRUE;
		String z = java.io.File.separator;

		int len = b.arrayField.length;

	}

}