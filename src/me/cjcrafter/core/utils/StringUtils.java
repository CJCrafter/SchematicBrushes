package me.cjcrafter.core.utils;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This utility class contains static utility methods
 * wrapping around the idea of a <code>String</code>
 */
public class StringUtils {


    /**
     * Don't let anyone instantiate this class
     */
    private StringUtils() {
    }

    /**
     * Counts the number of a given character in a String
     *
     * @param c The character to check for
     * @param string The string to check in
     * @return How many c's are found in the string
     */
    public static int countChars(char c, String string) {
        return (int) string.chars().filter(character -> character == c).count();
    }

    /**
     * Colors a given string
     *
     * @param string String to color
     * @return Colored String
     */
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Splits after each word, removing spaces if present.
     * Example:
     * <blockquote><pre>{@code
     *     splitAfterWord("Hello"); // ["Hello"]
     *     splitAfterWord("Hello World"); // ["Hello", "World"]
     *     splitAfterWord("^<38 7&*9()"); // ["^<38", "7&*9()"]
     * }</pre></blockquote>
     * A word being defined as not whitespace, meaning that numbers
     * and special characters do count as words in this context
     *
     * @param from the string to split
     * @return the given string as an array
     */
    public static String[] splitAfterWord(String from) {
        return from.split("(?![\\S]+) |(?![\\S]+)");
    }

    public static String camelToKey(String from) {
        return String.join("_", from.split("(?=[A-Z])")).toLowerCase();
    }

    public static String keyToRead(String key) {
        String[] split = key.split("_");

        StringBuilder builder = new StringBuilder();
        for (String s: split) {
            builder.append(s.substring(0, 1).toUpperCase());
            builder.append(s.substring(1));
            builder.append(" ");
        }
        return builder.substring(0, builder.length() - 1);
    }

    /**
     * Splits with whitespaces (" ") and minus ("-") while allowing negative values also
     * Example:
     * <pre>
     *    split("SOUND-1-5"); // [SOUND, 1, 1]
     *    split("Value--2-6"); // [Value, -2, 6]
     *    split("Something 22 -634"); // [Something, 22, -634]
     * </pre>
     *
     * @param from the string to split
     * @return the given string as an array
     */
    public static String[] split(String from) {
        return from.split("[~ ]+|(?<![~ -])-");
    }

    /**
     * Colors a given Array and returns it as a list.
     * Useful for TabCompletion stuff.
     *
     * Note: Players can NOT use colors in chat/commands
     *
     * @param strings Array to convert
     * @return The list version of the array
     */
    public static List<String> getList(String...strings) {
        return Arrays.stream(strings).map(StringUtils::color).collect(Collectors.toList());
    }
}