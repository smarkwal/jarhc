package b;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.DoNotCall;

@Deprecated
public class S {

	@Deprecated
	public int superField;

	@Deprecated
	public S() {
	}

	@Deprecated
	@Beta
	@VisibleForTesting
	@DoNotCall
	public void superMethod() {
	}

}