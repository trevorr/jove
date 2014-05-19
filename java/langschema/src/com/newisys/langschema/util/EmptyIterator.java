/*
 * LangSchema - Generic Programming Language Modeling Interfaces
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

package com.newisys.langschema.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Empty iterator implementation.
 * 
 * @author Trevor Robinson
 * @deprecated Use Collections.emptySet().iterator().
 */
public final class EmptyIterator<E>
    implements Iterator<E>
{
    public static final EmptyIterator INSTANCE = new EmptyIterator();

    public static final <T> EmptyIterator<T> getInstance()
    {
        return (EmptyIterator<T>) INSTANCE;
    }

    private EmptyIterator()
    {
    }

    public void remove()
    {
        throw new NoSuchElementException();
    }

    public boolean hasNext()
    {
        return false;
    }

    public E next()
    {
        throw new NoSuchElementException();
    }
}
