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

/**
 * Interface to be used by printf conversion formatters in order to get
 * information about the formatted string. Classes implementing this interface
 * are guaranteed that each method will be called exactly once and in the
 * following order:<br>
 * <ul>
 * <li>{@link #getMaximumLength(PrintfSpec)}</li>
 * <li>{@link #consumesArg(PrintfSpec)}</li>
 * <li>{@link #format(PrintfSpec, StringBuilder)}</li>
 * </ul>
 * 
 * @author Jon Nall
 */
public interface ConversionFormatter
{
    /**
     * Returns the maximum length of the string produced by the given
     * PrintfSpec when formatted. As its name suggests, this need not be exact,
     * but it should err on the side of returning a value larger than what will
     * actually be needed.
     * <P>
     * Note that an exact copy of <code>spec</code> will be passed to both
     * {@link #consumesArg} and {@link #format}.
     *
     * @param spec the PrintfSpec to be formatted
     * @return a number greater than or equal to the number of characters
     *      required to format <code>spec</code>,
     */
    public int getMaximumLength(PrintfSpec spec);

    /**
     * Returns true if the formatting of the given PrintfSpec will consume an
     * argument.
     * <P>
     * Note that <code>spec</code> will be the exact PrintfSpec passed to
     * {@link #getMaximumLength}.
     *
     * @param spec the PrintfSpec to be formatted
     * @return <code>true</code> if formatting the PrintfSpec will consume an
     *      argument, <code>false</code> otherwise
     */
    public boolean consumesArg(PrintfSpec spec);

    /**
     * Formats the given PrintfSpec, appending it to the specified StringBuilder.
     * <P>
     * Note that <code>spec</code> will be the exact PrintfSpec passed to
     * {@link #getMaximumLength} and {@link #consumesArg}.
     *
     * @param spec the PrintfSpec to format
     * @param buf the StringBuilder to which the formatted string should be
     *      appended
     */
    public void format(PrintfSpec spec, StringBuilder buf);
}
