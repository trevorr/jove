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

package com.newisys.util;

/**
 * Base class for objects that maintain a reference count, primarily for
 * implementing copy-on-write behavior.
 * 
 * @author Trevor Robinson
 */
public class RefCounted
{
    private int refCount = 1;

    /**
     * Returns whether there is more than one reference to this object.
     * @return true iff the reference count is greater than 1
     */
    public boolean isShared()
    {
        return refCount > 1;
    }

    /**
     * Increments the reference count of this object.
     */
    public void addRef()
    {
        ++refCount;
    }

    /**
     * Decrements the reference count of this object.
     */
    public void releaseRef()
    {
        --refCount;
        assert (refCount >= 0);
    }
}
