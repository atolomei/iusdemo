
package io.demo.util;

/**
 * 
 * @author atolomei@novamens.com (Alejandro Tolomei)
 */
public class Constant {

    static public final String SEPARATOR = "---------------------------------";
    static final public String NOT_THROWN = "---- not thrown ----";

    public static final double KB = 1024.0;
    public static final double MB = 1024.0 * KB;
    public static final double GB = 1024.0 * MB;

    public static final int iKB = 1024;
    public static final int iMB = 1024 * iKB;
    public static final int iGB = 1024 * iMB;

    public static final int BYTES_IN_INT = 4;
    public static final int BYTES_IN_LONG = 16;

    static final public long kilobyte = 1024;
    static final public long megabyte = (kilobyte * 1024);
    static final public long gigabyte = (megabyte * 1024);
    static final public long terabyte = (gigabyte * 1024);
    static final public long petabyte = (terabyte * 1024);
    static final public long exabyte = (petabyte * 1024);
    static final public long zettabyte = (exabyte * 1024);
    static final public long yottabyte = (zettabyte * 1024);

    static final public double d_kilobyte = 1024.0;
    static final public double d_megabyte = (d_kilobyte * 1024);
    static final public double d_gigabyte = (d_megabyte * 1024);
    static final public double d_terabyte = (d_gigabyte * 1024);
    static final public double d_petabyte = (d_terabyte * 1024);
    static final public double d_exabyte = (d_petabyte * 1024);
    static final public double d_zettabyte = (d_exabyte * 1024);
    static final public double d_yottabyte = (d_zettabyte * 1024);

    static final public String valid_endpoint_regex = "^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$";

    static final public int DEFAULT_PAGE_SIZE = 60;
    static final public int DEFAULT_EXPIRY_TIME = 7 * 24 * 3600;

    static final public int BUFFER_SIZE = 8192;
    static final public int TRAFFIC_TOKENS_DEFAULT = 10;
    static final public long DEFAULT_SLEEP_TIME = 1 * 60 * 1000; // 1 minute
    

}
