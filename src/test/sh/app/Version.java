public class Version {
	public static void main(String[] args) {
		String version = System.getProperty("java.version");
		String vendor = System.getProperty("java.vendor");
		String home = System.getProperty("java.home");
		System.out.println(version + " | " + vendor + " | " + home);
	}
}