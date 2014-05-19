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

package com.newisys.dv;

import com.newisys.util.text.TextUtil;

/**
 * Maintains a back-reference buffer of a particular depth N. As new values are
 * pushed into the front of the buffer (depth 0), old values are shifted back,
 * with the value previously at depth N-1 being discarded.
 * 
 * @author Trevor Robinson
 */
final class BackRefBuffer
{
    private final Object initialValue;
    private Object[] buffer;

    /**
     * Constructs a new back-reference buffer with the given initial depth and
     * initial value (generally Bit.X or an X-filled bit vector).
     *
     * @param depth the initial depth
     * @param initialValue the value to initialize the buffer entries with
     */
    public BackRefBuffer(int depth, Object initialValue)
    {
        this.initialValue = initialValue;
        setDepth(depth);
    }

    /**
     * Returns the depth of this buffer.
     *
     * @return the depth of this buffer
     */
    public int getDepth()
    {
        return buffer.length;
    }

    /**
     * Changes the depth of this buffer.
     *
     * @param newDepth the new depth, which must be greater than zero
     */
    public void setDepth(int newDepth)
    {
        assert (newDepth > 0);

        // buffer can be null if called from constructor
        final int oldDepth = buffer != null ? buffer.length : 0;

        // do nothing if buffer depth is not changing
        if (newDepth != oldDepth)
        {
            Object[] newBuffer = new Object[newDepth];

            // copy contents of old buffer, if any
            if (buffer != null)
            {
                System.arraycopy(buffer, 0, newBuffer, 0, Math.min(oldDepth,
                    newDepth));
            }

            // initialize any expanded entries to X
            for (int i = oldDepth; i < newDepth; ++i)
            {
                newBuffer[i] = initialValue;
            }

            buffer = newBuffer;
        }
    }

    /**
     * Pushes a new value into the front of the buffer and shifts the previous
     * values back.
     *
     * @param value the new depth-0 value
     */
    public void pushValue(Object value)
    {
        // shift the previous values back
        if (buffer.length > 1)
        {
            System.arraycopy(buffer, 0, buffer, 1, buffer.length - 1);
        }

        // set the depth-0 value
        buffer[0] = value;
    }

    /**
     * Returns the value in the buffer at the given depth.
     *
     * @param depth the depth of the value to return
     * @return the value at the given depth
     */
    public Object getValue(int depth)
    {
        return buffer[depth];
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return TextUtil.toString(buffer);
    }
}
