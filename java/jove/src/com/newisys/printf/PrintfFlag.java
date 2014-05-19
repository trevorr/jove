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
 * Enumeration of the valid flags that can be present for printf specifications.
 * 
 * @author Jon Nall
 */
public enum PrintfFlag
{
    /**
     * Left justify the string. Specified by '-'.
     */
    LEFT_JUSTIFY,

    /**
     * For positive signed values, prefix the value with a "+". Specified by
     * '+'.
     */
    PRINT_SIGN,

    /**
     * For positive signed values, prefix the value with a " ". Specified by
     * ' '.
     */
    SPACE_BEFORE_POSITIVE_VALUE,

    /**
     * Print the alternate form for the value. Specified by '#'.
     * @see Printf
     */
    ALTERNATE_FORM,

    /**
     * Format the value using only uppercase letters. Specified by '^' or a
     * capital conversion specifier.
     */
    UPPERCASE,

    /**
     * Pad extra width with zeros, rather than spaces. Specified by '0'.
     */
    ZERO_PAD,

    /**
     * Use the natural width of the type. Specified by '='.
     */
    USE_NATURAL_WIDTH;
}
