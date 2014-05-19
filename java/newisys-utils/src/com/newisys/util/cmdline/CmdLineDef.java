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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.newisys.io.IndentPrintWriter;

/**
 * Describes a command line specification.
 * 
 * @author Trevor Robinson
 */
public class CmdLineDef
{
    private final String programName;
    private final List<AbstractArgDef> argDefs;

    private final Map<String, OptionArgDef> optionDefNameMap;
    private final List<StringArgDef> stringArgDefs;
    private boolean gotOptionalStringArgDef;

    public CmdLineDef(String programName)
    {
        this.programName = programName;
        argDefs = new LinkedList<AbstractArgDef>();

        optionDefNameMap = new HashMap<String, OptionArgDef>();
        stringArgDefs = new LinkedList<StringArgDef>();
        gotOptionalStringArgDef = false;
    }

    public String getProgramName()
    {
        return programName;
    }

    public void addArgDef(AbstractArgDef argDef)
    {
        if (argDef instanceof StringArgDef)
        {
            if (argDef.isOptional())
            {
                gotOptionalStringArgDef = true;
            }
            else if (gotOptionalStringArgDef)
            {
                throw new IllegalStateException(
                    "Required string arguments must precede optional "
                        + "string arguments");
            }
            stringArgDefs.add((StringArgDef) argDef);
        }
        else if (argDef instanceof OptionArgDef)
        {
            final String name = argDef.getName();
            if (optionDefNameMap.containsKey(name))
            {
                throw new IllegalStateException("Duplicate option name: "
                    + name);
            }
            optionDefNameMap.put(name, (OptionArgDef) argDef);
        }
        argDefs.add(argDef);
    }

    public List<AbstractArgDef> getArgDefs()
    {
        return argDefs;
    }

    public void dumpUsage(PrintStream out)
    {
        IndentPrintWriter ipw = new IndentPrintWriter(out);
        dumpSyntax(ipw);
        dumpArgs(ipw);
    }

    private void dumpSyntax(IndentPrintWriter ipw)
    {
        ipw.print("Usage: ");
        ipw.print(programName);
        for (AbstractArgDef argDef : argDefs)
        {
            ipw.print(' ');
            ipw.print(argDef.getSyntaxString());
        }
        ipw.println();
    }

    private void dumpArgs(IndentPrintWriter ipw)
    {
        ipw.println("Arguments:");
        ipw.incIndent();
        for (AbstractArgDef argDef : argDefs)
        {
            argDef.dumpArgs(ipw);
        }
        ipw.decIndent();
    }

    public CmdLine processCmdLine(String[] args)
        throws ValidationException
    {
        final ArgIterator argIter = new ArgIterator(args);

        final CmdLine cmdLine = new CmdLine(this);

        final ArgList<AbstractArgDef, AbstractArg> cmdArgList = cmdLine
            .getArgList();
        final ParseContext pc = new TopLevelParseContext(cmdArgList,
            stringArgDefs, optionDefNameMap);
        while (argIter.hasNext())
        {
            pc.parseNext(argIter);
        }

        cmdLine.validate();

        return cmdLine;
    }

    private static class ParseContext
    {
        private final ArgList argList;
        private final int minStringArgCount;
        private final Iterator<StringArgDef> stringArgDefIter;
        private StringArgDef curStringArgDef = null;
        private int curStringArgCount = 0;
        private int totalStringArgCount = 0;

        public ParseContext(ArgList argList, List<StringArgDef> stringArgDefs)
        {
            this.argList = argList;
            minStringArgCount = getMinArgCount(stringArgDefs);
            stringArgDefIter = stringArgDefs.iterator();
        }

        private static int getMinArgCount(
            List< ? extends AbstractArgDef> argDefs)
        {
            int minArgs = 0;
            for (AbstractArgDef argDef : argDefs)
            {
                minArgs += argDef.getMinOccurs();
            }
            return minArgs;
        }

        public boolean gotMinArgs()
        {
            return totalStringArgCount >= minStringArgCount;
        }

        public void parseNext(ArgIterator argIter)
            throws ValidationException
        {
            final String s = argIter.next();

            if (!getNextArgDef())
            {
                throw new UnexpectedArgException(s);
            }

            final StringArg arg = new StringArg(curStringArgDef, s);
            addArg(curStringArgDef, arg);

            if (++curStringArgCount == curStringArgDef.getMaxOccurs())
            {
                curStringArgDef = null;
            }
            ++totalStringArgCount;
        }

        private boolean getNextArgDef()
        {
            if (curStringArgDef == null && stringArgDefIter.hasNext())
            {
                curStringArgDef = stringArgDefIter.next();
                curStringArgCount = 0;
            }
            return curStringArgDef != null;
        }

        protected void addArg(AbstractArgDef argDef, AbstractArg arg)
        {
            argList.addArg(argDef, arg);
        }
    }

    private static class TopLevelParseContext
        extends ParseContext
    {
        private final Map<String, OptionArgDef> optionDefNameMap;

        public TopLevelParseContext(
            ArgList<AbstractArgDef, AbstractArg> argList,
            List<StringArgDef> stringArgDefs,
            Map<String, OptionArgDef> optionDefNameMap)
        {
            super(argList, stringArgDefs);
            this.optionDefNameMap = optionDefNameMap;
        }

        public void parseNext(ArgIterator argIter)
            throws ValidationException
        {
            final String s = argIter.next();
            if (s.startsWith("-"))
            {
                final OptionArgDef optionDef = findOptionDef(s);
                if (optionDef == null)
                {
                    throw new UnknownOptionException(s);
                }

                final OptionArg arg = new OptionArg(optionDef);

                final ArgList<StringArgDef, StringArg> optArgList = arg
                    .getArgList();
                final List<StringArgDef> optArgDefs = optionDef.getArgDefs();
                final ParseContext pc = new ParseContext(optArgList, optArgDefs);
                while (argIter.hasNext())
                {
                    if (pc.gotMinArgs())
                    {
                        final String peek = argIter.next();
                        argIter.backUp();
                        if (peek.startsWith("-"))
                        {
                            break;
                        }
                    }

                    pc.parseNext(argIter);
                }

                addArg(optionDef, arg);
            }
            else
            {
                argIter.backUp();
                super.parseNext(argIter);
            }
        }

        private OptionArgDef findOptionDef(String arg)
        {
            return optionDefNameMap.get(arg.substring(1));
        }
    }
}
