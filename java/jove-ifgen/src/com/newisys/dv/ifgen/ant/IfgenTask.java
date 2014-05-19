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

package com.newisys.dv.ifgen.ant;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.newisys.dv.ifgen.Ifgen;
import com.newisys.dv.ifgen.IfgenTranslatorException;
import com.newisys.dv.ifgen.parser.ParseException;
import com.newisys.dv.ifgen.schema.IfgenResolverException;

/**
 * Ant task for the Jove interface generator.
 * 
 * @author Trevor Robinson
 * @author Jon Nall
 */
public class IfgenTask
    extends Task
{
    private String srcroot;
    private String tstamp;
    private String filelist;
    private boolean forcedefaultclock;
    private boolean genshells;
    private final List<FileSet> filesets = new LinkedList<FileSet>();
    private final List<ShellType> shells = new LinkedList<ShellType>();

    public void setSrcroot(String srcroot)
    {
        this.srcroot = srcroot;
    }

    public void setTstamp(String tstamp)
    {
        this.tstamp = tstamp;
    }

    public void setFilelist(String filelist)
    {
        this.filelist = filelist;
    }

    public void setForcedefaultclock(boolean forcedefaultclock)
    {
        this.forcedefaultclock = forcedefaultclock;
    }

    public void setGenshells(boolean genshells)
    {
        this.genshells = genshells;
    }

    public void addFileset(FileSet fileset)
    {
        filesets.add(fileset);
    }

    public void addConfiguredShell(ShellType shell)
    {
        shells.add(shell);
    }

    @Override
    public void execute()
        throws BuildException
    {
        if (srcroot == null)
        {
            throw new BuildException("srcroot attribute must be set",
                getLocation());
        }

        File srcRootDir = new File(srcroot);
        if (!srcRootDir.isAbsolute())
        {
            srcRootDir = new File(getProject().getBaseDir(), srcroot);
        }
        Ifgen ifgen = new Ifgen(srcRootDir);
        if (tstamp != null) ifgen.setTimestampFile(new File(tstamp));
        if (filelist != null) ifgen.setListFile(new File(filelist));
        if (forcedefaultclock) ifgen.setForceDefaultClockPort(true);
        if (genshells) ifgen.setGenshell(true);

        for (final FileSet fileset : filesets)
        {
            File baseDir;
            try
            {
                baseDir = fileset.getDir(getProject()).getCanonicalFile();
            }
            catch (IOException e)
            {
                throw new BuildException(e);
            }
            DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
            String[] names = ds.getIncludedFiles();
            for (String name : names)
            {
                ifgen.addSrcFile(new File(baseDir, name));
            }
        }

        for (final ShellType shell : shells)
        {
            Map<String, String> paramMap = new HashMap<String, String>();
            for (final ArgType pdef : shell.getParameters())
            {
                paramMap.put(pdef.getName(), pdef.getValue());
            }

            ifgen
                .addShell(shell.getTestbench(), paramMap, shell.getShellname());
        }

        try
        {
            ifgen.execute();
        }
        catch (ParseException e)
        {
            throw new BuildException(e);
        }
        catch (IfgenResolverException e)
        {
            throw new BuildException(e);
        }
        catch (IfgenTranslatorException e)
        {
            throw new BuildException(e);
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }
}
