
package io.demo.util;

/**
 * 
 * <p>
 * Checks used for preconditions in methods
 * </p>
 * <p>
 * Example:
 * </p>
 * 
 * <pre> {@code 
 public void putObject(String bucketName, String objectName, InputStream is, String fileName, String contentType) {

		Check.requireNonNullStringArgument(bucketName, "bucketName can not be null or empty");
		Check.requireNonNullStringArgument(objectName, "objectName can not be null or empty | b:"+ bucketName);
		Check.requireNonNullStringArgument(fileName, "file is null | b: " + bucketName + " o:" + objectName);
		Check.requireNonNullArgument(is, "InpuStream can not null -> b:" + bucketName+ " | o:"+objectName);
		...
}
}</pre>
 * 
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
public class Check {

    public static <T> T requireNonNullArgument(T obj, String message) {
        if (obj == null)
            throw new IllegalArgumentException(message);
        return obj;
    }

    public static String requireNonNullStringArgument(String obj, String message) {

        if (obj == null)
            throw new IllegalArgumentException(message);

        if ("".equals((String) obj))
            throw new IllegalArgumentException(message);

        return obj;
    }

    public static void requireTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static void checkTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }

    public static <T> T requireNonNull(T obj, String msg) {
        if (obj == null)
            throw new NullPointerException(msg);
        return obj;
    }

    
}
