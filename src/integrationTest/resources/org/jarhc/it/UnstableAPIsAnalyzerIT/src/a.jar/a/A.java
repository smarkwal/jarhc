package a;

import b.*;

@Z(annotationField = 1)
class A extends S implements I {

	B b;
	E e;

	A() {
		super();

		b = new B();
		B.staticField = 1;
		e = E.E1;
	}

	@Override
	public void superMethod() {
		super.superMethod();
		superField = 1;
	}

	@Override
	public void interfaceMethod() {
		defaultInterfaceMethod();
	}

}