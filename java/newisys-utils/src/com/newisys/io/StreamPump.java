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

package com.newisys.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class that streams data from an InputStream to an OutputStream, blocking as
 * necessary.
 * 
 * @author Trevor Robinson
 */
public class StreamPump
    extends Thread
{
    protected final InputStream in;
    protected final OutputStream out;
    protected final int bufferSize;

    public StreamPump(InputStream in, OutputStream out)
    {
        this(in, out, 1024);
    }

    public StreamPump(InputStream in, OutputStream out, int bufferSize)
    {
        this.in = in;
        this.out = out;
        this.bufferSize = bufferSize;
    }

    protected int read(byte[] b)
        throws IOException
    {
        return in.read(b);
    }

    public void run()
    {
        try
        {
            final byte[] echoBuffer = new byte[bufferSize];
            while (true)
            {
                // flush output stream if input stream read will block
                if (in.available() == 0)
                {
                    out.flush();
                }

                // read as much as possible from input stream,
                // blocking until data is available
                int count = read(echoBuffer);
                if (count <= 0)
                {
                    break;
                }

                // forward data to output stream
                out.write(echoBuffer, 0, count);
            }
        }
        catch (IOException e)
        {
            // ignored
            e.printStackTrace();
        }
    }
}
