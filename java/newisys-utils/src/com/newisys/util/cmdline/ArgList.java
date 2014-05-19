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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A list of command line option arguments.
 * 
 * @param <D> the argument specification type
 * @param <A> the argument type
 * @author Trevor Robinson
 */
public class ArgList<D extends AbstractArgDef, A extends AbstractArg>
{
    private final List<A> argList;
    private final Map<D, List<A>> argDefMap;

    public ArgList()
    {
        argList = new LinkedList<A>();
        argDefMap = new HashMap<D, List<A>>();
    }

    public void addArg(D argDef, A arg)
    {
        argList.add(arg);

        List<A> args = argDefMap.get(argDef);
        if (args == null)
        {
            args = new LinkedList<A>();
            argDefMap.put(argDef, args);
        }
        args.add(arg);
    }

    public List<A> getArgs()
    {
        return argList;
    }

    public List<A> getArgsForDef(D argDef)
    {
        return argDefMap.get(argDef);
    }

    public void validate(List< ? extends D> argDefs)
        throws ValidationException
    {
        final MultiValidationException me = new MultiValidationException();

        // validate each argument individually
        int index = 1;
        for (A arg : argList)
        {
            try
            {
                arg.validate();
            }
            catch (ValidationException e)
            {
                // wrap exception with context;
                // number arguments starting at 1
                me
                    .addException(new ValidationContextException(
                        "Error in argument " + index + " (" + arg.getArgDef()
                            + ")", e));
            }
            ++index;
        }

        // validate argument counts
        for (D argDef : argDefs)
        {
            final List args = getArgsForDef(argDef);
            if (!argDef.isOptional())
            {
                if (args == null || args.size() < argDef.getMinOccurs())
                {
                    ValidationException e = new MinOccurException(argDef);
                    me.addException(e);
                }
            }
            if (!argDef.isUnbounded())
            {
                if (args != null && args.size() > argDef.getMaxOccurs())
                {
                    ValidationException e = new MaxOccurException(argDef);
                    me.addException(e);
                }
            }
        }

        me.checkThrow();
    }
}
