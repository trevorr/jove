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
 * Class providing access to the options and arguments of a given CmdLineDef.
 *
 * @author Trevor Robinson
 */
public class CmdLine
{
    private final CmdLineDef cmdLineDef;
    private final ArgList<AbstractArgDef, AbstractArg> argList;

    public CmdLine(CmdLineDef cmdLineDef)
    {
        this.cmdLineDef = cmdLineDef;
        argList = new ArgList<AbstractArgDef, AbstractArg>();
    }

    public CmdLineDef getCmdLineDef()
    {
        return cmdLineDef;
    }

    public ArgList<AbstractArgDef, AbstractArg> getArgList()
    {
        return argList;
    }

    public List<AbstractArg> getArgsForDef(AbstractArgDef argDef)
    {
        return argList.getArgsForDef(argDef);
    }

    public String getOptionArgValue(OptionArgDef optDef, StringArgDef argDef)
    {
        int maxOccurs = optDef.getMaxOccurs();
        if (maxOccurs > 1 || maxOccurs == AbstractArgDef.UNBOUNDED)
        {
            throw new IllegalStateException("Option may occur multiple times");
        }

        List optsForDef = argList.getArgsForDef(optDef);
        if (optsForDef != null && !optsForDef.isEmpty())
        {
            OptionArg optArg = (OptionArg) optsForDef.get(0);
            return optArg.getArgValue(argDef);
        }
        return argDef.getDefValue();
    }

    public void validate()
        throws ValidationException
    {
        argList.validate(cmdLineDef.getArgDefs());
    }
}
