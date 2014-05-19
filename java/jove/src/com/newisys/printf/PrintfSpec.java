/*
 * Jove - The Open Verification Environment for the Java (TM) Platform
 * Copyright (C) 2005 Newisys, Inc. or its licensors, as applicable.
 * Java is a registered trademark of Sun Microsystems, Inc. in the U.S. or
 * other countries.
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

package com.newisys.printf;

import java.util.EnumSet;

/**
 * Class representing a printf specification.
 * 
 * @author Jon Nall
 */
final class PrintfSpec
{
    /**
     * The conversion specifier for this specification.
     */
    public final char conversionSpec;

    /**
     * The set of flags that have been set for this specification.
     */
    public final EnumSet<PrintfFlag> flags;

    /**
     * The width of this specification. This value is only valid if
     * <code>widthIsValid</code> is <code>true</code>.
     */
    public final int width;

    /**
     * Determines if <code>width</code> is valid.
     */
    public final boolean widthIsValid;

    /**
     * The precision of this specification. This value is only valid if
     * <code>precisionIsValid</code> is <code>true</code>.
     */
    public final int precision;

    /**
     * Determines if <code>precision</code> is valid.
     */
    public final boolean precisionIsValid;

    /**
     * The object to be formatted. This may be <code>null</code>, for example
     * in no-argument format specifications.
     */
    public final Object obj;

    /**
     * The starting index of this printf specification in the original format
     * string. For example, in "01%4s56", the startIdx would be 2 since index
     * 2 is where the printf specification "%4s" starts.
     */
    public final int startIdx;

    /**
     * The ending index of this printf specification in the original format
     * string. For example, in "01%4s56", the startIdx would be 4 since index
     * 4 is where the printf specification "%4s" ends.
     */
    public final int endIdx;

    /**
     * The cached string, which may be <code>null</code>. If not
     * <code>null</code>, this value holds the formatted string.
     *
     * Occasionally, when determining the length of a formatted string, we may
     * have to do all of the work of creating that string (e.g. printing an
     * unsupported object with %s -- in this case, we have to call toString()
     * on the object to get its length). in this case we should just cache the
     * string to avoid the cost of determining it twice.
     * <P>
     * However, the contents of this field are not strictly specified and
     * {@link ConversionFormatter} objects may use it as needed to cache values
     * that are expensive to calculate.
     */
    public String cachedString = null;

    /**
     * The cached length, which may be <code>-1</code>. Similarly to
     * {@link #cachedString}, the exact length required for the formatted
     * string may be calculated early on, in which case it is cached here.
     * <P>
     * However, the contents of this field are not strictly specified and
     * {@link ConversionFormatter} objects may use it as needed to cache values
     * that are expensive to calculate.
     */
    public int cachedLength = -1;

    /**
     * Sole constructor.
     *
     * @param startIdx the starting index of this specifier string in the
     *      original format string
     * @param endIdx the ending index of this specifier string in the
     *      original format string
     * @param conversionSpec the conversion specifier
     * @param flags the flags of this PrintfSpec
     * @param width the width of this PrintfSpec
     * @param widthIsValid <code>true</code> if <code>width</code> is
     *      value, <code>false</code> otherwise
     * @param precision the precision of this PrintfSpec
     * @param precisionIsValid <code>true</code> if <code>precision</code> is
     *      value, <code>false</code> otherwise
     * @param obj the object to format
     */
    public PrintfSpec(
        final int startIdx,
        final int endIdx,
        final char conversionSpec,
        final EnumSet<PrintfFlag> flags,
        final int width,
        final boolean widthIsValid,
        final int precision,
        final boolean precisionIsValid,
        final Object obj)
    {
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.conversionSpec = conversionSpec;
        this.flags = EnumSet.copyOf(flags);
        this.width = width;
        this.widthIsValid = widthIsValid;
        this.precision = precision;
        this.precisionIsValid = precisionIsValid;
        this.obj = obj;
    }
}
