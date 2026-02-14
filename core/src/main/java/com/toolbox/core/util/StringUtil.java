package com.toolbox.core.util;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtil {

    private StringUtil() {
    }

    private static final String ELLIPSIS3 = "...";
    private static final String SPACE = " ";
    private static final String EMPTY = "";
    private static final int INDEX_NOT_FOUND = -1;
    private static final Pattern STRIP_ACCENTS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+"); //$NON-NLS-1$

    /**
     * Abbreviates a String using ellipses. This will convert "Now is the time for all good men" into "Now is the time for..."
     *
     * <p>
     * Specifically:
     * </p>
     * <ul>
     * <li>If the number of characters in {@code str} is less than or equal to {@code maxWidth}, return {@code str}.</li>
     * <li>Else abbreviate it to {@code (substring(str, 0, max - 3) + "...")}.</li>
     * <li>If {@code maxWidth} is less than {@code 4}, throw an {@link IllegalArgumentException}.</li>
     * <li>In no case will it return a String of length greater than {@code maxWidth}.</li>
     * </ul>
     *
     * <pre>
     * StringUtil.abbreviate(null, *)      = null
     * StringUtil.abbreviate("", 4)        = ""
     * StringUtil.abbreviate("abcdefg", 6) = "abc..."
     * StringUtil.abbreviate("abcdefg", 7) = "abcdefg"
     * StringUtil.abbreviate("abcdefg", 8) = "abcdefg"
     * StringUtil.abbreviate("abcdefg", 4) = "a..."
     * StringUtil.abbreviate("abcdefg", 3) = Throws {@link IllegalArgumentException}.
     * </pre>
     *
     * @param str      the String to check, may be null.
     * @param maxWidth maximum length of result String, must be at least 4.
     * @return abbreviated String, {@code null} if null String input.
     * @throws IllegalArgumentException if the width is too small.
     */
    public static String abbreviate(final String str, final int maxWidth) {
        return abbreviate(str, ELLIPSIS3, 0, maxWidth);
    }

    /**
     * Abbreviates a String using ellipses. This will convert "Now is the time for all good men" into "...is the time for...".
     *
     * <p>
     * Works like {@code abbreviate(String, int)}, but allows you to specify a "left edge" offset. Note that this left edge is not necessarily going to be the
     * leftmost character in the result, or the first character following the ellipses, but it will appear somewhere in the result.
     * </p>
     * <p>
     * In no case will it return a String of length greater than {@code maxWidth}.
     * </p>
     *
     * <pre>
     * StringUtil.abbreviate(null, *, *)                = null
     * StringUtil.abbreviate("", 0, 4)                  = ""
     * StringUtil.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
     * StringUtil.abbreviate("abcdefghijklmno", 0, 10)  = "abcdefg..."
     * StringUtil.abbreviate("abcdefghijklmno", 1, 10)  = "abcdefg..."
     * StringUtil.abbreviate("abcdefghijklmno", 4, 10)  = "abcdefg..."
     * StringUtil.abbreviate("abcdefghijklmno", 5, 10)  = "...fghi..."
     * StringUtil.abbreviate("abcdefghijklmno", 6, 10)  = "...ghij..."
     * StringUtil.abbreviate("abcdefghijklmno", 8, 10)  = "...ijklmno"
     * StringUtil.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
     * StringUtil.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
     * StringUtil.abbreviate("abcdefghij", 0, 3)        = Throws {@link IllegalArgumentException}.
     * StringUtil.abbreviate("abcdefghij", 5, 6)        = Throws {@link IllegalArgumentException}.
     * </pre>
     *
     * @param str      the String to check, may be null.
     * @param offset   left edge of source String.
     * @param maxWidth maximum length of result String, must be at least 4.
     * @return abbreviated String, {@code null} if null String input.
     * @throws IllegalArgumentException if the width is too small.
     */
    public static String abbreviate(final String str, final int offset, final int maxWidth) {
        return abbreviate(str, ELLIPSIS3, offset, maxWidth);
    }

    /**
     * Abbreviates a String using another given String as replacement marker. This will convert "Now is the time for all good men" into "Now is the time for..."
     * when "..." is the replacement marker.
     *
     * <p>
     * Specifically:
     * </p>
     * <ul>
     * <li>If the number of characters in {@code str} is less than or equal to {@code maxWidth}, return {@code str}.</li>
     * <li>Else abbreviate it to {@code (substring(str, 0, max - abbrevMarker.length) + abbrevMarker)}.</li>
     * <li>If {@code maxWidth} is less than {@code abbrevMarker.length + 1}, throw an {@link IllegalArgumentException}.</li>
     * <li>In no case will it return a String of length greater than {@code maxWidth}.</li>
     * </ul>
     *
     * <pre>
     * StringUtil.abbreviate(null, "...", *)      = null
     * StringUtil.abbreviate("abcdefg", null, *)  = "abcdefg"
     * StringUtil.abbreviate("", "...", 4)        = ""
     * StringUtil.abbreviate("abcdefg", ".", 5)   = "abcd."
     * StringUtil.abbreviate("abcdefg", ".", 7)   = "abcdefg"
     * StringUtil.abbreviate("abcdefg", ".", 8)   = "abcdefg"
     * StringUtil.abbreviate("abcdefg", "..", 4)  = "ab.."
     * StringUtil.abbreviate("abcdefg", "..", 3)  = "a.."
     * StringUtil.abbreviate("abcdefg", "..", 2)  = Throws {@link IllegalArgumentException}.
     * StringUtil.abbreviate("abcdefg", "...", 3) = Throws {@link IllegalArgumentException}.
     * </pre>
     *
     * @param str          the String to check, may be null.
     * @param abbrevMarker the String used as replacement marker.
     * @param maxWidth     maximum length of result String, must be at least {@code abbrevMarker.length + 1}.
     * @return abbreviated String, {@code null} if null String input.
     * @throws IllegalArgumentException if the width is too small.
     */
    public static String abbreviate(final String str, final String abbrevMarker, final int maxWidth) {
        return abbreviate(str, abbrevMarker, 0, maxWidth);
    }

    /**
     * Abbreviates a String using a given replacement marker. This will convert "Now is the time for all good men" into "...is the time for..." when "..." is
     * the replacement marker.
     * <p>
     * Works like {@code abbreviate(String, String, int)}, but allows you to specify a "left edge" offset. Note that this left edge is not necessarily going to
     * be the leftmost character in the result, or the first character following the replacement marker, but it will appear somewhere in the result.
     * </p>
     * <p>
     * In no case will it return a String of length greater than {@code maxWidth}.
     * </p>
     *
     * <pre>
     * StringUtil.abbreviate(null, null, *, *)                 = null
     * StringUtil.abbreviate("abcdefghijklmno", null, *, *)    = "abcdefghijklmno"
     * StringUtil.abbreviate("", "...", 0, 4)                  = ""
     * StringUtil.abbreviate("abcdefghijklmno", "---", -1, 10) = "abcdefg---"
     * StringUtil.abbreviate("abcdefghijklmno", ",", 0, 10)    = "abcdefghi,"
     * StringUtil.abbreviate("abcdefghijklmno", ",", 1, 10)    = "abcdefghi,"
     * StringUtil.abbreviate("abcdefghijklmno", ",", 2, 10)    = "abcdefghi,"
     * StringUtil.abbreviate("abcdefghijklmno", "::", 4, 10)   = "::efghij::"
     * StringUtil.abbreviate("abcdefghijklmno", "...", 6, 10)  = "...ghij..."
     * StringUtil.abbreviate("abcdefghijklmno", "…", 6, 10)    = "…ghij…"
     * StringUtil.abbreviate("abcdefghijklmno", "*", 9, 10)    = "*ghijklmno"
     * StringUtil.abbreviate("abcdefghijklmno", "'", 10, 10)   = "'ghijklmno"
     * StringUtil.abbreviate("abcdefghijklmno", "!", 12, 10)   = "!ghijklmno"
     * StringUtil.abbreviate("abcdefghij", "abra", 0, 4)       = Throws {@link IllegalArgumentException}.
     * StringUtil.abbreviate("abcdefghij", "...", 5, 6)        = Throws {@link IllegalArgumentException}.
     * </pre>
     *
     * @param str          the String to check, may be null.
     * @param abbrevMarker the String used as replacement marker, for example "...", or Unicode HORIZONTAL ELLIPSIS, U+2026 '…'.
     * @param offset       left edge of source String.
     * @param maxWidth     maximum length of result String, must be at least 4.
     * @return abbreviated String, {@code null} if null String input.
     * @throws IllegalArgumentException if the width is too small.
     */
    public static String abbreviate(final String str, String abbrevMarker, int offset, final int maxWidth) {
        if (isEmpty(str)) {
            return str;
        }
        if (abbrevMarker == null) {
            abbrevMarker = EMPTY;
        }
        final int abbrevMarkerLength = abbrevMarker.length();
        final int minAbbrevWidth = abbrevMarkerLength + 1;
        final int minAbbrevWidthOffset = abbrevMarkerLength + abbrevMarkerLength + 1;

        if (maxWidth < minAbbrevWidth) {
            throw new IllegalArgumentException(String.format("Minimum abbreviation width is %d", minAbbrevWidth));
        }
        final int strLen = str.length();
        if (strLen <= maxWidth) {
            return str;
        }
        if (offset > strLen) {
            offset = strLen;
        }
        if (strLen - offset < maxWidth - abbrevMarkerLength) {
            offset = strLen - (maxWidth - abbrevMarkerLength);
        }
        if (offset <= abbrevMarkerLength + 1) {
            return str.substring(0, maxWidth - abbrevMarkerLength) + abbrevMarker;
        }
        if (maxWidth < minAbbrevWidthOffset) {
            throw new IllegalArgumentException(String.format("Minimum abbreviation width with offset is %d", minAbbrevWidthOffset));
        }
        if (offset + maxWidth - abbrevMarkerLength < strLen) {
            return abbrevMarker + abbreviate(str.substring(offset), abbrevMarker, maxWidth - abbrevMarkerLength);
        }
        return abbrevMarker + str.substring(strLen - (maxWidth - abbrevMarkerLength));
    }

    /**
     * Computes the capacity required for a StringBuilder to hold {@code items} of {@code maxElementChars} characters plus the separators between them. The
     * separator is assumed to be 1 character.
     *
     * @param count           The number of items.
     * @param maxElementChars The maximum number of characters per item.
     * @return A StringBuilder with the appropriate capacity.
     */
    private static StringBuilder capacity(final int count, final byte maxElementChars) {
        return new StringBuilder(count * maxElementChars + count - 1);
    }

    /**
     * Capitalizes a String changing the first character to title case as per {@link Character#toTitleCase(int)}. No other characters are changed.
     *
     * <p>
     * </p>
     *
     * <pre>
     * StringUtil.capitalize(null)    = null
     * StringUtil.capitalize("")      = ""
     * StringUtil.capitalize("cat")   = "Cat"
     * StringUtil.capitalize("cAt")   = "CAt"
     * StringUtil.capitalize("'cat'") = "'cat'"
     * </pre>
     *
     * @param str the String to capitalize, may be null.
     * @return the capitalized String, {@code null} if null String input.
     * @see #uncapitalize(String)
     */
    public static String capitalize(final String str) {
        if (isEmpty(str)) {
            return str;
        }
        final int firstCodepoint = str.codePointAt(0);
        final int newCodePoint = Character.toTitleCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            // already capitalized
            return str;
        }
        final int[] newCodePoints = str.codePoints().toArray();
        newCodePoints[0] = newCodePoint; // copy the first code point
        return new String(newCodePoints, 0, newCodePoints.length);
    }

    /**
     * Compares two Strings lexicographically, as per {@link String#compareTo(String)}, returning :
     * <ul>
     * <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
     * <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
     * <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
     * </ul>
     *
     * <p>
     * This is a {@code null} safe version of :
     * </p>
     *
     * <pre>
     * str1.compareTo(str2)
     * </pre>
     *
     * <p>
     * {@code null} inputs are handled according to the {@code nullIsLess} parameter. Two {@code null} references are considered equal.
     * </p>
     *
     * <pre>{@code
     * StringUtil.compare(null, null, *)     = 0
     * StringUtil.compare(null , "a", true)  < 0
     * StringUtil.compare(null , "a", false) > 0
     * StringUtil.compare("a", null, true)   > 0
     * StringUtil.compare("a", null, false)  < 0
     * StringUtil.compare("abc", "abc", *)   = 0
     * StringUtil.compare("a", "b", *)       < 0
     * StringUtil.compare("b", "a", *)       > 0
     * StringUtil.compare("a", "B", *)       > 0
     * StringUtil.compare("ab", "abc", *)    < 0
     * }</pre>
     *
     * @param str1       the String to compare from.
     * @param str2       the String to compare to.
     * @param nullIsLess whether consider {@code null} value less than non-{@code null} value.
     * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal ou greater than {@code str2}.
     * @see String#compareTo(String)
     */
    public static int compare(final String str1, final String str2, final boolean nullIsLess) {
        if (str1 == str2) { // NOSONARLINT this intentionally uses == to allow for both null
            return 0;
        }
        if (str1 == null) {
            return nullIsLess ? -1 : 1;
        }
        if (str2 == null) {
            return nullIsLess ? 1 : -1;
        }
        return str1.compareTo(str2);
    }

    /**
     * Compares two Strings lexicographically, ignoring case differences, as per {@link String#compareToIgnoreCase(String)}, returning :
     * <ul>
     * <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
     * <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
     * <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
     * </ul>
     *
     * <p>
     * This is a {@code null} safe version of :
     * </p>
     * <pre>
     * str1.compareToIgnoreCase(str2)
     * </pre>
     *
     * <p>
     * {@code null} inputs are handled according to the {@code nullIsLess} parameter. Two {@code null} references are considered equal. Comparison is case
     * insensitive.
     * </p>
     *
     * <pre>{@code
     * StringUtil.compareIgnoreCase(null, null, *)     = 0
     * StringUtil.compareIgnoreCase(null , "a", true)  < 0
     * StringUtil.compareIgnoreCase(null , "a", false) > 0
     * StringUtil.compareIgnoreCase("a", null, true)   > 0
     * StringUtil.compareIgnoreCase("a", null, false)  < 0
     * StringUtil.compareIgnoreCase("abc", "abc", *)   = 0
     * StringUtil.compareIgnoreCase("abc", "ABC", *)   = 0
     * StringUtil.compareIgnoreCase("a", "b", *)       < 0
     * StringUtil.compareIgnoreCase("b", "a", *)       > 0
     * StringUtil.compareIgnoreCase("a", "B", *)       < 0
     * StringUtil.compareIgnoreCase("A", "b", *)       < 0
     * StringUtil.compareIgnoreCase("ab", "abc", *)    < 0
     * }</pre>
     *
     * @param str1       the String to compare from.
     * @param str2       the String to compare to.
     * @param nullIsLess whether consider {@code null} value less than non-{@code null} value.
     * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal ou greater than {@code str2}, ignoring case differences.
     * @see String#compareToIgnoreCase(String)
     */
    public static int compareIgnoreCase(final String str1, final String str2, final boolean nullIsLess) {
        if (str1 == str2) { // NOSONARLINT this intentionally uses == to allow for both null
            return 0;
        }
        if (str1 == null) {
            return nullIsLess ? -1 : 1;
        }
        if (str2 == null) {
            return nullIsLess ? 1 : -1;
        }
        return str1.compareToIgnoreCase(str2);
    }

    /**
     * Tests that the CharSequence does not contain certain characters.
     *
     * <p>
     * A {@code null} CharSequence will return {@code true}. A {@code null} invalid character array will return {@code true}. An empty CharSequence (length()=0)
     * always returns true.
     * </p>
     *
     * <pre>
     * StringUtil.containsNone(null, *)               = true
     * StringUtil.containsNone(*, null)               = true
     * StringUtil.containsNone("", *)                 = true
     * StringUtil.containsNone("ab", '')              = true
     * StringUtil.containsNone("abab", 'x', 'y', 'z') = true
     * StringUtil.containsNone("ab1", 'x', 'y', 'z')  = true
     * StringUtil.containsNone("abz", 'x', 'y', 'z')  = false
     * </pre>
     *
     * @param cs          the CharSequence to check, may be null.
     * @param searchChars an array of invalid chars, may be null.
     * @return true if it contains none of the invalid chars, or is null.
     */
    public static boolean containsNone(final CharSequence cs, final char... searchChars) {
        if (cs == null || searchChars == null) {
            return true;
        }
        final int csLen = cs.length();
        final int csLast = csLen - 1;
        final int searchLen = searchChars.length;
        final int searchLast = searchLen - 1;
        for (int i = 0; i < csLen; i++) {
            final char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    if (!Character.isHighSurrogate(ch) || j == searchLast || i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Tests that the CharSequence does not contain certain characters.
     *
     * <p>
     * A {@code null} CharSequence will return {@code true}. A {@code null} invalid character array will return {@code true}. An empty String ("") always
     * returns true.
     * </p>
     *
     * <pre>
     * StringUtil.containsNone(null, *)       = true
     * StringUtil.containsNone(*, null)       = true
     * StringUtil.containsNone("", *)         = true
     * StringUtil.containsNone("ab", "")      = true
     * StringUtil.containsNone("abab", "xyz") = true
     * StringUtil.containsNone("ab1", "xyz")  = true
     * StringUtil.containsNone("abz", "xyz")  = false
     * </pre>
     *
     * @param cs           the CharSequence to check, may be null.
     * @param invalidChars a String of invalid chars, may be null.
     * @return true if it contains none of the invalid chars, or is null.
     */
    public static boolean containsNone(final CharSequence cs, final String invalidChars) {
        if (invalidChars == null) {
            return true;
        }
        return containsNone(cs, invalidChars.toCharArray());
    }

    /**
     * Tests whether the given CharSequence contains any whitespace characters.
     *
     * <p>
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StringUtil.containsWhitespace(null)       = false
     * StringUtil.containsWhitespace("")         = false
     * StringUtil.containsWhitespace("ab")       = false
     * StringUtil.containsWhitespace(" ab")      = true
     * StringUtil.containsWhitespace("a b")      = true
     * StringUtil.containsWhitespace("ab ")      = true
     * </pre>
     *
     * @param seq the CharSequence to check (may be {@code null}).
     * @return {@code true} if the CharSequence is not empty and contains at least 1 (breaking) whitespace character.
     */
    public static boolean containsWhitespace(final CharSequence seq) {
        if (isEmpty(seq)) {
            return false;
        }
        final int strLen = seq.length();
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(seq.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static void convertRemainingAccentCharacters(final StringBuilder decomposed) {
        for (int i = 0; i < decomposed.length(); i++) {
            final char charAt = decomposed.charAt(i);
            switch (charAt) {
                case '\u0141':
                    decomposed.setCharAt(i, 'L');
                    break;
                case '\u0142':
                    decomposed.setCharAt(i, 'l');
                    break;
                // D with stroke
                case '\u0110':
                    // LATIN CAPITAL LETTER D WITH STROKE
                    decomposed.setCharAt(i, 'D');
                    break;
                case '\u0111':
                    // LATIN SMALL LETTER D WITH STROKE
                    decomposed.setCharAt(i, 'd');
                    break;
                // I with bar
                case '\u0197':
                    decomposed.setCharAt(i, 'I');
                    break;
                case '\u0268':
                    decomposed.setCharAt(i, 'i');
                    break;
                case '\u1D7B':
                    decomposed.setCharAt(i, 'I');
                    break;
                case '\u1DA4':
                    decomposed.setCharAt(i, 'i');
                    break;
                case '\u1DA7':
                    decomposed.setCharAt(i, 'I');
                    break;
                // U with bar
                case '\u0244':
                    // LATIN CAPITAL LETTER U BAR
                    decomposed.setCharAt(i, 'U');
                    break;
                case '\u0289':
                    // LATIN SMALL LETTER U BAR
                    decomposed.setCharAt(i, 'u');
                    break;
                case '\u1D7E':
                    // LATIN SMALL CAPITAL LETTER U WITH STROKE
                    decomposed.setCharAt(i, 'U');
                    break;
                case '\u1DB6':
                    // MODIFIER LETTER SMALL U BAR
                    decomposed.setCharAt(i, 'u');
                    break;
                // T with stroke
                case '\u0166':
                    // LATIN CAPITAL LETTER T WITH STROKE
                    decomposed.setCharAt(i, 'T');
                    break;
                case '\u0167':
                    // LATIN SMALL LETTER T WITH STROKE
                    decomposed.setCharAt(i, 't');
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Counts how many times the char appears in the given string.
     *
     * <p>
     * A {@code null} or empty ("") String input returns {@code 0}.
     * </p>
     *
     * <pre>
     * StringUtil.countMatches(null, *)     = 0
     * StringUtil.countMatches("", *)       = 0
     * StringUtil.countMatches("abba", 0)   = 0
     * StringUtil.countMatches("abba", 'a') = 2
     * StringUtil.countMatches("abba", 'b') = 2
     * StringUtil.countMatches("abba", 'x') = 0
     * </pre>
     *
     * @param str the CharSequence to check, may be null.
     * @param ch  the char to count.
     * @return the number of occurrences, 0 if the CharSequence is {@code null}.
     */
    public static int countMatches(final CharSequence str, final char ch) {
        if (isEmpty(str)) {
            return 0;
        }
        int count = 0;
        // We could also call str.toCharArray() for faster lookups but that would generate more garbage.
        for (int i = 0; i < str.length(); i++) {
            if (ch == str.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns either the passed in CharSequence, or if the CharSequence is {@link #isBlank(CharSequence) blank} (whitespaces, empty ({@code ""}), or
     * {@code null}), the value of {@code defaultStr}.
     *
     * <p>
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StringUtil.defaultIfBlank(null, "NULL")  = "NULL"
     * StringUtil.defaultIfBlank("", "NULL")    = "NULL"
     * StringUtil.defaultIfBlank(" ", "NULL")   = "NULL"
     * StringUtil.defaultIfBlank("bat", "NULL") = "bat"
     * StringUtil.defaultIfBlank("", null)      = null
     * </pre>
     *
     * @param <T>        the specific kind of CharSequence.
     * @param str        the CharSequence to check, may be null.
     * @param defaultStr the default CharSequence to return if {@code str} is {@link #isBlank(CharSequence) blank} (whitespaces, empty ({@code ""}), or
     *                   {@code null}); may be null.
     * @return the passed in CharSequence, or the default.
     * @see #isBlank(CharSequence)
     */
    public static <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    /**
     * Returns either the passed in CharSequence, or if the CharSequence is empty or {@code null}, the value of {@code defaultStr}.
     *
     * <pre>
     * StringUtil.defaultIfEmpty(null, "NULL")  = "NULL"
     * StringUtil.defaultIfEmpty("", "NULL")    = "NULL"
     * StringUtil.defaultIfEmpty(" ", "NULL")   = " "
     * StringUtil.defaultIfEmpty("bat", "NULL") = "bat"
     * StringUtil.defaultIfEmpty("", null)      = null
     * </pre>
     *
     * @param <T>        the specific kind of CharSequence.
     * @param str        the CharSequence to check, may be null.
     * @param defaultStr the default CharSequence to return if the input is empty ("") or {@code null}, may be null.
     * @return the passed in CharSequence, or the default.
     */
    public static <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * Returns either the passed in String, or if the String is {@code null}, an empty String ("").
     *
     * <pre>
     * StringUtil.defaultString(null)  = ""
     * StringUtil.defaultString("")    = ""
     * StringUtil.defaultString("bat") = "bat"
     * </pre>
     *
     * @param str the String to check, may be null.
     * @return the passed in String, or the empty String if it was {@code null}.
     * @see Objects#toString(Object, String)
     * @see String#valueOf(Object)
     */
    public static String defaultString(final String str) {
        return Objects.toString(str, EMPTY);
    }

    /**
     * Deletes all whitespaces from a String as defined by {@link Character#isWhitespace(char)}.
     *
     * <pre>
     * StringUtil.deleteWhitespace(null)         = null
     * StringUtil.deleteWhitespace("")           = ""
     * StringUtil.deleteWhitespace("abc")        = "abc"
     * StringUtil.deleteWhitespace("   ab  c  ") = "abc"
     * </pre>
     *
     * @param str the String to delete whitespace from, may be null.
     * @return the String without whitespaces, {@code null} if null String input.
     */
    public static String deleteWhitespace(final String str) {
        if (isEmpty(str)) {
            return str;
        }
        final int sz = str.length();
        final char[] chs = new char[sz];
        int count = 0;
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                chs[count++] = str.charAt(i);
            }
        }
        if (count == sz) {
            return str;
        }
        if (count == 0) {
            return EMPTY;
        }
        return new String(chs, 0, count);
    }

    /**
     * Compares two Strings, and returns the portion where they differ. More precisely, return the remainder of the second String, starting from where it's
     * different from the first. This means that the difference between "abc" and "ab" is the empty String and not "c".
     *
     * <p>
     * For example, {@code difference("i am a machine", "i am a robot") -> "robot"}.
     * </p>
     *
     * <pre>
     * StringUtil.difference(null, null)       = null
     * StringUtil.difference("", "")           = ""
     * StringUtil.difference("", "abc")        = "abc"
     * StringUtil.difference("abc", "")        = ""
     * StringUtil.difference("abc", "abc")     = ""
     * StringUtil.difference("abc", "ab")      = ""
     * StringUtil.difference("ab", "abxyz")    = "xyz"
     * StringUtil.difference("abcde", "abxyz") = "xyz"
     * StringUtil.difference("abcde", "xyz")   = "xyz"
     * </pre>
     *
     * @param str1 the first String, may be null.
     * @param str2 the second String, may be null.
     * @return the portion of str2 where it differs from str1; returns the empty String if they are equal.
     * @see #indexOfDifference(CharSequence, CharSequence)
     */
    public static String difference(final String str1, final String str2) {
        if (str1 == null) {
            return str2;
        }
        if (str2 == null) {
            return str1;
        }
        final int at = indexOfDifference(str1, str2);
        if (at == INDEX_NOT_FOUND) {
            return EMPTY;
        }
        return str2.substring(at);
    }

    /**
     * Returns the first value in the array which is not empty (""), {@code null} or whitespace only.
     *
     * <p>
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <p>
     * If all values are blank or the array is {@code null} or empty then {@code null} is returned.
     * </p>
     *
     * <pre>
     * StringUtil.firstNonBlank(null, null, null)     = null
     * StringUtil.firstNonBlank(null, "", " ")        = null
     * StringUtil.firstNonBlank("abc")                = "abc"
     * StringUtil.firstNonBlank(null, "xyz")          = "xyz"
     * StringUtil.firstNonBlank(null, "", " ", "xyz") = "xyz"
     * StringUtil.firstNonBlank(null, "xyz", "abc")   = "xyz"
     * StringUtil.firstNonBlank()                     = null
     * </pre>
     *
     * @param <T>    the specific kind of CharSequence.
     * @param values the values to test, may be {@code null} or empty.
     * @return the first value from {@code values} which is not blank, or {@code null} if there are no non-blank values.
     */
    @SafeVarargs
    public static <T extends CharSequence> T firstNonBlank(final T... values) {
        if (values != null) {
            for (final T val : values) {
                if (isNotBlank(val)) {
                    return val;
                }
            }
        }
        return null;
    }

    /**
     * Returns the first value in the array which is not empty.
     *
     * <p>
     * If all values are empty or the array is {@code null} or empty then {@code null} is returned.
     * </p>
     *
     * <pre>
     * StringUtil.firstNonEmpty(null, null, null)   = null
     * StringUtil.firstNonEmpty(null, null, "")     = null
     * StringUtil.firstNonEmpty(null, "", " ")      = " "
     * StringUtil.firstNonEmpty("abc")              = "abc"
     * StringUtil.firstNonEmpty(null, "xyz")        = "xyz"
     * StringUtil.firstNonEmpty("", "xyz")          = "xyz"
     * StringUtil.firstNonEmpty(null, "xyz", "abc") = "xyz"
     * StringUtil.firstNonEmpty()                   = null
     * </pre>
     *
     * @param <T>    the specific kind of CharSequence.
     * @param values the values to test, may be {@code null} or empty.
     * @return the first value from {@code values} which is not empty, or {@code null} if there are no non-empty values.
     */
    @SafeVarargs
    public static <T extends CharSequence> T firstNonEmpty(final T... values) {
        if (values != null) {
            for (final T val : values) {
                if (isNotEmpty(val)) {
                    return val;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a String {@code str} contains Unicode digits, if yes then concatenate all the digits in {@code str} and return it as a String.
     *
     * <p>
     * An empty ("") String will be returned if no digits found in {@code str}.
     * </p>
     *
     * <pre>
     * StringUtil.getDigits(null)                 = null
     * StringUtil.getDigits("")                   = ""
     * StringUtil.getDigits("abc")                = ""
     * StringUtil.getDigits("1000$")              = "1000"
     * StringUtil.getDigits("1123~45")            = "112345"
     * StringUtil.getDigits("(541) 754-3010")     = "5417543010"
     * StringUtil.getDigits("\u0967\u0968\u0969") = "\u0967\u0968\u0969"
     * </pre>
     *
     * @param str the String to extract digits from, may be null.
     * @return String with only digits, or an empty ("") String if no digits found, or {@code null} String if {@code str} is null.
     */
    public static String getDigits(final String str) {
        if (isEmpty(str)) {
            return str;
        }
        final int len = str.length();
        final char[] buffer = new char[len];
        int count = 0;

        for (int i = 0; i < len; i++) {
            final char tempChar = str.charAt(i);
            if (Character.isDigit(tempChar)) {
                buffer[count++] = tempChar;
            }
        }
        return new String(buffer, 0, count);
    }

    /**
     * Search a CharSequence to find the first index of any character not in the given set of characters, i.e., find index i of first char in seq such that
     * (seq.codePointAt(i) ∉ { x ∈ codepoints(searchChars) })
     *
     * <p>
     * A {@code null} CharSequence will return {@code -1}. A {@code null} or empty search string will return {@code -1}.
     * </p>
     *
     * <pre>
     * StringUtil.indexOfAnyBut(null, *)            = -1
     * StringUtil.indexOfAnyBut("", *)              = -1
     * StringUtil.indexOfAnyBut(*, null)            = -1
     * StringUtil.indexOfAnyBut(*, "")              = -1
     * StringUtil.indexOfAnyBut("zzabyycdxx", "za") = 3
     * StringUtil.indexOfAnyBut("zzabyycdxx", "")   = -1
     * StringUtil.indexOfAnyBut("aba", "ab")        = -1
     * </pre>
     *
     * @param seq         the CharSequence to check, may be null.
     * @param searchChars the chars to search for, may be null.
     * @return the index of any of the chars, -1 if no match or null input.
     */
    public static int indexOfAnyBut(final CharSequence seq, final CharSequence searchChars) {
        if (isEmpty(seq) || isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        final Set<Integer> searchSetCodePoints = searchChars.codePoints()
                .boxed().collect(Collectors.toSet());
        // advance character index from one interpreted codepoint to the next
        for (int curSeqCharIdx = 0; curSeqCharIdx < seq.length(); ) {
            final int curSeqCodePoint = Character.codePointAt(seq, curSeqCharIdx);
            if (!searchSetCodePoints.contains(curSeqCodePoint)) {
                return curSeqCharIdx;
            }
            curSeqCharIdx += Character.charCount(curSeqCodePoint); // skip indices to paired low-surrogates
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * Compares two CharSequences, and returns the index at which the CharSequences begin to differ.
     *
     * <p>
     * For example, {@code indexOfDifference("i am a machine", "i am a robot") -> 7}
     * </p>
     *
     * <pre>
     * StringUtil.indexOfDifference(null, null)       = -1
     * StringUtil.indexOfDifference("", "")           = -1
     * StringUtil.indexOfDifference("", "abc")        = 0
     * StringUtil.indexOfDifference("abc", "")        = 0
     * StringUtil.indexOfDifference("abc", "abc")     = -1
     * StringUtil.indexOfDifference("ab", "abxyz")    = 2
     * StringUtil.indexOfDifference("abcde", "abxyz") = 2
     * StringUtil.indexOfDifference("abcde", "xyz")   = 0
     * </pre>
     *
     * @param cs1 the first CharSequence, may be null.
     * @param cs2 the second CharSequence, may be null.
     * @return the index where cs1 and cs2 begin to differ; -1 if they are equal.
     */
    public static int indexOfDifference(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return INDEX_NOT_FOUND;
        }
        if (cs1 == null || cs2 == null) {
            return 0;
        }
        int i;
        for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                break;
            }
        }
        if (i < cs2.length() || i < cs1.length()) {
            return i;
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * Tests if the CharSequence contains only lowercase characters.
     *
     * <p>
     * {@code null} will return {@code false}. An empty CharSequence (length()=0) will return {@code false}.
     * </p>
     *
     * <pre>
     * StringUtil.isAllLowerCase(null)   = false
     * StringUtil.isAllLowerCase("")     = false
     * StringUtil.isAllLowerCase("  ")   = false
     * StringUtil.isAllLowerCase("abc")  = true
     * StringUtil.isAllLowerCase("abC")  = false
     * StringUtil.isAllLowerCase("ab c") = false
     * StringUtil.isAllLowerCase("ab1c") = false
     * StringUtil.isAllLowerCase("ab/c") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if only contains lowercase characters, and is non-null.
     */
    public static boolean isAllLowerCase(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLowerCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if the CharSequence contains only uppercase characters.
     *
     * <p>{@code null} will return {@code false}.
     * An empty String (length()=0) will return {@code false}.</p>
     *
     * <pre>
     * StringUtil.isAllUpperCase(null)   = false
     * StringUtil.isAllUpperCase("")     = false
     * StringUtil.isAllUpperCase("  ")   = false
     * StringUtil.isAllUpperCase("ABC")  = true
     * StringUtil.isAllUpperCase("aBC")  = false
     * StringUtil.isAllUpperCase("A C")  = false
     * StringUtil.isAllUpperCase("A1C")  = false
     * StringUtil.isAllUpperCase("A/C")  = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if only contains uppercase characters, and is non-null.
     */
    public static boolean isAllUpperCase(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isUpperCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if the CharSequence contains only Unicode letters.
     *
     * <p>
     * {@code null} will return {@code false}. An empty CharSequence (length()=0) will return {@code false}.
     * </p>
     *
     * <pre>
     * StringUtil.isAlpha(null)   = false
     * StringUtil.isAlpha("")     = false
     * StringUtil.isAlpha("  ")   = false
     * StringUtil.isAlpha("abc")  = true
     * StringUtil.isAlpha("ab2c") = false
     * StringUtil.isAlpha("ab-c") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if only contains letters, and is non-null.
     */
    public static boolean isAlpha(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLetter(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if the CharSequence contains only Unicode letters or digits.
     *
     * <p>
     * {@code null} will return {@code false}. An empty CharSequence (length()=0) will return {@code false}.
     * </p>
     *
     * <pre>
     * StringUtil.isAlphanumeric(null)   = false
     * StringUtil.isAlphanumeric("")     = false
     * StringUtil.isAlphanumeric("  ")   = false
     * StringUtil.isAlphanumeric("abc")  = true
     * StringUtil.isAlphanumeric("ab c") = false
     * StringUtil.isAlphanumeric("ab2c") = true
     * StringUtil.isAlphanumeric("ab-c") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if only contains letters or digits, and is non-null.
     */
    public static boolean isAlphanumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLetterOrDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if the CharSequence contains only Unicode letters, digits or space ({@code ' '}).
     *
     * <p>
     * {@code null} will return {@code false}. An empty CharSequence (length()=0) will return {@code true}.
     * </p>
     *
     * <pre>
     * StringUtil.isAlphanumericSpace(null)   = false
     * StringUtil.isAlphanumericSpace("")     = true
     * StringUtil.isAlphanumericSpace("  ")   = true
     * StringUtil.isAlphanumericSpace("abc")  = true
     * StringUtil.isAlphanumericSpace("ab c") = true
     * StringUtil.isAlphanumericSpace("ab2c") = true
     * StringUtil.isAlphanumericSpace("ab-c") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if only contains letters, digits or space, and is non-null.
     */
    public static boolean isAlphanumericSpace(final CharSequence cs) {
        if (cs == null) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            final char nowChar = cs.charAt(i);
            if (nowChar != ' ' && !Character.isLetterOrDigit(nowChar)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if the CharSequence contains only Unicode letters and space (' ').
     *
     * <p>
     * {@code null} will return {@code false} An empty CharSequence (length()=0) will return {@code true}.
     * </p>
     *
     * <pre>
     * StringUtil.isAlphaSpace(null)   = false
     * StringUtil.isAlphaSpace("")     = true
     * StringUtil.isAlphaSpace("  ")   = true
     * StringUtil.isAlphaSpace("abc")  = true
     * StringUtil.isAlphaSpace("ab c") = true
     * StringUtil.isAlphaSpace("ab2c") = false
     * StringUtil.isAlphaSpace("ab-c") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if only contains letters and space, and is non-null.
     */
    public static boolean isAlphaSpace(final CharSequence cs) {
        if (cs == null) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            final char nowChar = cs.charAt(i);
            if (nowChar != ' ' && !Character.isLetter(nowChar)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if a CharSequence is empty ({@code "")}, null, or contains only whitespace as defined by {@link Character#isWhitespace(char)}.
     *
     * <pre>
     * StringUtil.isBlank(null)      = true
     * StringUtil.isBlank("")        = true
     * StringUtil.isBlank(" ")       = true
     * StringUtil.isBlank("bob")     = false
     * StringUtil.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if the CharSequence is null, empty or whitespace only.
     */
    public static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if a CharSequence is empty ("") or null.
     *
     * <pre>
     * StringUtil.isEmpty(null)      = true
     * StringUtil.isEmpty("")        = true
     * StringUtil.isEmpty(" ")       = false
     * StringUtil.isEmpty("bob")     = false
     * StringUtil.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>
     * NOTE: This method changed in Lang version 2.0. It no longer trims the CharSequence. That functionality is available in isBlank().
     * </p>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if the CharSequence is empty or null.
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Tests if the CharSequence contains mixed casing of both uppercase and lowercase characters.
     *
     * <p>
     * {@code null} will return {@code false}. An empty CharSequence ({@code length()=0}) will return {@code false}.
     * </p>
     *
     * <pre>
     * StringUtil.isMixedCase(null)    = false
     * StringUtil.isMixedCase("")      = false
     * StringUtil.isMixedCase(" ")     = false
     * StringUtil.isMixedCase("ABC")   = false
     * StringUtil.isMixedCase("abc")   = false
     * StringUtil.isMixedCase("aBc")   = true
     * StringUtil.isMixedCase("A c")   = true
     * StringUtil.isMixedCase("A1c")   = true
     * StringUtil.isMixedCase("a/C")   = true
     * StringUtil.isMixedCase("aC\t")  = true
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if the CharSequence contains both uppercase and lowercase characters.
     */
    public static boolean isMixedCase(final CharSequence cs) {
        if (isEmpty(cs) || cs.length() == 1) {
            return false;
        }
        boolean containsUppercase = false;
        boolean containsLowercase = false;
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            final char nowChar = cs.charAt(i);
            if (Character.isUpperCase(nowChar)) {
                containsUppercase = true;
            } else if (Character.isLowerCase(nowChar)) {
                containsLowercase = true;
            }
            if (containsUppercase && containsLowercase) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if a CharSequence is not {@link #isBlank(CharSequence) blank} (whitespaces, empty ({@code ""}), or {@code null}).
     *
     * <p>
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StringUtil.isNotBlank(null)      = false
     * StringUtil.isNotBlank("")        = false
     * StringUtil.isNotBlank(" ")       = false
     * StringUtil.isNotBlank("bob")     = true
     * StringUtil.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if the CharSequence is not {@link #isBlank(CharSequence) blank} (whitespaces, empty ({@code ""}), or {@code null}).
     * @see #isBlank(CharSequence)
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Tests if a CharSequence is not empty ("") and not null.
     *
     * <pre>
     * StringUtil.isNotEmpty(null)      = false
     * StringUtil.isNotEmpty("")        = false
     * StringUtil.isNotEmpty(" ")       = true
     * StringUtil.isNotEmpty("bob")     = true
     * StringUtil.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if the CharSequence is not empty and not null.
     */
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * Tests if the CharSequence contains only Unicode digits. A decimal point is not a Unicode digit and returns false.
     *
     * <p>
     * {@code null} will return {@code false}. An empty CharSequence (length()=0) will return {@code false}.
     * </p>
     *
     * <p>
     * Note that the method does not allow for a leading sign, either positive or negative. Also, if a String passes the numeric test, it may still generate a
     * NumberFormatException when parsed by Integer.parseInt or Long.parseLong, e.g. if the value is outside the range for int or long respectively.
     * </p>
     *
     * <pre>
     * StringUtil.isNumeric(null)   = false
     * StringUtil.isNumeric("")     = false
     * StringUtil.isNumeric("  ")   = false
     * StringUtil.isNumeric("123")  = true
     * StringUtil.isNumeric("\u0967\u0968\u0969")  = true
     * StringUtil.isNumeric("12 3") = false
     * StringUtil.isNumeric("ab2c") = false
     * StringUtil.isNumeric("12-3") = false
     * StringUtil.isNumeric("12.3") = false
     * StringUtil.isNumeric("-123") = false
     * StringUtil.isNumeric("+123") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if only contains digits, and is non-null.
     */
    public static boolean isNumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if the CharSequence contains only Unicode digits or space ({@code ' '}). A decimal point is not a Unicode digit and returns false.
     *
     * <p>
     * {@code null} will return {@code false}. An empty CharSequence (length()=0) will return {@code true}.
     * </p>
     *
     * <pre>
     * StringUtil.isNumericSpace(null)   = false
     * StringUtil.isNumericSpace("")     = true
     * StringUtil.isNumericSpace("  ")   = true
     * StringUtil.isNumericSpace("123")  = true
     * StringUtil.isNumericSpace("12 3") = true
     * StringUtil.isNumericSpace("\u0967\u0968\u0969")   = true
     * StringUtil.isNumericSpace("\u0967\u0968 \u0969")  = true
     * StringUtil.isNumericSpace("ab2c") = false
     * StringUtil.isNumericSpace("12-3") = false
     * StringUtil.isNumericSpace("12.3") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if only contains digits or space, and is non-null.
     */
    public static boolean isNumericSpace(final CharSequence cs) {
        if (cs == null) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            final char nowChar = cs.charAt(i);
            if (nowChar != ' ' && !Character.isDigit(nowChar)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if the CharSequence contains only whitespace.
     *
     * <p>
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <p>
     * {@code null} will return {@code false}. An empty CharSequence (length()=0) will return {@code true}.
     * </p>
     *
     * <pre>
     * StringUtil.isWhitespace(null)   = false
     * StringUtil.isWhitespace("")     = true
     * StringUtil.isWhitespace("  ")   = true
     * StringUtil.isWhitespace("abc")  = false
     * StringUtil.isWhitespace("ab2c") = false
     * StringUtil.isWhitespace("ab-c") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null.
     * @return {@code true} if only contains whitespace, and is non-null.
     */
    public static boolean isWhitespace(final CharSequence cs) {
        if (cs == null) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)             = null
     * StringUtil.join([], *)               = ""
     * StringUtil.join([null], *)           = ""
     * StringUtil.join([false, false], ';') = "false;false"
     * </pre>
     *
     * @param array     the array of values to join together, may be null.
     * @param delimiter the separator character to use.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final boolean[] array, final char delimiter) {
        if (array == null) {
            return null;
        }
        return join(array, delimiter, 0, array.length);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)                  = null
     * StringUtil.join([], *)                    = ""
     * StringUtil.join([null], *)                = ""
     * StringUtil.join([true, false, true], ';') = "true;false;true"
     * </pre>
     *
     * @param array      the array of values to join together, may be null.
     * @param delimiter  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the
     *                   array.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
     *                   the array.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final boolean[] array, final char delimiter, final int startIndex, final int endIndex) {
        // See StringUtilsJoinBenchmark
        if (array == null) {
            return null;
        }
        final int count = endIndex - startIndex;
        if (count <= 0) {
            return EMPTY;
        }
        final byte maxElementChars = 5; // "false"
        final StringBuilder stringBuilder = capacity(count, maxElementChars);
        stringBuilder.append(array[startIndex]);
        for (int i = startIndex + 1; i < endIndex; i++) {
            stringBuilder.append(delimiter).append(array[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)         = null
     * StringUtil.join([], *)           = ""
     * StringUtil.join([null], *)       = ""
     * StringUtil.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtil.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array     the array of values to join together, may be null.
     * @param delimiter the separator character to use.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final byte[] array, final char delimiter) {
        if (array == null) {
            return null;
        }
        return join(array, delimiter, 0, array.length);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)         = null
     * StringUtil.join([], *)           = ""
     * StringUtil.join([null], *)       = ""
     * StringUtil.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtil.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array      the array of values to join together, may be null.
     * @param delimiter  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the
     *                   array.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
     *                   the array.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final byte[] array, final char delimiter, final int startIndex, final int endIndex) {
        // See StringUtilsJoinBenchmark
        if (array == null) {
            return null;
        }
        final int count = endIndex - startIndex;
        if (count <= 0) {
            return EMPTY;
        }
        final byte maxElementChars = 4; // "-128"
        final StringBuilder stringBuilder = capacity(count, maxElementChars);
        stringBuilder.append(array[startIndex]);
        for (int i = startIndex + 1; i < endIndex; i++) {
            stringBuilder.append(delimiter).append(array[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)         = null
     * StringUtil.join([], *)           = ""
     * StringUtil.join([null], *)       = ""
     * StringUtil.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtil.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array     the array of values to join together, may be null.
     * @param delimiter the separator character to use.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final char[] array, final char delimiter) {
        if (array == null) {
            return null;
        }
        return join(array, delimiter, 0, array.length);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)         = null
     * StringUtil.join([], *)           = ""
     * StringUtil.join([null], *)       = ""
     * StringUtil.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtil.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array      the array of values to join together, may be null.
     * @param delimiter  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the
     *                   array.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
     *                   the array.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final char[] array, final char delimiter, final int startIndex, final int endIndex) {
        // See StringUtilsJoinBenchmark
        if (array == null) {
            return null;
        }
        final int count = endIndex - startIndex;
        if (count <= 0) {
            return EMPTY;
        }
        final byte maxElementChars = 1;
        final StringBuilder stringBuilder = capacity(count, maxElementChars);
        stringBuilder.append(array[startIndex]);
        for (int i = startIndex + 1; i < endIndex; i++) {
            stringBuilder.append(delimiter).append(array[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)          = null
     * StringUtil.join([], *)            = ""
     * StringUtil.join([null], *)        = ""
     * StringUtil.join([1, 2, 3], ';')   = "1;2;3"
     * StringUtil.join([1, 2, 3], null)  = "123"
     * </pre>
     *
     * @param array     the array of values to join together, may be null.
     * @param delimiter the separator character to use.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final double[] array, final char delimiter) {
        if (array == null) {
            return null;
        }
        return join(array, delimiter, 0, array.length);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)          = null
     * StringUtil.join([], *)            = ""
     * StringUtil.join([null], *)        = ""
     * StringUtil.join([1, 2, 3], ';')   = "1;2;3"
     * StringUtil.join([1, 2, 3], null)  = "123"
     * </pre>
     *
     * @param array      the array of values to join together, may be null.
     * @param delimiter  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the
     *                   array.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
     *                   the array.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final double[] array, final char delimiter, final int startIndex, final int endIndex) {
        // See StringUtilsJoinBenchmark
        if (array == null) {
            return null;
        }
        final int count = endIndex - startIndex;
        if (count <= 0) {
            return EMPTY;
        }
        final byte maxElementChars = 22; // "1.7976931348623157E308"
        final StringBuilder stringBuilder = capacity(count, maxElementChars);
        stringBuilder.append(array[startIndex]);
        for (int i = startIndex + 1; i < endIndex; i++) {
            stringBuilder.append(delimiter).append(array[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)          = null
     * StringUtil.join([], *)            = ""
     * StringUtil.join([null], *)        = ""
     * StringUtil.join([1, 2, 3], ';')   = "1;2;3"
     * StringUtil.join([1, 2, 3], null)  = "123"
     * </pre>
     *
     * @param array     the array of values to join together, may be null.
     * @param delimiter the separator character to use.
     * @return the joined String, {@code null} if null array input
     */
    public static String join(final float[] array, final char delimiter) {
        if (array == null) {
            return null;
        }
        return join(array, delimiter, 0, array.length);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)          = null
     * StringUtil.join([], *)            = ""
     * StringUtil.join([null], *)        = ""
     * StringUtil.join([1, 2, 3], ';')   = "1;2;3"
     * StringUtil.join([1, 2, 3], null)  = "123"
     * </pre>
     *
     * @param array      the array of values to join together, may be null.
     * @param delimiter  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the
     *                   array.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
     *                   the array.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final float[] array, final char delimiter, final int startIndex, final int endIndex) {
        // See StringUtilsJoinBenchmark
        if (array == null) {
            return null;
        }
        final int count = endIndex - startIndex;
        if (count <= 0) {
            return EMPTY;
        }
        final byte maxElementChars = 12; // "3.4028235E38"
        final StringBuilder stringBuilder = capacity(count, maxElementChars);
        stringBuilder.append(array[startIndex]);
        for (int i = startIndex + 1; i < endIndex; i++) {
            stringBuilder.append(delimiter).append(array[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)          = null
     * StringUtil.join([], *)            = ""
     * StringUtil.join([null], *)        = ""
     * StringUtil.join([1, 2, 3], ';')   = "1;2;3"
     * StringUtil.join([1, 2, 3], null)  = "123"
     * </pre>
     *
     * @param array     the array of values to join together, may be null.
     * @param separator the separator character to use.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final int[] array, final char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)          = null
     * StringUtil.join([], *)            = ""
     * StringUtil.join([null], *)        = ""
     * StringUtil.join([1, 2, 3], ';')   = "1;2;3"
     * StringUtil.join([1, 2, 3], null)  = "123"
     * </pre>
     *
     * @param array      the array of values to join together, may be null.
     * @param delimiter  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the
     *                   array.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
     *                   the array.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final int[] array, final char delimiter, final int startIndex, final int endIndex) {
        // See StringUtilsJoinBenchmark
        if (array == null) {
            return null;
        }
        final int count = endIndex - startIndex;
        if (count <= 0) {
            return EMPTY;
        }
        final byte maxElementChars = 11; // "-2147483648"
        final StringBuilder stringBuilder = capacity(count, maxElementChars);
        stringBuilder.append(array[startIndex]);
        for (int i = startIndex + 1; i < endIndex; i++) {
            stringBuilder.append(delimiter).append(array[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * Joins the elements of the provided {@link Iterable} into a single String containing the provided elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the iteration are represented by empty strings.
     * </p>
     *
     * <p>
     * See the examples here: {@link #join(Object[], char)}.
     * </p>
     *
     * @param iterable  the {@link Iterable} providing the values to join together, may be null.
     * @param separator the separator character to use.
     * @return the joined String, {@code null} if null iterator input.
     */
    public static String join(final Iterable<?> iterable, final char separator) {
        return iterable != null ? join(iterable.iterator(), separator) : null;
    }

    /**
     * Joins the elements of the provided {@link Iterable} into a single String containing the provided elements.
     *
     * <p>
     * No delimiter is added before or after the list. A {@code null} separator is the same as an empty String ("").
     * </p>
     *
     * <p>
     * </p>
     *
     * @param iterable  the {@link Iterable} providing the values to join together, may be null.
     * @param separator the separator character to use, null treated as "".
     * @return the joined String, {@code null} if null iterator input.
     */
    public static String join(final Iterable<?> iterable, final String separator) {
        return iterable != null ? join(iterable.iterator(), separator) : null;
    }

    /**
     * Joins the elements of the provided {@link List} into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)               = null
     * StringUtil.join([], *)                 = ""
     * StringUtil.join([null], *)             = ""
     * StringUtil.join(["a", "b", "c"], ';')  = "a;b;c"
     * StringUtil.join(["a", "b", "c"], null) = "abc"
     * StringUtil.join([null, "", "a"], ';')  = ";;a"
     * </pre>
     *
     * @param list       the {@link List} of values to join together, may be null.
     * @param separator  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the list.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of the list.
     * @return the joined String, {@code null} if null list input.
     */
    public static String join(final List<?> list, final char separator, final int startIndex, final int endIndex) {
        if (list == null) {
            return null;
        }
        final int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        final List<?> subList = list.subList(startIndex, endIndex);
        return join(subList.iterator(), separator);
    }

    /**
     * Joins the elements of the provided {@link List} into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)               = null
     * StringUtil.join([], *)                 = ""
     * StringUtil.join([null], *)             = ""
     * StringUtil.join(["a", "b", "c"], ';')  = "a;b;c"
     * StringUtil.join(["a", "b", "c"], null) = "abc"
     * StringUtil.join([null, "", "a"], ';')  = ";;a"
     * </pre>
     *
     * @param list       the {@link List} of values to join together, may be null.
     * @param separator  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the list.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of the list.
     * @return the joined String, {@code null} if null list input.
     */
    public static String join(final List<?> list, final String separator, final int startIndex, final int endIndex) {
        if (list == null) {
            return null;
        }
        final int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }
        final List<?> subList = list.subList(startIndex, endIndex);
        return join(subList.iterator(), separator);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)               = null
     * StringUtil.join([], *)                 = ""
     * StringUtil.join([null], *)             = ""
     * StringUtil.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtil.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array     the array of values to join together, may be null.
     * @param separator the separator character to use.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final long[] array, final char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)               = null
     * StringUtil.join([], *)                 = ""
     * StringUtil.join([null], *)             = ""
     * StringUtil.join([1, 2, 3], ';')  = "1;2;3"
     * StringUtil.join([1, 2, 3], null) = "123"
     * </pre>
     *
     * @param array      the array of values to join together, may be null.
     * @param delimiter  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the
     *                   array.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
     *                   the array.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final long[] array, final char delimiter, final int startIndex, final int endIndex) {
        // See StringUtilsJoinBenchmark
        if (array == null) {
            return null;
        }
        final int count = endIndex - startIndex;
        if (count <= 0) {
            return EMPTY;
        }
        final byte maxElementChars = 20; // "-9223372036854775808"
        final StringBuilder stringBuilder = capacity(count, maxElementChars);
        stringBuilder.append(array[startIndex]);
        for (int i = startIndex + 1; i < endIndex; i++) {
            stringBuilder.append(delimiter).append(array[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)               = null
     * StringUtil.join([], *)                 = ""
     * StringUtil.join([null], *)             = ""
     * StringUtil.join(["a", "b", "c"], ';')  = "a;b;c"
     * StringUtil.join(["a", "b", "c"], null) = "abc"
     * StringUtil.join([null, "", "a"], ';')  = ";;a"
     * </pre>
     *
     * @param array     the array of values to join together, may be null.
     * @param delimiter the separator character to use.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final Object[] array, final char delimiter) {
        if (array == null) {
            return null;
        }
        return join(array, delimiter, 0, array.length);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)               = null
     * StringUtil.join([], *)                 = ""
     * StringUtil.join([null], *)             = ""
     * StringUtil.join(["a", "b", "c"], ';')  = "a;b;c"
     * StringUtil.join(["a", "b", "c"], null) = "abc"
     * StringUtil.join([null, "", "a"], ';')  = ";;a"
     * </pre>
     *
     * @param array      the array of values to join together, may be null.
     * @param delimiter  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the array.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of the array.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final Object[] array, final char delimiter, final int startIndex, final int endIndex) {
        return join(array, String.valueOf(delimiter), startIndex, endIndex);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)          = null
     * StringUtil.join([], *)            = ""
     * StringUtil.join([null], *)        = ""
     * StringUtil.join([1, 2, 3], ';')   = "1;2;3"
     * StringUtil.join([1, 2, 3], null)  = "123"
     * </pre>
     *
     * @param array     the array of values to join together, may be null.
     * @param delimiter the separator character to use.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final short[] array, final char delimiter) {
        if (array == null) {
            return null;
        }
        return join(array, delimiter, 0, array.length);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
     * by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null, *)          = null
     * StringUtil.join([], *)            = ""
     * StringUtil.join([null], *)        = ""
     * StringUtil.join([1, 2, 3], ';')   = "1;2;3"
     * StringUtil.join([1, 2, 3], null)  = "123"
     * </pre>
     *
     * @param array      the array of values to join together, may be null.
     * @param delimiter  the separator character to use.
     * @param startIndex the first index to start joining from. It is an error to pass in a start index past the end of the
     *                   array.
     * @param endIndex   the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
     *                   the array.
     * @return the joined String, {@code null} if null array input.
     */
    public static String join(final short[] array, final char delimiter, final int startIndex, final int endIndex) {
        // See StringUtilsJoinBenchmark
        if (array == null) {
            return null;
        }
        final int count = endIndex - startIndex;
        if (count <= 0) {
            return EMPTY;
        }
        final byte maxElementChars = 6; // "-32768"
        final StringBuilder stringBuilder = capacity(count, maxElementChars);
        stringBuilder.append(array[startIndex]);
        for (int i = startIndex + 1; i < endIndex; i++) {
            stringBuilder.append(delimiter).append(array[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided list of elements.
     *
     * <p>
     * No separator is added to the joined String. Null objects or empty strings within the array are represented by empty strings.
     * </p>
     *
     * <pre>
     * StringUtil.join(null)            = null
     * StringUtil.join([])              = ""
     * StringUtil.join([null])          = ""
     * StringUtil.join("a", "b", "c")   = "abc"
     * StringUtil.join(null, "", "a")   = "a"
     * </pre>
     *
     * @param <T>      the specific type of values to join together.
     * @param elements the values to join together, may be null.
     * @return the joined String, {@code null} if null array input.
     */
    @SafeVarargs
    public static <T> String join(final T... elements) {
        return join(elements, null);
    }

    /**
     * Joins the elements of the provided varargs into a single String containing the provided elements.
     *
     * <p>
     * No delimiter is added before or after the list. {@code null} elements and separator are treated as empty Strings ("").
     * </p>
     *
     * <pre>
     * StringUtil.joinWith(",", "a", "b")        = "a,b"
     * StringUtil.joinWith(",", "a", "b","")     = "a,b,"
     * StringUtil.joinWith(",", "a", null, "b")  = "a,,b"
     * StringUtil.joinWith(null, "a", "b")       = "ab"
     * </pre>
     *
     * @param delimiter the separator character to use, null treated as "".
     * @param array     the varargs providing the values to join together. {@code null} elements are treated as "".
     * @return the joined String.
     * @throws IllegalArgumentException if a null varargs is provided.
     */
    public static String joinWith(final String delimiter, final Object... array) {
        if (array == null) {
            throw new IllegalArgumentException("Object varargs must not be null");
        }
        return join(array, delimiter);
    }

    /**
     * Gets the leftmost {@code len} characters of a String.
     *
     * <p>
     * If {@code len} characters are not available, or the String is {@code null}, the String will be returned without an exception. An empty String is returned
     * if len is negative.
     * </p>
     *
     * <pre>
     * StringUtil.left(null, *)    = null
     * StringUtil.left(*, -ve)     = ""
     * StringUtil.left("", *)      = ""
     * StringUtil.left("abc", 0)   = ""
     * StringUtil.left("abc", 2)   = "ab"
     * StringUtil.left("abc", 4)   = "abc"
     * </pre>
     *
     * @param str the String to get the leftmost characters from, may be null.
     * @param len the length of the required String.
     * @return the leftmost characters, {@code null} if null String input.
     */
    public static String left(final String str, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return EMPTY;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(0, len);
    }

    /**
     * Gets a CharSequence length or {@code 0} if the CharSequence is {@code null}.
     *
     * @param cs a CharSequence or {@code null}.
     * @return CharSequence length or {@code 0} if the CharSequence is {@code null}.
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * Converts a String to lower case as per {@link String#toLowerCase()}.
     *
     * <p>
     * A {@code null} input String returns {@code null}.
     * </p>
     *
     * <pre>
     * StringUtil.lowerCase(null)  = null
     * StringUtil.lowerCase("")    = ""
     * StringUtil.lowerCase("aBc") = "abc"
     * </pre>
     *
     * <p>
     * <strong>Note:</strong> As described in the documentation for {@link String#toLowerCase()}, the result of this method is affected by the current locale.
     * {@link Locale#ENGLISH}).
     * </p>
     *
     * @param str the String to lower case, may be null.
     * @return the lower cased String, {@code null} if null String input.
     */
    public static String lowerCase(final String str) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase();
    }

    /**
     * Gets {@code len} characters from the middle of a String.
     *
     * <p>
     * If {@code len} characters are not available, the remainder of the String will be returned without an exception. If the String is {@code null},
     * {@code null} will be returned. An empty String is returned if len is negative or exceeds the length of {@code str}.
     * </p>
     *
     * <pre>
     * StringUtil.mid(null, *, *)    = null
     * StringUtil.mid(*, *, -ve)     = ""
     * StringUtil.mid("", 0, *)      = ""
     * StringUtil.mid("abc", 0, 2)   = "ab"
     * StringUtil.mid("abc", 0, 4)   = "abc"
     * StringUtil.mid("abc", 2, 4)   = "c"
     * StringUtil.mid("abc", 4, 2)   = ""
     * StringUtil.mid("abc", -2, 2)  = "ab"
     * </pre>
     *
     * @param str the String to get the characters from, may be null.
     * @param pos the position to start from, negative treated as zero.
     * @param len the length of the required String.
     * @return the middle characters, {@code null} if null String input.
     */
    public static String mid(final String str, int pos, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0 || pos > str.length()) {
            return EMPTY;
        }
        if (pos < 0) {
            pos = 0;
        }
        if (str.length() <= pos + len) {
            return str.substring(pos);
        }
        return str.substring(pos, pos + len);
    }

    /**
     * Similar to <a href="https://www.w3.org/TR/xpath/#function-normalize-space">https://www.w3.org/TR/xpath/#function-normalize -space</a>
     *
     * <p>
     * This function returns the argument string with whitespace normalized by using {@code {@link #trim(String)}} to remove leading and trailing whitespace and
     * then replacing sequences of whitespace characters by a single space.
     * </p>
     * In XML, whitespace characters are the same as those allowed by the <a href="https://www.w3.org/TR/REC-xml/#NT-S">S</a> production, which is S ::= (#x20 |
     * #x9 | #xD | #xA)+
     * <p>
     * Java's regexp pattern \s defines whitespace as [ \t\n\x0B\f\r]
     * </p>
     * <p>
     * For reference:
     * </p>
     * <ul>
     * <li>\x0B = vertical tab</li>
     * <li>\f = #xC = form feed</li>
     * <li>#x20 = space</li>
     * <li>#x9 = \t</li>
     * <li>#xA = \n</li>
     * <li>#xD = \r</li>
     * </ul>
     *
     * <p>
     * The difference is that Java's whitespace includes vertical tab and form feed, which this function will also normalize. Additionally {@code {@link
     * #trim(String)}} removes control characters (char &lt;= 32) from both ends of this String.
     * </p>
     *
     * @param str the source String to normalize whitespaces from, may be null.
     * @return the modified string with whitespace normalized, {@code null} if null String input.
     * @see Pattern
     * @see #trim(String)
     * @see <a href="https://www.w3.org/TR/xpath/#function-normalize-space">https://www.w3.org/TR/xpath/#function-normalize-space</a>
     */
    public static String normalizeSpace(final String str) {
        // LANG-1020: Improved performance significantly by normalizing manually instead of using regex
        // See https://github.com/librucha/commons-lang-normalizespaces-benchmark for performance test
        if (isEmpty(str)) {
            return str;
        }
        final int size = str.length();
        final char[] newChars = new char[size];
        int count = 0;
        int whitespacesCount = 0;
        boolean startWhitespaces = true;
        for (int i = 0; i < size; i++) {
            final char actualChar = str.charAt(i);
            final boolean isWhitespace = Character.isWhitespace(actualChar);
            if (isWhitespace) {
                if (whitespacesCount == 0 && !startWhitespaces) {
                    newChars[count++] = SPACE.charAt(0);
                }
                whitespacesCount++;
            } else {
                startWhitespaces = false;
                newChars[count++] = actualChar == 160 ? 32 : actualChar;
                whitespacesCount = 0;
            }
        }
        if (startWhitespaces) {
            return EMPTY;
        }
        return new String(newChars, 0, count - (whitespacesCount > 0 ? 1 : 0)).trim();
    }

    /**
     * Overlays part of a String with another String.
     *
     * <p>
     * A {@code null} string input returns {@code null}. A negative index is treated as zero. An index greater than the string length is treated as the string
     * length. The start index is always the smaller of the two indices.
     * </p>
     *
     * <pre>
     * StringUtil.overlay(null, *, *, *)            = null
     * StringUtil.overlay("", "abc", 0, 0)          = "abc"
     * StringUtil.overlay("abcdef", null, 2, 4)     = "abef"
     * StringUtil.overlay("abcdef", "", 2, 4)       = "abef"
     * StringUtil.overlay("abcdef", "", 4, 2)       = "abef"
     * StringUtil.overlay("abcdef", "zzzz", 2, 4)   = "abzzzzef"
     * StringUtil.overlay("abcdef", "zzzz", 4, 2)   = "abzzzzef"
     * StringUtil.overlay("abcdef", "zzzz", -1, 4)  = "zzzzef"
     * StringUtil.overlay("abcdef", "zzzz", 2, 8)   = "abzzzz"
     * StringUtil.overlay("abcdef", "zzzz", -2, -3) = "zzzzabcdef"
     * StringUtil.overlay("abcdef", "zzzz", 8, 10)  = "abcdefzzzz"
     * </pre>
     *
     * @param str     the String to do overlaying in, may be null.
     * @param overlay the String to overlay, may be null.
     * @param start   the position to start overlaying at.
     * @param end     the position to stop overlaying before.
     * @return overlayed String, {@code null} if null String input.
     */
    public static String overlay(final String str, String overlay, int start, int end) {
        if (str == null) {
            return null;
        }
        if (overlay == null) {
            overlay = EMPTY;
        }
        final int len = str.length();
        if (start < 0) {
            start = 0;
        }
        if (start > len) {
            start = len;
        }
        if (end < 0) {
            end = 0;
        }
        if (end > len) {
            end = len;
        }
        if (start > end) {
            final int temp = start;
            start = end;
            end = temp;
        }
        return str.substring(0, start) + overlay + str.substring(end);
    }

    /**
     * Removes all occurrences of a character from within the source string.
     *
     * <p>
     * A {@code null} source string will return {@code null}. An empty ("") source string will return the empty string.
     * </p>
     *
     * <pre>
     * StringUtil.remove(null, *)       = null
     * StringUtil.remove("", *)         = ""
     * StringUtil.remove("queued", 'u') = "qeed"
     * StringUtil.remove("queued", 'z') = "queued"
     * </pre>
     *
     * @param str    the source String to search, may be null.
     * @param remove the char to search for and remove, may be null.
     * @return the substring with the char removed if found, {@code null} if null String input.
     */
    public static String remove(final String str, final char remove) {
        if (isEmpty(str) || str.indexOf(remove) == INDEX_NOT_FOUND) {
            return str;
        }
        final char[] chars = str.toCharArray();
        int pos = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != remove) {
                chars[pos++] = chars[i];
            }
        }
        return new String(chars, 0, pos);
    }

    /**
     * Removes a char only if it is at the beginning of a source string, otherwise returns the source string.
     *
     * <p>
     * A {@code null} source string will return {@code null}. An empty ("") source string will return the empty string. A {@code null} search char will return
     * the source string.
     * </p>
     *
     * <pre>
     * StringUtil.removeStart(null, *)      = null
     * StringUtil.removeStart("", *)        = ""
     * StringUtil.removeStart(*, null)      = *
     * StringUtil.removeStart("/path", '/') = "path"
     * StringUtil.removeStart("path", '/')  = "path"
     * StringUtil.removeStart("path", 0)    = "path"
     * </pre>
     *
     * @param str    the source String to search, may be null.
     * @param remove the char to search for and remove.
     * @return the substring with the char removed if found, {@code null} if null String input.
     */
    public static String removeStart(final String str, final char remove) {
        if (isEmpty(str)) {
            return str;
        }
        return str.charAt(0) == remove ? str.substring(1) : str;
    }

    /**
     * Replaces all occurrences of a character in a String with another. This is a null-safe version of {@link String#replace(char, char)}.
     *
     * <p>
     * A {@code null} string input returns {@code null}. An empty ("") string input returns an empty string.
     * </p>
     *
     * <pre>
     * StringUtil.replaceChars(null, *, *)        = null
     * StringUtil.replaceChars("", *, *)          = ""
     * StringUtil.replaceChars("abcba", 'b', 'y') = "aycya"
     * StringUtil.replaceChars("abcba", 'z', 'y') = "abcba"
     * </pre>
     *
     * @param str         String to replace characters in, may be null.
     * @param searchChar  the character to search for, may be null.
     * @param replaceChar the character to replace, may be null.
     * @return modified String, {@code null} if null string input.
     */
    public static String replaceChars(final String str, final char searchChar, final char replaceChar) {
        if (str == null) {
            return null;
        }
        return str.replace(searchChar, replaceChar);
    }

    /**
     * Reverses a String as per {@link StringBuilder#reverse()}.
     *
     * <p>
     * A {@code null} String returns {@code null}.
     * </p>
     *
     * <pre>
     * StringUtil.reverse(null)  = null
     * StringUtil.reverse("")    = ""
     * StringUtil.reverse("bat") = "tab"
     * </pre>
     *
     * @param str the String to reverse, may be null.
     * @return the reversed String, {@code null} if null String input.
     */
    public static String reverse(final String str) {
        if (str == null) {
            return null;
        }
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * Gets the rightmost {@code len} characters of a String.
     *
     * <p>
     * If {@code len} characters are not available, or the String is {@code null}, the String will be returned without an exception. An empty String is
     * returned if len is negative.
     * </p>
     *
     * <pre>
     * StringUtil.right(null, *)    = null
     * StringUtil.right(*, -ve)     = ""
     * StringUtil.right("", *)      = ""
     * StringUtil.right("abc", 0)   = ""
     * StringUtil.right("abc", 2)   = "bc"
     * StringUtil.right("abc", 4)   = "abc"
     * </pre>
     *
     * @param str the String to get the rightmost characters from, may be null.
     * @param len the length of the required String.
     * @return the rightmost characters, {@code null} if null String input.
     */
    public static String right(final String str, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return EMPTY;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(str.length() - len);
    }

    /**
     * Rotate (circular shift) a String of {@code shift} characters.
     * <ul>
     * <li>If {@code shift > 0}, right circular shift (ex : ABCDEF =&gt; FABCDE)</li>
     * <li>If {@code shift < 0}, left circular shift (ex : ABCDEF =&gt; BCDEFA)</li>
     * </ul>
     *
     * <pre>
     * StringUtil.rotate(null, *)        = null
     * StringUtil.rotate("", *)          = ""
     * StringUtil.rotate("abcdefg", 0)   = "abcdefg"
     * StringUtil.rotate("abcdefg", 2)   = "fgabcde"
     * StringUtil.rotate("abcdefg", -2)  = "cdefgab"
     * StringUtil.rotate("abcdefg", 7)   = "abcdefg"
     * StringUtil.rotate("abcdefg", -7)  = "abcdefg"
     * StringUtil.rotate("abcdefg", 9)   = "fgabcde"
     * StringUtil.rotate("abcdefg", -9)  = "cdefgab"
     * </pre>
     *
     * @param str   the String to rotate, may be null.
     * @param shift number of time to shift (positive : right shift, negative : left shift).
     * @return the rotated String, or the original String if {@code shift == 0}, or {@code null} if null String input.
     */
    public static String rotate(final String str, final int shift) {
        if (str == null) {
            return null;
        }
        final int strLen = str.length();
        if (shift == 0 || strLen == 0 || shift % strLen == 0) {
            return str;
        }
        final StringBuilder builder = new StringBuilder(strLen);
        final int offset = -(shift % strLen);
        builder.append(substring(str, offset));
        builder.append(substring(str, 0, offset));
        return builder.toString();
    }

    /**
     * Strips whitespace from the start and end of a String.
     *
     * <p>
     * This is similar to {@link #trim(String)} but removes whitespace. Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <p>
     * A {@code null} input String returns {@code null}.
     * </p>
     *
     * <pre>
     * StringUtil.strip(null)     = null
     * StringUtil.strip("")       = ""
     * StringUtil.strip("   ")    = ""
     * StringUtil.strip("abc")    = "abc"
     * StringUtil.strip("  abc")  = "abc"
     * StringUtil.strip("abc  ")  = "abc"
     * StringUtil.strip(" abc ")  = "abc"
     * StringUtil.strip(" ab c ") = "ab c"
     * </pre>
     *
     * @param str the String to remove whitespace from, may be null.
     * @return the stripped String, {@code null} if null String input.
     */
    public static String strip(final String str) {
        return strip(str, null);
    }

    /**
     * Strips any of a set of characters from the start and end of a String. This is similar to {@link String#trim()} but allows the characters to be stripped
     * to be controlled.
     *
     * <p>
     * A {@code null} input String returns {@code null}. An empty string ("") input returns the empty string.
     * </p>
     *
     * <p>
     * If the stripChars String is {@code null}, whitespace is stripped as defined by {@link Character#isWhitespace(char)}. Alternatively use
     * {@link #strip(String)}.
     * </p>
     *
     * <pre>
     * StringUtil.strip(null, *)          = null
     * StringUtil.strip("", *)            = ""
     * StringUtil.strip("abc", null)      = "abc"
     * StringUtil.strip("  abc", null)    = "abc"
     * StringUtil.strip("abc  ", null)    = "abc"
     * StringUtil.strip(" abc ", null)    = "abc"
     * StringUtil.strip("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str        the String to remove characters from, may be null.
     * @param stripChars the characters to remove, null treated as whitespace.
     * @return the stripped String, {@code null} if null String input.
     */
    public static String strip(String str, final String stripChars) {
        str = stripStart(str, stripChars);
        return stripEnd(str, stripChars);
    }

    /**
     * Removes diacritics (~= accents) from a string. The case will not be altered.
     * <p>
     * For instance, '&agrave;' will be replaced by 'a'.
     * </p>
     * <p>
     * Decomposes ligatures and digraphs per the KD column in the <a href = "https://www.unicode.org/charts/normalization/">Unicode Normalization Chart.</a>
     * </p>
     *
     * <pre>
     * StringUtil.stripAccents(null)         = null
     * StringUtil.stripAccents("")           = ""
     * StringUtil.stripAccents("control")    = "control"
     * StringUtil.stripAccents("&eacute;clair")     = "eclair"
     * StringUtil.stripAccents("\u1d43\u1d47\u1d9c\u00b9\u00b2\u00b3")     = "abc123"
     * StringUtil.stripAccents("\u00BC \u00BD \u00BE")      = "1⁄4 1⁄2 3⁄4"
     * </pre>
     * <p>
     * See also <a href="https://www.unicode.org/unicode/reports/tr15/tr15-23.html">Unicode Standard Annex #15 Unicode Normalization Forms</a>.
     * </p>
     *
     * @param input String to be stripped.
     * @return input text with diacritics removed.
     */
    public static String stripAccents(final String input) {
        if (isEmpty(input)) {
            return input;
        }
        final StringBuilder decomposed = new StringBuilder(Normalizer.normalize(input, Normalizer.Form.NFKD));
        convertRemainingAccentCharacters(decomposed);
        return STRIP_ACCENTS_PATTERN.matcher(decomposed).replaceAll(EMPTY);
    }

    /**
     * Strips any of a set of characters from the end of a String.
     *
     * <p>
     * A {@code null} input String returns {@code null}. An empty string ("") input returns the empty string.
     * </p>
     *
     * <p>
     * If the stripChars String is {@code null}, whitespace is stripped as defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StringUtil.stripEnd(null, *)          = null
     * StringUtil.stripEnd("", *)            = ""
     * StringUtil.stripEnd("abc", "")        = "abc"
     * StringUtil.stripEnd("abc", null)      = "abc"
     * StringUtil.stripEnd("  abc", null)    = "  abc"
     * StringUtil.stripEnd("abc  ", null)    = "abc"
     * StringUtil.stripEnd(" abc ", null)    = " abc"
     * StringUtil.stripEnd("  abcyx", "xyz") = "  abc"
     * StringUtil.stripEnd("120.00", ".0")   = "12"
     * </pre>
     *
     * @param str        the String to remove characters from, may be null.
     * @param stripChars the set of characters to remove, null treated as whitespace.
     * @return the stripped String, {@code null} if null String input.
     */
    public static String stripEnd(final String str, final String stripChars) {
        int end = length(str);
        if (end == 0) {
            return str;
        }
        if (stripChars == null) {
            while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    /**
     * Strips any of a set of characters from the start of a String.
     *
     * <p>
     * A {@code null} input String returns {@code null}. An empty string ("") input returns the empty string.
     * </p>
     *
     * <p>
     * If the stripChars String is {@code null}, whitespace is stripped as defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StringUtil.stripStart(null, *)          = null
     * StringUtil.stripStart("", *)            = ""
     * StringUtil.stripStart("abc", "")        = "abc"
     * StringUtil.stripStart("abc", null)      = "abc"
     * StringUtil.stripStart("  abc", null)    = "abc"
     * StringUtil.stripStart("abc  ", null)    = "abc  "
     * StringUtil.stripStart(" abc ", null)    = "abc "
     * StringUtil.stripStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     *
     * @param str        the String to remove characters from, may be null.
     * @param stripChars the characters to remove, null treated as whitespace.
     * @return the stripped String, {@code null} if null String input.
     */
    public static String stripStart(final String str, final String stripChars) {
        final int strLen = length(str);
        if (strLen == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while (start != strLen && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (start != strLen && stripChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND) {
                start++;
            }
        }
        return str.substring(start);
    }

    /**
     * Strips whitespace from the start and end of a String returning an empty String if {@code null} input.
     *
     * <p>
     * This is similar to {@link #trimToEmpty(String)} but removes whitespace. Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StringUtil.stripToEmpty(null)     = ""
     * StringUtil.stripToEmpty("")       = ""
     * StringUtil.stripToEmpty("   ")    = ""
     * StringUtil.stripToEmpty("abc")    = "abc"
     * StringUtil.stripToEmpty("  abc")  = "abc"
     * StringUtil.stripToEmpty("abc  ")  = "abc"
     * StringUtil.stripToEmpty(" abc ")  = "abc"
     * StringUtil.stripToEmpty(" ab c ") = "ab c"
     * </pre>
     *
     * @param str the String to be stripped, may be null.
     * @return the trimmed String, or an empty String if {@code null} input.
     */
    public static String stripToEmpty(final String str) {
        return str == null ? EMPTY : strip(str, null);
    }

    /**
     * Strips whitespace from the start and end of a String returning {@code null} if the String is empty ("") after the strip.
     *
     * <p>
     * This is similar to {@link #trimToNull(String)} but removes whitespace. Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StringUtil.stripToNull(null)     = null
     * StringUtil.stripToNull("")       = null
     * StringUtil.stripToNull("   ")    = null
     * StringUtil.stripToNull("abc")    = "abc"
     * StringUtil.stripToNull("  abc")  = "abc"
     * StringUtil.stripToNull("abc  ")  = "abc"
     * StringUtil.stripToNull(" abc ")  = "abc"
     * StringUtil.stripToNull(" ab c ") = "ab c"
     * </pre>
     *
     * @param str the String to be stripped, may be null.
     * @return the stripped String, {@code null} if whitespace, empty or null String input.
     */
    public static String stripToNull(String str) {
        if (str == null) {
            return null;
        }
        str = strip(str, null);
        return str.isEmpty() ? null : str; // NOSONARLINT str cannot be null here
    }

    /**
     * Gets a substring from the specified String avoiding exceptions.
     *
     * <p>
     * A negative start position can be used to start {@code n} characters from the end of the String.
     * </p>
     *
     * <p>
     * A {@code null} String will return {@code null}. An empty ("") String will return "".
     * </p>
     *
     * <pre>
     * StringUtil.substring(null, *)   = null
     * StringUtil.substring("", *)     = ""
     * StringUtil.substring("abc", 0)  = "abc"
     * StringUtil.substring("abc", 2)  = "c"
     * StringUtil.substring("abc", 4)  = ""
     * StringUtil.substring("abc", -2) = "bc"
     * StringUtil.substring("abc", -4) = "abc"
     * </pre>
     *
     * @param str   the String to get the substring from, may be null.
     * @param start the position to start from, negative means count back from the end of the String by this many characters.
     * @return substring from start position, {@code null} if null String input.
     */
    public static String substring(final String str, int start) {
        if (str == null) {
            return null;
        }
        // handle negatives, which means last n characters
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }
        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return EMPTY;
        }
        return str.substring(start);
    }

    /**
     * Gets a substring from the specified String avoiding exceptions.
     *
     * <p>
     * A negative start position can be used to start/end {@code n} characters from the end of the String.
     * </p>
     *
     * <p>
     * The returned substring starts with the character in the {@code start} position and ends before the {@code end} position. All position counting is
     * zero-based -- i.e., to start at the beginning of the string use {@code start = 0}. Negative start and end positions can be used to specify offsets
     * relative to the end of the String.
     * </p>
     *
     * <p>
     * If {@code start} is not strictly to the left of {@code end}, "" is returned.
     * </p>
     *
     * <pre>
     * StringUtil.substring(null, *, *)    = null
     * StringUtil.substring("", * ,  *)    = "";
     * StringUtil.substring("abc", 0, 2)   = "ab"
     * StringUtil.substring("abc", 2, 0)   = ""
     * StringUtil.substring("abc", 2, 4)   = "c"
     * StringUtil.substring("abc", 4, 6)   = ""
     * StringUtil.substring("abc", 2, 2)   = ""
     * StringUtil.substring("abc", -2, -1) = "b"
     * StringUtil.substring("abc", -4, 2)  = "ab"
     * </pre>
     *
     * @param str   the String to get the substring from, may be null.
     * @param start the position to start from, negative means count back from the end of the String by this many characters.
     * @param end   the position to end at (exclusive), negative means count back from the end of the String by this many characters.
     * @return substring from start position to end position, {@code null} if null String input.
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }
        // handle negatives
        if (end < 0) {
            end = str.length() + end; // remember end is negative
        }
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }
        // check length next
        if (end > str.length()) {
            end = str.length();
        }
        // if start is greater than end, return ""
        if (start > end) {
            return EMPTY;
        }
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
        return str.substring(start, end);
    }

    /**
     * Gets the substring after the first occurrence of a separator. The separator is not returned.
     *
     * <p>
     * A {@code null} string input will return {@code null}. An empty ("") string input will return the empty string.
     * </p>
     *
     * <p>
     * If nothing is found, the empty string is returned.
     * </p>
     *
     * <pre>
     * StringUtil.substringAfter(null, *)      = null
     * StringUtil.substringAfter("", *)        = ""
     * StringUtil.substringAfter("abc", 'a')   = "bc"
     * StringUtil.substringAfter("abcba", 'b') = "cba"
     * StringUtil.substringAfter("abc", 'c')   = ""
     * StringUtil.substringAfter("abc", 'd')   = ""
     * StringUtil.substringAfter(" abc", 32)   = "abc"
     * </pre>
     *
     * @param str  the String to get a substring from, may be null.
     * @param find the character (Unicode code point) to find.
     * @return the substring after the first occurrence of the specified character, {@code null} if null String input.
     */
    public static String substringAfter(final String str, final int find) {
        if (isEmpty(str)) {
            return str;
        }
        final int pos = str.indexOf(find);
        if (pos == INDEX_NOT_FOUND) {
            return EMPTY;
        }
        return str.substring(pos + 1);
    }

    /**
     * Gets the substring after the first occurrence of a separator. The separator is not returned.
     *
     * <p>
     * A {@code null} string input will return {@code null}. An empty ("") string input will return the empty string. A {@code null} separator will return the
     * empty string if the input string is not {@code null}.
     * </p>
     *
     * <p>
     * If nothing is found, the empty string is returned.
     * </p>
     *
     * <pre>
     * StringUtil.substringAfter(null, *)      = null
     * StringUtil.substringAfter("", *)        = ""
     * StringUtil.substringAfter(*, null)      = ""
     * StringUtil.substringAfter("abc", "a")   = "bc"
     * StringUtil.substringAfter("abcba", "b") = "cba"
     * StringUtil.substringAfter("abc", "c")   = ""
     * StringUtil.substringAfter("abc", "d")   = ""
     * StringUtil.substringAfter("abc", "")    = "abc"
     * </pre>
     *
     * @param str  the String to get a substring from, may be null.
     * @param find the String to find, may be null.
     * @return the substring after the first occurrence of the specified string, {@code null} if null String input.
     */
    public static String substringAfter(final String str, final String find) {
        if (isEmpty(str)) {
            return str;
        }
        if (find == null) {
            return EMPTY;
        }
        final int pos = str.indexOf(find);
        if (pos == INDEX_NOT_FOUND) {
            return EMPTY;
        }
        return str.substring(pos + find.length());
    }

    /**
     * Gets the substring after the last occurrence of a separator. The separator is not returned.
     *
     * <p>
     * A {@code null} string input will return {@code null}. An empty ("") string input will return the empty string.
     * </p>
     *
     * <p>
     * If nothing is found, the empty string is returned.
     * </p>
     *
     * <pre>
     * StringUtil.substringAfterLast(null, *)      = null
     * StringUtil.substringAfterLast("", *)        = ""
     * StringUtil.substringAfterLast("abc", 'a')   = "bc"
     * StringUtil.substringAfterLast(" bc", 32)    = "bc"
     * StringUtil.substringAfterLast("abcba", 'b') = "a"
     * StringUtil.substringAfterLast("abc", 'c')   = ""
     * StringUtil.substringAfterLast("a", 'a')     = ""
     * StringUtil.substringAfterLast("a", 'z')     = ""
     * </pre>
     *
     * @param str  the String to get a substring from, may be null.
     * @param find the character (Unicode code point) to find.
     * @return the substring after the last occurrence of the specified character, {@code null} if null String input.
     */
    public static String substringAfterLast(final String str, final int find) {
        if (isEmpty(str)) {
            return str;
        }
        final int pos = str.lastIndexOf(find);
        if (pos == INDEX_NOT_FOUND || pos == str.length() - 1) {
            return EMPTY;
        }
        return str.substring(pos + 1);
    }

    /**
     * Gets the substring after the last occurrence of a separator. The separator is not returned.
     *
     * <p>
     * A {@code null} string input will return {@code null}. An empty ("") string input will return the empty string. An empty or {@code null} separator will
     * return the empty string if the input string is not {@code null}.
     * </p>
     *
     * <p>
     * If nothing is found, the empty string is returned.
     * </p>
     *
     * <pre>
     * StringUtil.substringAfterLast(null, *)      = null
     * StringUtil.substringAfterLast("", *)        = ""
     * StringUtil.substringAfterLast(*, "")        = ""
     * StringUtil.substringAfterLast(*, null)      = ""
     * StringUtil.substringAfterLast("abc", "a")   = "bc"
     * StringUtil.substringAfterLast("abcba", "b") = "a"
     * StringUtil.substringAfterLast("abc", "c")   = ""
     * StringUtil.substringAfterLast("a", "a")     = ""
     * StringUtil.substringAfterLast("a", "z")     = ""
     * </pre>
     *
     * @param str  the String to get a substring from, may be null.
     * @param find the String to find, may be null.
     * @return the substring after the last occurrence of the specified string, {@code null} if null String input.
     */
    public static String substringAfterLast(final String str, final String find) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(find)) {
            return EMPTY;
        }
        final int pos = str.lastIndexOf(find);
        if (pos == INDEX_NOT_FOUND || pos == str.length() - find.length()) {
            return EMPTY;
        }
        return str.substring(pos + find.length());
    }

    /**
     * Gets the substring before the first occurrence of a separator. The separator is not returned.
     *
     * <p>
     * A {@code null} string input will return {@code null}. An empty ("") string input will return the empty string.
     * </p>
     *
     * <p>
     * If nothing is found, the string input is returned.
     * </p>
     *
     * <pre>
     * StringUtil.substringBefore(null, *)      = null
     * StringUtil.substringBefore("", *)        = ""
     * StringUtil.substringBefore("abc", 'a')   = ""
     * StringUtil.substringBefore("abcba", 'b') = "a"
     * StringUtil.substringBefore("abc", 'c')   = "ab"
     * StringUtil.substringBefore("abc", 'd')   = "abc"
     * </pre>
     *
     * @param str  the String to get a substring from, may be null.
     * @param find the character (Unicode code point) to find.
     * @return the substring before the first occurrence of the specified character, {@code null} if null String input.
     */
    public static String substringBefore(final String str, final int find) {
        if (isEmpty(str)) {
            return str;
        }
        final int pos = str.indexOf(find);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * Gets the substring before the first occurrence of a separator. The separator is not returned.
     *
     * <p>
     * A {@code null} string input will return {@code null}. An empty ("") string input will return the empty string. A {@code null} separator will return the
     * input string.
     * </p>
     *
     * <p>
     * If nothing is found, the string input is returned.
     * </p>
     *
     * <pre>
     * StringUtil.substringBefore(null, *)      = null
     * StringUtil.substringBefore("", *)        = ""
     * StringUtil.substringBefore("abc", "a")   = ""
     * StringUtil.substringBefore("abcba", "b") = "a"
     * StringUtil.substringBefore("abc", "c")   = "ab"
     * StringUtil.substringBefore("abc", "d")   = "abc"
     * StringUtil.substringBefore("abc", "")    = ""
     * StringUtil.substringBefore("abc", null)  = "abc"
     * </pre>
     *
     * @param str  the String to get a substring from, may be null.
     * @param find the String to find, may be null.
     * @return the substring before the first occurrence of the specified string, {@code null} if null String input.
     */
    public static String substringBefore(final String str, final String find) {
        if (isEmpty(str) || find == null) {
            return str;
        }
        if (find.isEmpty()) {
            return EMPTY;
        }
        final int pos = str.indexOf(find);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * Gets the substring before the last occurrence of a separator. The separator is not returned.
     *
     * <p>
     * A {@code null} string input will return {@code null}. An empty ("") string input will return the empty string. An empty or {@code null} separator will
     * return the input string.
     * </p>
     *
     * <p>
     * If nothing is found, the string input is returned.
     * </p>
     *
     * <pre>
     * StringUtil.substringBeforeLast(null, *)      = null
     * StringUtil.substringBeforeLast("", *)        = ""
     * StringUtil.substringBeforeLast("abcba", "b") = "abc"
     * StringUtil.substringBeforeLast("abc", "c")   = "ab"
     * StringUtil.substringBeforeLast("a", "a")     = ""
     * StringUtil.substringBeforeLast("a", "z")     = "a"
     * StringUtil.substringBeforeLast("a", null)    = "a"
     * StringUtil.substringBeforeLast("a", "")      = "a"
     * </pre>
     *
     * @param str  the String to get a substring from, may be null.
     * @param find the String to find, may be null.
     * @return the substring before the last occurrence of the specified string, {@code null} if null String input.
     */
    public static String substringBeforeLast(final String str, final String find) {
        if (isEmpty(str) || isEmpty(find)) {
            return str;
        }
        final int pos = str.lastIndexOf(find);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * Converts the given source String as a lower-case using the {@link Locale#ROOT} locale in a null-safe manner.
     *
     * @param source A source String or null.
     * @return the given source String as a lower-case using the {@link Locale#ROOT} locale or null.
     */
    public static String toRootLowerCase(final String source) {
        return source == null ? null : source.toLowerCase(Locale.ROOT);
    }

    /**
     * Converts the given source String as an upper-case using the {@link Locale#ROOT} locale in a null-safe manner.
     *
     * @param source A source String or null.
     * @return the given source String as an upper-case using the {@link Locale#ROOT} locale or null.
     */
    public static String toRootUpperCase(final String source) {
        return source == null ? null : source.toUpperCase(Locale.ROOT);
    }

    /**
     * Removes control characters (char &lt;= 32) from both ends of this String, handling {@code null} by returning {@code null}.
     *
     * <p>
     * The String is trimmed using {@link String#trim()}. Trim removes start and end characters &lt;= 32. To strip whitespace use {@link #strip(String)}.
     * </p>
     *
     * <p>
     * To trim your choice of characters, use the {@link #strip(String, String)} methods.
     * </p>
     *
     * <pre>
     * StringUtil.trim(null)          = null
     * StringUtil.trim("")            = ""
     * StringUtil.trim("     ")       = ""
     * StringUtil.trim("abc")         = "abc"
     * StringUtil.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str the String to be trimmed, may be null.
     * @return the trimmed string, {@code null} if null String input.
     */
    public static String trim(final String str) {
        return str == null ? null : str.trim();
    }

    /**
     * Removes control characters (char &lt;= 32) from both ends of this String returning an empty String ("") if the String is empty ("") after the trim or if
     * it is {@code null}.
     *
     * <p>
     * The String is trimmed using {@link String#trim()}. Trim removes start and end characters &lt;= 32. To strip whitespace use {@link #stripToEmpty(String)}.
     * </p>
     *
     * <pre>
     * StringUtil.trimToEmpty(null)          = ""
     * StringUtil.trimToEmpty("")            = ""
     * StringUtil.trimToEmpty("     ")       = ""
     * StringUtil.trimToEmpty("abc")         = "abc"
     * StringUtil.trimToEmpty("    abc    ") = "abc"
     * </pre>
     *
     * @param str the String to be trimmed, may be null.
     * @return the trimmed String, or an empty String if {@code null} input.
     */
    public static String trimToEmpty(final String str) {
        return str == null ? EMPTY : str.trim();
    }

    /**
     * Removes control characters (char &lt;= 32) from both ends of this String returning {@code null} if the String is empty ("") after the trim or if it is
     * {@code null}.
     *
     * <p>
     * The String is trimmed using {@link String#trim()}. Trim removes start and end characters &lt;= 32. To strip whitespace use {@link #stripToNull(String)}.
     * </p>
     *
     * <pre>
     * StringUtil.trimToNull(null)          = null
     * StringUtil.trimToNull("")            = null
     * StringUtil.trimToNull("     ")       = null
     * StringUtil.trimToNull("abc")         = "abc"
     * StringUtil.trimToNull("    abc    ") = "abc"
     * </pre>
     *
     * @param str the String to be trimmed, may be null.
     * @return the trimmed String, {@code null} if only chars &lt;= 32, empty or null String input.
     */
    public static String trimToNull(final String str) {
        final String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }

    /**
     * Truncates a String. This will turn "Now is the time for all good men" into "Now is the time for".
     *
     * <p>
     * Specifically:
     * </p>
     * <ul>
     * <li>If {@code str} is less than {@code maxWidth} characters long, return it.</li>
     * <li>Else truncate it to {@code substring(str, 0, maxWidth)}.</li>
     * <li>If {@code maxWidth} is less than {@code 0}, throw an {@link IllegalArgumentException}.</li>
     * <li>In no case will it return a String of length greater than {@code maxWidth}.</li>
     * </ul>
     *
     * <pre>
     * StringUtil.truncate(null, 0)       = null
     * StringUtil.truncate(null, 2)       = null
     * StringUtil.truncate("", 4)         = ""
     * StringUtil.truncate("abcdefg", 4)  = "abcd"
     * StringUtil.truncate("abcdefg", 6)  = "abcdef"
     * StringUtil.truncate("abcdefg", 7)  = "abcdefg"
     * StringUtil.truncate("abcdefg", 8)  = "abcdefg"
     * StringUtil.truncate("abcdefg", -1) = throws an IllegalArgumentException
     * </pre>
     *
     * @param str      the String to truncate, may be null.
     * @param maxWidth maximum length of result String, must be non-negative.
     * @return truncated String, {@code null} if null String input.
     * @throws IllegalArgumentException If {@code maxWidth} is less than {@code 0}.
     */
    public static String truncate(final String str, final int maxWidth) {
        return truncate(str, 0, maxWidth);
    }

    /**
     * Truncates a String. This will turn "Now is the time for all good men" into "is the time for all".
     *
     * <p>
     * Works like {@code truncate(String, int)}, but allows you to specify a "left edge" offset.
     * </p>
     *
     * <p>
     * Specifically:
     * </p>
     * <ul>
     * <li>If {@code str} is less than {@code maxWidth} characters long, return it.</li>
     * <li>Else truncate it to {@code substring(str, offset, maxWidth)}.</li>
     * <li>If {@code maxWidth} is less than {@code 0}, throw an {@link IllegalArgumentException}.</li>
     * <li>If {@code offset} is less than {@code 0}, throw an {@link IllegalArgumentException}.</li>
     * <li>In no case will it return a String of length greater than {@code maxWidth}.</li>
     * </ul>
     *
     * <pre>
     * StringUtil.truncate(null, 0, 0) = null
     * StringUtil.truncate(null, 2, 4) = null
     * StringUtil.truncate("", 0, 10) = ""
     * StringUtil.truncate("", 2, 10) = ""
     * StringUtil.truncate("abcdefghij", 0, 3) = "abc"
     * StringUtil.truncate("abcdefghij", 5, 6) = "fghij"
     * StringUtil.truncate("raspberry peach", 10, 15) = "peach"
     * StringUtil.truncate("abcdefghijklmno", 0, 10) = "abcdefghij"
     * StringUtil.truncate("abcdefghijklmno", -1, 10) = throws an IllegalArgumentException
     * StringUtil.truncate("abcdefghijklmno", Integer.MIN_VALUE, 10) = throws an IllegalArgumentException
     * StringUtil.truncate("abcdefghijklmno", Integer.MIN_VALUE, Integer.MAX_VALUE) = throws an IllegalArgumentException
     * StringUtil.truncate("abcdefghijklmno", 0, Integer.MAX_VALUE) = "abcdefghijklmno"
     * StringUtil.truncate("abcdefghijklmno", 1, 10) = "bcdefghijk"
     * StringUtil.truncate("abcdefghijklmno", 2, 10) = "cdefghijkl"
     * StringUtil.truncate("abcdefghijklmno", 3, 10) = "defghijklm"
     * StringUtil.truncate("abcdefghijklmno", 4, 10) = "efghijklmn"
     * StringUtil.truncate("abcdefghijklmno", 5, 10) = "fghijklmno"
     * StringUtil.truncate("abcdefghijklmno", 5, 5) = "fghij"
     * StringUtil.truncate("abcdefghijklmno", 5, 3) = "fgh"
     * StringUtil.truncate("abcdefghijklmno", 10, 3) = "klm"
     * StringUtil.truncate("abcdefghijklmno", 10, Integer.MAX_VALUE) = "klmno"
     * StringUtil.truncate("abcdefghijklmno", 13, 1) = "n"
     * StringUtil.truncate("abcdefghijklmno", 13, Integer.MAX_VALUE) = "no"
     * StringUtil.truncate("abcdefghijklmno", 14, 1) = "o"
     * StringUtil.truncate("abcdefghijklmno", 14, Integer.MAX_VALUE) = "o"
     * StringUtil.truncate("abcdefghijklmno", 15, 1) = ""
     * StringUtil.truncate("abcdefghijklmno", 15, Integer.MAX_VALUE) = ""
     * StringUtil.truncate("abcdefghijklmno", Integer.MAX_VALUE, Integer.MAX_VALUE) = ""
     * StringUtil.truncate("abcdefghij", 3, -1) = throws an IllegalArgumentException
     * StringUtil.truncate("abcdefghij", -2, 4) = throws an IllegalArgumentException
     * </pre>
     *
     * @param str      the String to truncate, may be null.
     * @param offset   left edge of source String.
     * @param maxWidth maximum length of result String, must be non-negative.
     * @return truncated String, {@code null} if null String input.
     * @throws IllegalArgumentException If {@code offset} or {@code maxWidth} is less than {@code 0}.
     */
    public static String truncate(final String str, final int offset, final int maxWidth) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset cannot be negative");
        }
        if (maxWidth < 0) {
            throw new IllegalArgumentException("maxWidth cannot be negative");
        }
        if (str == null) {
            return null;
        }
        final int len = str.length();
        final int start = Math.min(offset, len);
        final int end = offset > len - maxWidth ? len : offset + maxWidth;
        return str.substring(start, Math.min(end, len));
    }

    /**
     * Uncapitalizes a String, changing the first character to lower case as per {@link Character#toLowerCase(int)}. No other characters are changed.
     *
     * <p>
     * </p>
     *
     * <pre>
     * StringUtil.uncapitalize(null)  = null
     * StringUtil.uncapitalize("")    = ""
     * StringUtil.uncapitalize("cat") = "cat"
     * StringUtil.uncapitalize("Cat") = "cat"
     * StringUtil.uncapitalize("CAT") = "cAT"
     * </pre>
     *
     * @param str the String to uncapitalize, may be null.
     * @return the uncapitalized String, {@code null} if null String input.
     * @see #capitalize(String)
     */
    public static String uncapitalize(final String str) {
        final int strLen = length(str);
        if (strLen == 0) {
            return str;
        }
        final int firstCodePoint = str.codePointAt(0);
        final int newCodePoint = Character.toLowerCase(firstCodePoint);
        if (firstCodePoint == newCodePoint) {
            // already uncapitalized
            return str;
        }
        final int[] newCodePoints = str.codePoints().toArray();
        newCodePoints[0] = newCodePoint; // copy the first code point
        return new String(newCodePoints, 0, newCodePoints.length);
    }

    /**
     * Converts a String to upper case as per {@link String#toUpperCase()}.
     *
     * <p>
     * A {@code null} input String returns {@code null}.
     * </p>
     *
     * <pre>
     * StringUtil.upperCase(null)  = null
     * StringUtil.upperCase("")    = ""
     * StringUtil.upperCase("aBc") = "ABC"
     * </pre>
     *
     * <p>
     * <strong>Note:</strong> As described in the documentation for {@link String#toUpperCase()}, the result of this method is affected by the current locale.
     * For platform-independent case transformations, the method {@link #(String, Locale)} should be used with a specific locale (e.g.
     * {@link Locale#ENGLISH}).
     * </p>
     *
     * @param str the String to upper case, may be null.
     * @return the upper-cased String, {@code null} if null String input.
     */
    public static String upperCase(final String str) {
        if (str == null) {
            return null;
        }
        return str.toUpperCase();
    }

    /**
     * Returns the string representation of the {@code char} array or null.
     *
     * @param value the character array.
     * @return a String or null.
     * @see String#valueOf(char[])
     */
    public static String valueOf(final char[] value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Wraps a String with another String.
     *
     * <p>
     * A {@code null} input String returns {@code null}.
     * </p>
     *
     * <pre>
     * StringUtil.wrap(null, *)         = null
     * StringUtil.wrap("", *)           = ""
     * StringUtil.wrap("ab", null)      = "ab"
     * StringUtil.wrap("ab", "x")       = "xabx"
     * StringUtil.wrap("ab", "\"")      = "\"ab\""
     * StringUtil.wrap("\"ab\"", "\"")  = "\"\"ab\"\""
     * StringUtil.wrap("ab", "'")       = "'ab'"
     * StringUtil.wrap("'abcd'", "'")   = "''abcd''"
     * StringUtil.wrap("\"abcd\"", "'") = "'\"abcd\"'"
     * StringUtil.wrap("'abcd'", "\"")  = "\"'abcd'\""
     * </pre>
     *
     * @param str      the String to be wrapper, may be null.
     * @param wrapWith the String that will wrap str.
     * @return wrapped String, {@code null} if null String input.
     */
    public static String wrap(final String str, final String wrapWith) {
        if (isEmpty(str) || isEmpty(wrapWith)) {
            return str;
        }
        return wrapWith.concat(str).concat(wrapWith);
    }

    /**
     * Wraps a string with a string if that string is missing from the start or end of the given string.
     *
     * <p>
     * A new {@link String} will not be created if {@code str} is already wrapped.
     * </p>
     *
     * <pre>
     * StringUtil.wrapIfMissing(null, *)         = null
     * StringUtil.wrapIfMissing("", *)           = ""
     * StringUtil.wrapIfMissing("ab", null)      = "ab"
     * StringUtil.wrapIfMissing("ab", "x")       = "xabx"
     * StringUtil.wrapIfMissing("ab", "\"")      = "\"ab\""
     * StringUtil.wrapIfMissing("\"ab\"", "\"")  = "\"ab\""
     * StringUtil.wrapIfMissing("ab", "'")       = "'ab'"
     * StringUtil.wrapIfMissing("'abcd'", "'")   = "'abcd'"
     * StringUtil.wrapIfMissing("\"abcd\"", "'") = "'\"abcd\"'"
     * StringUtil.wrapIfMissing("'abcd'", "\"")  = "\"'abcd'\""
     * StringUtil.wrapIfMissing("/", "/")  = "/"
     * StringUtil.wrapIfMissing("a/b/c", "/")  = "/a/b/c/"
     * StringUtil.wrapIfMissing("/a/b/c", "/")  = "/a/b/c/"
     * StringUtil.wrapIfMissing("a/b/c/", "/")  = "/a/b/c/"
     * </pre>
     *
     * @param str      the string to be wrapped, may be {@code null}.
     * @param wrapWith the string that will wrap {@code str}.
     * @return the wrapped string, or {@code null} if {@code str == null}.
     */
    public static String wrapIfMissing(final String str, final String wrapWith) {
        if (isEmpty(str) || isEmpty(wrapWith)) {
            return str;
        }
        final boolean wrapStart = !str.startsWith(wrapWith);
        final boolean wrapEnd = !str.endsWith(wrapWith);
        if (!wrapStart && !wrapEnd) {
            return str;
        }
        final StringBuilder builder = new StringBuilder(str.length() + wrapWith.length() + wrapWith.length());
        if (wrapStart) {
            builder.append(wrapWith);
        }
        builder.append(str);
        if (wrapEnd) {
            builder.append(wrapWith);
        }
        return builder.toString();
    }
}