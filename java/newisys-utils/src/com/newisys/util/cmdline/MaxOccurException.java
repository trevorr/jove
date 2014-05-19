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

package com.newisys.util.cmdline;

/**
 * Describes condition where an argument is specified on the command line more
 * times than allowed.
 *
 * @author Trevor Robinson
 */
public class MaxOccurException
    extends ArgDefValidationException
{
    public MaxOccurException(AbstractArgDef argDef)
    {
        super(argDef, formatMessage(argDef));
    }

    private static String formatMessage(AbstractArgDef argDef)
    {
        return "At most " + argDef.getMaxOccurs() + " occurrences of " + argDef
            + " are allowed";
    }
}
