package a;

import b.B;

public class A {

	public A() {

		B b = new B();
		b.publicField = 1;
		b.nonStaticField = 2;
		B.staticField = 3;
		b.nonFinalField = 4;
		b.intField = 5;
		b.existingField = 6;

	}

}