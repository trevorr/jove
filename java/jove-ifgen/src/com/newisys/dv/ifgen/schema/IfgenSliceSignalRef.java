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

package com.newisys.dv.ifgen.schema;

/**
 * Ifgen schema object for signal slice references.
 * 
 * @author Trevor Robinson
 */
public final class IfgenSliceSignalRef
    implements IfgenSignalRef
{
    private IfgenSignalRef signal;
    private int fromIndex;
    private int toIndex;

    public IfgenSliceSignalRef(IfgenSignalRef signal, int fromIndex, int toIndex)
    {
        this.signal = signal;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    public IfgenSignalRef getSignal()
    {
        return signal;
    }

    public void setSignal(IfgenSignalRef baseRef)
    {
        this.signal = baseRef;
    }

    public int getFromIndex()
    {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex)
    {
        this.fromIndex = fromIndex;
    }

    public int getToIndex()
    {
        return toIndex;
    }

    public void setToIndex(int toIndex)
    {
        this.toIndex = toIndex;
    }

    public void accept(IfgenSignalRefVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(signal);
        buf.append('[');
        buf.append(fromIndex);
        if (toIndex != fromIndex)
        {
            buf.append(':');
            buf.append(toIndex);
        }
        buf.append(']');
        return buf.toString();
    }
}
