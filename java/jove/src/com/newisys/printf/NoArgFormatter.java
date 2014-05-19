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
 * Class used to format conversion specifiers that take no argument.
 * 
 * @author Jon Nall
 */
final class NoArgFormatter
    implements ConversionFormatter
{
    /**
     * Sole constructor.
     */
    public NoArgFormatter()
    {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public int getMaximumLength(PrintfSpec spec)
    {
        // the only supported conversion spec is "%%" which returns "%"
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    public boolean consumesArg(PrintfSpec spec)
    {
        // by definition this always returns false for NoArgFormatter
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void format(PrintfSpec spec, StringBuilder buf)
    {
        checkFlagsWidthAndPrecision(spec);

        if (spec.conversionSpec != '%')
        {
            throw new InvalidFormatSpecException(
                "Unsupported no-arg conversion specifier: %"
                    + spec.conversionSpec);
        }
        buf.append('%');
    }

    /**
     * Checks that no flags were present on the conversion specifier and no
     * width or precision was specicifed.
     *
     * @param spec the PrintfSpec to analyze
     * @throws InvalidFormatSpecException if any flags, width or precision
     *      are contained in <code>spec</code>
     */
    protected final void checkFlagsWidthAndPrecision(PrintfSpec spec)
    {
        for (final PrintfFlag f : spec.flags)
        {

            throw new InvalidFormatSpecException("Flag [" + f
                + "] is unsupported in no-arg conversions");
        }

        if (spec.widthIsValid)
        {
            throw new InvalidFormatSpecException(
                "Width is unsupported for no-arg conversions");
        }
        if (spec.precisionIsValid)
        {
            throw new InvalidFormatSpecException(
                "Precision is unsupported for no-arg conversions");
        }
    }
}
