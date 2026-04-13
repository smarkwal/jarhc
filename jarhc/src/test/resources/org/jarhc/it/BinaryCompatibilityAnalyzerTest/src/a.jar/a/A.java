package a;

import jdk.internal.util.ArraysSupport;
import sun.text.IntHashtable;

public class A {

	public static void main(String[] args) {
		ArraysSupport.mismatch(new int[0], new int[0], 0);
		IntHashtable hashtable = new IntHashtable();
	}

}