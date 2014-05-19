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

package com.newisys.util.system;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.newisys.io.InterruptableStreamPump;
import com.newisys.io.StreamPump;

/**
 * A collection of system utilities for interacting with external processes.
 *
 * @author Trevor Robinson
 */
public final class SystemUtil
{
    private SystemUtil()
    {
        // prevent instantiation
    }

    public static String[] getShellArgs(String cmd)
    {
        String osName = System.getProperty("os.name");
        String[] args = new String[3];
        if (osName.startsWith("Windows 9"))
        {
            args[0] = "command.com";
            args[1] = "/C";
        }
        else if (osName.startsWith("Windows"))
        {
            args[0] = "cmd.exe";
            args[1] = "/C";
        }
        else
        {
            args[0] = "/bin/sh";
            args[1] = "-c";
        }
        args[2] = cmd;
        return args;
    }

    public static Process execShell(String cmd)
        throws IOException, InterruptedException
    {
        return Runtime.getRuntime().exec(getShellArgs(cmd));
    }

    public static void echoProcess(Process process)
        throws IOException, InterruptedException
    {
        final OutputStream stdin = process.getOutputStream();
        final InputStream stdout = process.getInputStream();
        final InputStream stderr = process.getErrorStream();

        final StreamPump inThread = new InterruptableStreamPump(System.in,
            stdin);
        final StreamPump outThread = new StreamPump(stdout, System.out);
        final StreamPump errThread = new StreamPump(stderr, System.err);

        inThread.start();
        outThread.start();
        errThread.start();

        outThread.join();
        stdout.close();

        errThread.join();
        stderr.close();

        inThread.interrupt();
        inThread.join();
        stdin.close();
    }
}
