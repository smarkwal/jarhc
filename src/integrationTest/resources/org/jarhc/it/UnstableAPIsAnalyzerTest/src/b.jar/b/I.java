package b;

@Deprecated
public interface I {

	@Deprecated
	int interfaceField = 0;

	@Deprecated
	void interfaceMethod();

	@Deprecated
	default void defaultInterfaceMethod() {
	}

}