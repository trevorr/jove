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

package com.newisys.dv.ifgen;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.newisys.util.cmdline.AbstractArgDef;
import com.newisys.util.cmdline.CmdLine;
import com.newisys.util.cmdline.CmdLineDef;
import com.newisys.util.cmdline.OptionArg;
import com.newisys.util.cmdline.OptionArgDef;
import com.newisys.util.cmdline.StringArg;
import com.newisys.util.cmdline.StringArgDef;
import com.newisys.util.cmdline.ValidationException;

/**
 * Command-line driver for the Jove interface generator.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public final class IfgenMain
{
    private static class MyCmdLineDef
        extends CmdLineDef
    {
        public final OptionArgDef helpOpt;

        public final OptionArgDef srcRootOpt;
        public final StringArgDef srcRootArg;

        public final OptionArgDef tstampOpt;
        public final StringArgDef tstampArg;

        public final OptionArgDef fileListOpt;
        public final StringArgDef fileListArg;

        public final OptionArgDef forceDefaultClockOpt;

        public final OptionArgDef fileOpt;
        public final StringArgDef fileArg;

        public final OptionArgDef dirOpt;
        public final StringArgDef dirArg;

        public final OptionArgDef genshellsOpt;

        public final OptionArgDef shellOpt;
        public final StringArgDef testbenchArg;
        public final StringArgDef paramsArg;
        public final StringArgDef shellnameArg;

        public MyCmdLineDef()
        {
            super("Ifgen");

            helpOpt = new OptionArgDef("help",
                "Display usage information and exit", 0, 1);
            addArgDef(helpOpt);

            srcRootOpt = new OptionArgDef("srcroot",
                "Root directory for generated source files", 0, 1);
            srcRootArg = new StringArgDef("path");
            srcRootOpt.addArgDef(srcRootArg);
            addArgDef(srcRootOpt);

            tstampOpt = new OptionArgDef("tstamp",
                "Timestamp file used to regenerate only when necessary", 0, 1);
            tstampArg = new StringArgDef("path");
            tstampOpt.addArgDef(tstampArg);
            addArgDef(tstampOpt);

            fileListOpt = new OptionArgDef("filelist",
                "Output list of generated files to the given file", 0, 1);
            fileListArg = new StringArgDef("path");
            fileListOpt.addArgDef(fileListArg);
            addArgDef(fileListOpt);

            forceDefaultClockOpt = new OptionArgDef(
                "forcedefaultclock",
                "Force inclusion of DefaultClock port on Verilog shell modules",
                0, 1);
            addArgDef(forceDefaultClockOpt);

            fileOpt = new OptionArgDef("file", "Interface source file", 0,
                AbstractArgDef.UNBOUNDED);
            fileArg = new StringArgDef("path", null, 1,
                AbstractArgDef.UNBOUNDED);
            fileOpt.addArgDef(fileArg);
            addArgDef(fileOpt);

            dirOpt = new OptionArgDef("dir",
                "Directory to scan for .if source files", 0,
                AbstractArgDef.UNBOUNDED);
            dirArg = new StringArgDef("path", null, 1, AbstractArgDef.UNBOUNDED);
            dirOpt.addArgDef(dirArg);
            addArgDef(dirOpt);

            genshellsOpt = new OptionArgDef("genshells",
                "Generate shells for non-parameterized testbenches", 0, 1);
            addArgDef(genshellsOpt);

            shellOpt = new OptionArgDef("shell",
                "Generate a shell for the specified testbench", 0,
                AbstractArgDef.UNBOUNDED);
            testbenchArg = new StringArgDef(
                "testbench",
                "The qualified name of the ifgen testbench block to generate a shell for",
                1, 1);
            shellOpt.addArgDef(testbenchArg);
            paramsArg = new StringArgDef(
                "params",
                "Testbench parameters in the form \"<param1=val1, param2=val2, ...>\"",
                1, 1);
            shellOpt.addArgDef(paramsArg);
            shellnameArg = new StringArgDef("shellname",
                "The name of the generated shell file", 0, 1);
            shellOpt.addArgDef(shellnameArg);
            addArgDef(shellOpt);

        }
    }

    private static final MyCmdLineDef cmdLineDef = new MyCmdLineDef();

    public static void main(String[] args)
    {
        try
        {
            CmdLine cmdLine = cmdLineDef.processCmdLine(args);

            if (cmdLine.getArgsForDef(cmdLineDef.helpOpt) != null)
            {
                cmdLineDef.dumpUsage(System.out);
                System.exit(0);
            }

            String srcRootPath = cmdLine.getOptionArgValue(
                cmdLineDef.srcRootOpt, cmdLineDef.srcRootArg);
            if (srcRootPath == null)
            {
                srcRootPath = System.getProperty("user.dir");
            }
            File srcRootDir = new File(srcRootPath);

            List fileOpts = cmdLine.getArgsForDef(cmdLineDef.fileOpt);
            List dirOpts = cmdLine.getArgsForDef(cmdLineDef.dirOpt);
            if (fileOpts == null && dirOpts == null)
            {
                throw new ValidationException(
                    "At least one -file or -dir option is required");
            }

            Ifgen ifgen = new Ifgen(srcRootDir);
            String tstampPath = cmdLine.getOptionArgValue(cmdLineDef.tstampOpt,
                cmdLineDef.tstampArg);
            if (tstampPath != null)
            {
                ifgen.setTimestampFile(new File(tstampPath));
            }
            String fileListPath = cmdLine.getOptionArgValue(
                cmdLineDef.fileListOpt, cmdLineDef.fileListArg);
            if (fileListPath != null)
            {
                ifgen.setListFile(new File(fileListPath));
            }
            if (cmdLine.getArgsForDef(cmdLineDef.forceDefaultClockOpt) != null)
            {
                ifgen.setForceDefaultClockPort(true);
            }

            if (fileOpts != null)
            {
                for (Object opt : fileOpts)
                {
                    OptionArg fileOpt = (OptionArg) opt;
                    List<StringArg> fileArgs = fileOpt
                        .getArgsForDef(cmdLineDef.fileArg);
                    for (StringArg fileArg : fileArgs)
                    {
                        String filePath = fileArg.getValue();
                        File file = new File(filePath);
                        ifgen.addSrcFile(file);
                    }
                }
            }
            if (dirOpts != null)
            {
                for (Object opt : dirOpts)
                {
                    OptionArg dirOpt = (OptionArg) opt;
                    List<StringArg> dirArgs = dirOpt
                        .getArgsForDef(cmdLineDef.dirArg);
                    for (StringArg dirArg : dirArgs)
                    {
                        String dirPath = dirArg.getValue();
                        File dir = new File(dirPath);
                        findFiles(dir, ".if", ifgen.getSrcFiles());
                    }
                }
            }

            if (cmdLine.getArgsForDef(cmdLineDef.genshellsOpt) != null)
            {
                ifgen.setGenshell(true);
            }

            List shells = cmdLine.getArgsForDef(cmdLineDef.shellOpt);
            if (shells != null)
            {
                for (Object opt : shells)
                {
                    OptionArg shell = (OptionArg) opt;
                    String testbench = shell
                        .getArgValue(cmdLineDef.testbenchArg);
                    String params = shell.getArgValue(cmdLineDef.paramsArg)
                        .trim();
                    String shellname = shell
                        .getArgValue(cmdLineDef.shellnameArg);

                    // Create a map from the parameters
                    Map<String, String> paramMap = new HashMap<String, String>();
                    System.out.println("params: " + params);
                    if (params.length() > 0)
                    {
                        if (!params.startsWith("<"))
                        {
                            throw new ValidationException(
                                "Invalid parameter string. Missing leading '<': "
                                    + params);
                        }
                        else if (!params.endsWith(">"))
                        {
                            throw new ValidationException(
                                "Invalid parameter string. Missing trailing '>': "
                                    + params);
                        }
                        params = params.substring(1, params.length() - 1);
                        String[] pvPairs = params.split(",");
                        for (String pair : pvPairs)
                        {
                            String[] pv = pair.split("=");
                            if (pv.length != 2)
                            {
                                throw new ValidationException(
                                    "Invalid parameter/value pair: " + pair);
                            }
                            paramMap.put(pv[0].trim(), pv[1].trim());
                        }
                    }

                    ifgen.addShell(testbench, paramMap, shellname);
                }
            }

            ifgen.execute();

            // exit with success
            System.exit(0);
        }
        catch (ValidationException e)
        {
            System.err.println(e.getMessage());
            cmdLineDef.dumpUsage(System.err);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // exit with error
        System.exit(1);
    }

    static void findFiles(
        final File dir,
        final String ext,
        final Collection<File> files)
        throws IOException
    {
        final File[] fileArray;
        try
        {
            fileArray = dir.listFiles(new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    if (pathname.isDirectory())
                    {
                        try
                        {
                            findFiles(pathname, ext, files);
                        }
                        catch (IOException e)
                        {
                            throw new TunnelledIOException(e);
                        }
                        return false;
                    }
                    else
                    {
                        return pathname.getName().endsWith(ext);
                    }
                }
            });
        }
        catch (TunnelledIOException e)
        {
            throw e.getCause();
        }
        if (fileArray == null)
        {
            throw new IOException("Unable to read directory: " + dir);
        }
        for (File file : fileArray)
        {
            files.add(file.getCanonicalFile());
        }
    }
}
