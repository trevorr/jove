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

package com.newisys.verilog.pli;

import java.io.IOException;
import java.io.OutputStream;

import com.newisys.verilog.VerilogRuntimeException;

/**
 * An OutputStream that writes to the Verilog simulator output stream.
 * 
 * @author Trevor Robinson
 */
public class PLILogOutputStream
    extends OutputStream
{
    private final PLIInterface pliIntf;

    public PLILogOutputStream(PLIInterface pliIntf)
    {
        super();

        this.pliIntf = pliIntf;
    }

    @Override
    public void write(int b)
        throws IOException
    {
        try
        {
            pliIntf.print(new byte[] { (byte) b }, 0, 1);
        }
        catch (VerilogRuntimeException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void write(byte[] b, int off, int len)
        throws IOException
    {
        try
        {
            pliIntf.print(b, off, len);
        }
        catch (VerilogRuntimeException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void flush()
        throws IOException
    {
        try
        {
            pliIntf.flush();
        }
        catch (VerilogRuntimeException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void close()
    {
        // do nothing
    }
}
