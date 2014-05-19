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
 * Describes a command line option string argument specification.
 * 
 * @author Trevor Robinson
 */
public class StringArgDef
    extends AbstractArgDef
{
    protected String defValue;

    public StringArgDef(String name)
    {
        this(name, null, 1, 1);
    }

    public StringArgDef(String name, String description)
    {
        this(name, description, 1, 1);
    }

    public StringArgDef(
        String name,
        String description,
        int minOccurs,
        int maxOccurs)
    {
        super(name, description, minOccurs, maxOccurs);
    }

    public String getDefValue()
    {
        return defValue;
    }

    public void setDefValue(String defValue)
    {
        try
        {
            validateValue(defValue);
            this.defValue = defValue;
        }
        catch (ValidationException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void validateValue(String value)
        throws ValidationException
    {
        // do nothing by default
    }
}
