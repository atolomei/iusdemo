
package io.demo.util;

import java.util.Random;

/**
 * <p>Simple random String generator</p>
 * 
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
public class RandomIDGenerator {

	private static Random random = new Random();

	public String randomString(final int size) {
		int leftLimit = 97; // letter 'a'
	    int rightLimit = 122; // letter 'z'
	    int targetStringLength = size;
	    String generatedString = random.ints(leftLimit, rightLimit + 1)
	      .limit(targetStringLength)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();
	    return generatedString;
	}
	
}
