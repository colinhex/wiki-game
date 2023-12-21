package utils;

public class Caches {
    private static final int KILO = (int) Math.pow( 10, 3 );
    private static final int MEGA = (int) Math.pow( 10, 6 );
    private static final int GIGA = (int) Math.pow( 10, 9 );

    public static int kiloBytesInteger(int size) {
        return size * (KILO/Integer.BYTES);
    }

    public static int megaBytesInteger(int size) {
        return size * (MEGA/Integer.BYTES);
    }

    public static int gigaBytesInteger(int size) {
        return size * (GIGA/Integer.BYTES);
    }

    public static int kiloBytesDouble(int size) {
        return size * (KILO/Double.BYTES);
    }

    public static int megaBytesDouble(int size) {
        return size * (MEGA/Double.BYTES);
    }

    public static int gigaBytesDouble(int size) {
        return size * (GIGA/Double.BYTES);
    }

    public static int kiloBytesLong(int size) {
        return size * (KILO/Long.BYTES);
    }

    public static int megaBytesLong(int size) {
        return size * (MEGA/Long.BYTES);
    }

    public static int gigaBytesLong(int size) {
        return size * (GIGA/Long.BYTES);
    }
}
