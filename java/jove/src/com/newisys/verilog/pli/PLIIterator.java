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

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.newisys.verilog.VerilogObject;

/**
 * An Iterator implementation for PLI objects.
 * 
 * @author Trevor Robinson
 */
public final class PLIIterator<T extends VerilogObject>
    extends PLIVerilogObject
    implements Iterator<T>
{
    private T next;

    public PLIIterator(PLIInterface pliIntf, long handle)
    {
        super(pliIntf, PLIObjectType.ITERATOR, handle, handle != NULL_HANDLE);

        if (handle != NULL_HANDLE)
        {
            advance();
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException("remove not implemented");
    }

    public boolean hasNext()
    {
        return next != null;
    }

    public T next()
    {
        if (next == null)
        {
            throw new NoSuchElementException();
        }

        T obj = next;
        advance();
        return obj;
    }

    // SuppressWarnings avoids the following eclipse warning
    // Type safety: The cast from PLIVerilogObject to T is actually checking
    // against the erased type VerilogObject
    @SuppressWarnings("unchecked")
    private void advance()
    {
        next = (T) pliIntf.scan(handle);

        // handle is auto-freed when scan returns null
        if (next == null)
        {
            invalidate();
        }
    }
}
