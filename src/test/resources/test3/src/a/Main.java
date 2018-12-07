package a;

import java.util.ArrayList;
import java.util.List;

@ClassAnnotation("class")
public class Main extends Base implements Interface {

	public static void main(String[] args) {
		Main main = new Main();
	}

	@FieldAnnotation("field")
	public String name;

	public int number;
	public byte[] data;

	@MethodAnnotation("method")
	public void test() throws CustomException {
		Number value = new Long(1234);
		try {
			System.out.println(value);
		} catch (RuntimeException e) {
			System.out.println("exception");
		} finally {
			System.out.println("finally");
		}
	}

	public static List<Boolean> createList() {
		return new ArrayList<>();
	}

	public class InnerMain {

	}

	public static class StaticInnerMain {

	}

}