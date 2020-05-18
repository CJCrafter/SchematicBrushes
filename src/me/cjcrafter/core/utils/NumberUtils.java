package me.cjcrafter.core.utils;

import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class NumberUtils {

    // Generally used for enchantments in lore
    private static final TreeMap<Integer, String> numerals;

    // Used to display the amount of time passed
    private static final TreeMap<Integer, String> time;

    static {
        numerals = new TreeMap<>();
        numerals.put(1000, "M");
        numerals.put(900, "CM");
        numerals.put(500, "D");
        numerals.put(400, "CD");
        numerals.put(100, "C");
        numerals.put(90, "XC");
        numerals.put(50, "L");
        numerals.put(40, "XL");
        numerals.put(10, "X");
        numerals.put(9, "IX");
        numerals.put(5, "V");
        numerals.put(4, "IV");
        numerals.put(1, "I");

        // It's important to know that each unit
        // is number of seconds in it.
        time = new TreeMap<>();
        time.put(31536000, "y");
        time.put(86400, "d");
        time.put(3600, "h");
        time.put(60, "m");
        time.put(1, "s");
    }

    /**
     * Don't let anyone instantiate this class
     */
    private NumberUtils() {
    }

    /**
     * Threadsafe method to generate
     * a random integer [min, max]
     *
     * @param min minimum size of the number
     * @param max maximum size of the number
     * @return random int between min and max
     */
    public static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Threadsafe method to generate
     * a random double [min, max)
     *
     * @param min minimum size of the number
     * @param max maximum size of the number
     * @return random double between min and max
     */
    public static double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Checks if a chance (Which should be a
     * number [0, 1]) was successful
     *
     * @param chance The percentage chance to be successful
     * @return If the chance was successful or not
     */
    public static boolean chance(double chance) {
        return Math.random() < chance;
    }


    /**
     * Recursive function that translates an
     * <code>int</code> number to a <code>String
     * </code> roman numeral.
     *
     * @param from Integer to translate
     * @return Roman numeral translation
     */
    public static String toRomanNumeral(int from) {
        int numeral = numerals.floorKey(from);
        if (from == numeral) {
            return numerals.get(from);
        }
        return numerals.get(numeral) + toRomanNumeral(from - numeral);
    }

    public static String toTime(int seconds) {
        int unit = time.floorKey(seconds);
        int amount = seconds / unit;
        if (seconds % unit == 0) {
            return amount + time.get(unit);
        }
        return amount + time.get(unit) + " " + toTime(seconds - amount * unit);
    }

    public static double getAsRounded(double d) {
        return ((int) (d * 100)) / 100.0;
    }
}
