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

import java.util.List;

/**
 * Represents a user-supplied command-line option.
 * 
 * @author Trevor Robinson
 */
public class OptionArg
    extends AbstractArg<OptionArgDef>
{
    private final ArgList<StringArgDef, StringArg> argList;

    public OptionArg(OptionArgDef argDef)
    {
        super(argDef);
        argList = new ArgList<StringArgDef, StringArg>();
    }

    public ArgList<StringArgDef, StringArg> getArgList()
    {
        return argList;
    }

    public List<StringArg> getArgsForDef(StringArgDef argDef)
    {
        return argList.getArgsForDef(argDef);
    }

    public String getArgValue(StringArgDef argDef)
    {
        int maxOccurs = argDef.getMaxOccurs();
        if (maxOccurs > 1 || maxOccurs == AbstractArgDef.UNBOUNDED)
        {
            throw new IllegalStateException("Argument may occur multiple times");
        }

        List<StringArg> argsForDef = argList.getArgsForDef(argDef);
        if (argsForDef != null && !argsForDef.isEmpty())
        {
            StringArg strArg = argsForDef.get(0);
            return strArg.getValue();
        }
        return argDef.getDefValue();
    }

    public void validate()
        throws ValidationException
    {
        argList.validate(argDef.getArgDefs());
    }
}
