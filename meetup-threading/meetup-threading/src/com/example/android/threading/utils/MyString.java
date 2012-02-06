package com.example.android.threading.utils;

import java.util.Vector;

public class MyString
{
    /**
     * Tests if a String value is null or empty.
     * 
     * @param value
     *            the String value to test
     * @return true if the String is null, zero length, or ""
     */
    public static boolean isNullOrEmpty(String value)
    {
        return (value == null || value.length() == 0 || value == "");
    }

    public static String getClassName(Object o)
    {
        return getClassName((o == null) ? null : o.getClass());
    }

    public static String getClassName(Class c)
    {
        if (c == null)
        {
            return "null";
        }

        return c.getName();
    }

    public static String getShortClassName(Object o)
    {
        return getShortClassName((o == null) ? null : o.getClass());
    }

    public static String getShortClassName(Class c)
    {
        if (c == null)
        {
            return "null";
        }

        String name = c.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static String formatNumber(long number, int minimumLength)
    {
        String s = String.valueOf(number);
        while (s.length() < minimumLength)
        {
            s = "0" + s;
        }
        return s;
    }

    public static String formatNumber(double number, int leading, int trailing)
    {
        if (number == Double.NaN || number == Double.NEGATIVE_INFINITY || number == Double.POSITIVE_INFINITY)
        {
            return String.valueOf(number);
        }

        // String.valueOf(1) is guranteed to at least be of the form "1.0"
        String[] parts = split(String.valueOf(number), ".", 0);
        while (parts[0].length() < leading)
        {
            parts[0] = '0' + parts[0];
        }
        while (parts[1].length() < trailing)
        {
            parts[1] = parts[1] + '0';
        }
        parts[1] = parts[1].substring(0, trailing);
        return parts[0] + '.' + parts[1];
    }

    /**
     * Returns a string array that contains the substrings in a source string that are delimited by a specified string. 
     * @param source String to split with the given delimiter.
     * @param separator String that delimits the substrings in the source string.
     * @param limit Determines the maximum number of entries in the resulting array, and the treatment of trailing empty strings.
     * <ul>
     * <li>For n > 0, the resulting array contains at most n entries. If this is fewer than the number of matches, the final entry will contain all remaining input.</li>
     * <li>For n < 0, the length of the resulting array is exactly the number of occurrences of the Pattern plus one for the text after the final separator. All entries are included.</li>
     * <li>For n == 0, the result is as for n < 0, except trailing empty strings will not be returned. (Note that the case where the input is itself an empty string is special, as described above, and the limit parameter does not apply there.)</li>
     * </ul>
     * @return An array whose elements contain the substrings in a source string that are delimited by a separator string.
     */
    public static String[] split(String source, String separator, int limit)
    {
        if (isNullOrEmpty(source) || isNullOrEmpty(separator))
        {
            return new String[]
            {
                source
            };
        }

        int indexB = source.indexOf(separator);
        if (indexB == -1)
        {
            return new String[]
            {
                source
            };
        }

        int indexA = 0;
        String value;
        Vector values = new Vector();

        while (indexB != -1 && (limit < 1 || values.size() < (limit - 1)))
        {
            value = new String(source.substring(indexA, indexB));
            if (!isNullOrEmpty(value) || limit < 0)
            {
                values.addElement(value);
            }
            indexA = indexB + separator.length();
            indexB = source.indexOf(separator, indexA);
        }

        indexB = source.length();
        value = new String(source.substring(indexA, indexB));
        if (!isNullOrEmpty(value) || limit < 0)
        {
            values.addElement(value);
        }

        String[] result = new String[values.size()];
        values.copyInto(result);
        return result;
    }
}
