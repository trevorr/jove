/*
 * Newisys-Utils - Newisys Utility Classes
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
 *
 * Licensed under the Open Software License version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You should
 * have received a copy of the License along with this software; if not, you
 * may obtain a copy of the License at
 *
 * http://opensource.org/licenses/osl-2.0.php
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.newisys.util.text;

import java.util.Collection;
import java.util.Iterator;

import com.newisys.util.ArrayIterator;

/**
 * A collection of static utility methods for working with text.
 * 
 * @author Trevor Robinson
 */
public final class TextUtil
{
    private TextUtil()
    {
        // prevent instantiation
    }

    /**
     * Pads the given string to the given length using the given character. If
     * leading is true, the padding is inserted at the start of the string;
     * otherwise it is added to the end. If the string is already greater than
     * or equal to the given length, the original string is returned.
     *
     * @param s the String to pad
     * @param length the length of the padded string
     * @param c the padding character
     * @param leading whether to insert padding a start of string
     * @return s padded to the given length with c
     */
    public static String pad(String s, int length, char c, boolean leading)
    {
        if (s.length() >= length)
        {
            return s;
        }
        else
        {
            StringBuffer buf = new StringBuffer(length);
            if (!leading)
            {
                buf.append(s);
            }
            for (int i = s.length(); i < length; ++i)
            {
                buf.append(c);
            }
            if (leading)
            {
                buf.append(s);
            }
            return buf.toString();
        }
    }

    /**
     * Pads the given string to the given length using the given character
     * inserted at the start of the string. If the string is already greater
     * than or equal to the given length, the original string is returned.
     *
     * @param s the String to pad
     * @param length the length of the padded string
     * @param c the padding character
     * @return s padded to the given length with leading c's
     */
    public static String padLeading(String s, int length, char c)
    {
        return pad(s, length, c, true);
    }

    /**
     * Pads the given string to the given length by inserting spaces at the
     * start of the string. If the string is already greater than or equal to
     * the given length, the original string is returned.
     *
     * @param s the String to pad
     * @param length the length of the padded string
     * @return s padded to the given length with leading spaces
     */
    public static String padLeading(String s, int length)
    {
        return pad(s, length, ' ', true);
    }

    /**
     * Pads the given string to the given length using the given character
     * added to the end of the string. If the string is already greater than or
     * equal to the given length, the original string is returned.
     *
     * @param s the String to pad
     * @param length the length of the padded string
     * @param c the padding character
     * @return s padded to the given length with trailing c's
     */
    public static String padTrailing(String s, int length, char c)
    {
        return pad(s, length, c, false);
    }

    /**
     * Pads the given string to the given length by adding spaces to the end of
     * the string. If the string is already greater than or equal to the given
     * length, the original string is returned.
     *
     * @param s the String to pad
     * @param length the length of the padded string
     * @return s padded to the given length with trailing spaces
     */
    public static String padTrailing(String s, int length)
    {
        return pad(s, length, ' ', false);
    }

    /**
     * Returns a String containing the given character replicated the given
     * number of times.
     *
     * @param c a character
     * @param count number of times to replicate the character
     * @return a String containing count copies of c
     */
    public static String replicate(char c, int count)
    {
        StringBuffer buf = new StringBuffer(count);
        for (int i = 0; i < count; ++i)
        {
            buf.append(c);
        }
        return buf.toString();
    }

    /**
     * Returns a string representation of the objects in the given iterator,
     * using the given leading, trailing, and separator strings.
     *
     * @param iter an Iterator
     * @param leading string to append at start of output
     * @param trailing string to append at end of output
     * @param separator string to append between each object
     * @return a String
     */
    public static String toString(
        Iterator iter,
        String leading,
        String trailing,
        String separator)
    {
        StringBuffer buf = new StringBuffer();
        if (leading != null)
        {
            buf.append(leading);
        }
        while (iter.hasNext())
        {
            buf.append(iter.next().toString());
            if (separator != null && iter.hasNext())
            {
                buf.append(separator);
            }
        }
        if (trailing != null)
        {
            buf.append(trailing);
        }
        return buf.toString();
    }

    /**
     * Returns a string representation of the objects in the given iterator,
     * using the default leading ('['), trailing (']'), and separator (',')
     * strings.
     *
     * @param iter an Iterator
     * @return a String
     */
    public static String toString(Iterator iter)
    {
        return toString(iter, "[", "]", ",");
    }

    /**
     * Returns a string representation of the objects in the given collection.
     *
     * @param coll a Collection
     * @return a String
     */
    public static String toString(Collection coll)
    {
        return toString(coll.iterator());
    }

    /**
     * Returns a string representation of the objects in the given array.
     *
     * @param arr an array of Objects
     * @return a String
     */
    public static String toString(Object[] arr)
    {
        return toString(new ArrayIterator(arr));
    }
}
