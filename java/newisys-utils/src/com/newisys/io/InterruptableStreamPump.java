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
 * StreamPump class that can be interrupted while reading.
 * 
 * @author Trevor Robinson
 */
public class InterruptableStreamPump
    extends StreamPump
{
    public InterruptableStreamPump(InputStream in, OutputStream out)
    {
        super(in, out);
    }

    public InterruptableStreamPump(
        InputStream in,
        OutputStream out,
        int bufferSize)
    {
        super(in, out, bufferSize);
    }

    protected int read(byte[] b)
        throws IOException
    {
        while (in.available() == 0)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                return -1;
            }
        }
        return super.read(b);
    }
}
