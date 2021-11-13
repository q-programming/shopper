package pl.qprogramming.shopper.watch.util;

import java.util.Collection;

public class Utils {
    private Utils() {

    }


    /**
     * Checks if collection is null or empty
     *
     * @param coll collection to be tested
     * @return true if collection is null or empty
     */
    public static boolean isEmpty(Collection<?> coll) {
        return (coll == null || coll.isEmpty());
    }

    public static boolean isEmpty(String string) {
        return (string == null || string.isEmpty());
    }
}
