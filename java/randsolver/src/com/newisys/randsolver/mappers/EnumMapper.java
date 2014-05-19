/*
 * Jove Constraint-based Random Solver
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

package com.newisys.randsolver.mappers;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import com.newisys.randsolver.Constraint;
import com.newisys.randsolver.RandomMapper;

abstract public class EnumMapper<E extends Enum<E>>
    implements RandomMapper
{
    private Map<E, Integer> valuesToIds;

    abstract public Constraint getConstraint();

    public EnumMapper(Class<E> klass, EnumSet excludedSet)
    {
        valuesToIds = new EnumMap<E, Integer>(klass);
        EnumSet<E> allValues = EnumSet.allOf(klass);

        int pseudoOrdinal = 0;
        for (E e : allValues)
        {
            if (!excludedSet.contains(e))
            {
                valuesToIds.put(e, pseudoOrdinal);
                ++pseudoOrdinal;
            }
        }
    }

    public int getID(Object o)
    {
        Integer id = valuesToIds.get(o);
        if (id == null)
        {
            throw new IllegalArgumentException("Illegal enum: " + o);
        }
        return id;
    }

    public E getObject(int id)
    {
        for (E e : valuesToIds.keySet())
        {
            if (valuesToIds.get(e) == id)
            {
                return e;
            }
        }
        throw new IllegalArgumentException("Illegal object ID: " + id);
    }

    public int size()
    {
        return valuesToIds.size();
    }

}
