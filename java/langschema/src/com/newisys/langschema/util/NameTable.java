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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;

/**
 * Provides a lookup table of identifier Strings to sets of NamedObjects.
 * 
 * @author Trevor Robinson
 */
public final class NameTable
    implements Serializable, Cloneable
{
    private static final long serialVersionUID = 4121128156234397490L;

    private final Map<String, List<NamedObject>> nameListMap;

    public NameTable()
    {
        nameListMap = new LinkedHashMap<String, List<NamedObject>>();
    }

    public NameTable(NameTable other)
    {
        nameListMap = new LinkedHashMap<String, List<NamedObject>>(
            other.nameListMap);
    }

    private List<NamedObject> getListForIdentifier(String identifier)
    {
        return nameListMap.get(identifier);
    }

    public void addObject(NamedObject obj, String identifier)
    {
        List<NamedObject> objList = getListForIdentifier(identifier);
        if (objList == null)
        {
            objList = new LinkedList<NamedObject>();
            nameListMap.put(identifier, objList);
        }
        objList.add(obj);
    }

    public void addObject(NamedObject obj)
    {
        String identifier = obj.getName().getIdentifier();
        addObject(obj, identifier);
    }

    public void removeObject(NamedObject obj, String identifier)
    {
        List<NamedObject> objList = getListForIdentifier(identifier);
        if (objList != null)
        {
            objList.remove(obj);
            if (objList.isEmpty())
            {
                nameListMap.remove(identifier);
            }
        }
    }

    public void removeObject(NamedObject obj)
    {
        String identifier = obj.getName().getIdentifier();
        removeObject(obj, identifier);
    }

    public void clear()
    {
        nameListMap.clear();
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        List<NamedObject> objList = getListForIdentifier(identifier);
        if (objList != null)
        {
            return new KindIterator(objList, kind);
        }
        else
        {
            return EmptyIterator.getInstance();
        }
    }

    public NameTable clone()
    {
        return new NameTable(this);
    }

    private static class KindIterator
        implements Iterator<NamedObject>
    {
        private final Iterator<NamedObject> iter;
        private final NameKind kind;
        private NamedObject next;

        public KindIterator(List<NamedObject> objects, NameKind kind)
        {
            this.iter = objects.iterator();
            this.kind = kind;
            findNext();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext()
        {
            return next != null;
        }

        public NamedObject next()
        {
            if (next == null)
            {
                throw new NoSuchElementException();
            }
            NamedObject result = next;
            findNext();
            return result;
        }

        private void findNext()
        {
            while (iter.hasNext())
            {
                NamedObject obj = iter.next();
                if (kind.contains(obj.getName().getKind()))
                {
                    next = obj;
                    return;
                }
            }
            next = null;
        }
    }
}
