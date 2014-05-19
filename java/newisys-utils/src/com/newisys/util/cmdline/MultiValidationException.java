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

import java.util.LinkedList;
import java.util.List;

/**
 * Describes the condition where multiple ValidationExceptions occurred while 
 * validating a command line.
 * 
 * @author Trevor Robinson
 */
public class MultiValidationException
    extends ValidationException
{
    private final List<ValidationException> exceptions;

    public MultiValidationException()
    {
        super();
        exceptions = new LinkedList<ValidationException>();
    }

    public void addException(ValidationException e)
    {
        exceptions.add(e);
    }

    public List getExceptions()
    {
        return exceptions;
    }

    public String getMessage()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(exceptions.size());
        buf.append(" validation errors");
        for (ValidationException e : exceptions)
        {
            buf.append("\n  ");
            buf.append(e.getMessage());
        }
        return buf.toString();
    }

    public void checkThrow()
        throws ValidationException
    {
        switch (exceptions.size())
        {
        case 0:
            break;
        case 1:
            throw exceptions.get(0);
        default:
            throw this;
        }
    }
}
