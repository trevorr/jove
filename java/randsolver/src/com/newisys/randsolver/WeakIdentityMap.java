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

package com.newisys.randsolver;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class WeakIdentityMap<K, V>
    implements Map<K, V>
{
    final class WeakId<K>
        extends WeakReference<K>
    {
        private int hashCode = 0;

        public WeakId(K obj, ReferenceQueue<K> queue)
        {
            super(obj, queue);
            this.hashCode = System.identityHashCode(obj);
        }

        public int hashCode()
        {
            return hashCode;
        }

        public boolean equals(Object o1)
        {
            Object obj = this.get();

            if (obj != null)
            {
                return obj == ((WeakId) o1).get();
            }
            else
            {
                return this == o1;
            }
        }

        public String toString()
        {
            return "WeakId(" + get() + ")@" + hashCode();
        }
    }

    /**
     * The underlying map used in this implementation.
     */
    Map<WeakId<K>, V> map;

    /**
     * The reference queue used to remove entries from the map that have
     * been garbage-collected.
     */
    ReferenceQueue<K> queue = new ReferenceQueue<K>();

    public WeakIdentityMap(int initialCapacity, float loadFactor)
    {
        map = new HashMap<WeakId<K>, V>(initialCapacity, loadFactor);
    }

    public WeakIdentityMap(int initialCapacity)
    {
        map = new HashMap<WeakId<K>, V>(initialCapacity);
    }

    public WeakIdentityMap()
    {
        map = new HashMap<WeakId<K>, V>();
    }

    public boolean containsKey(Object key)
    {
        checkRefQueue();
        return map.containsKey(new WeakId<K>((K) key, queue));
    }

    public boolean containsValue(Object value)
    {
        checkRefQueue();
        return map.containsValue(value);
    }

    public void clear()
    {
        checkRefQueue();
        map.clear();
    }

    public V put(K key, V value)
    {
        checkRefQueue();
        V oldvalue = map.put(new WeakId<K>(key, queue), value);
        return oldvalue;
    }

    public void putAll(Map< ? extends K, ? extends V> t)
    {
        checkRefQueue();
        for (K key : t.keySet())
        {
            map.put(new WeakId<K>(key, queue), t.get(key));
        }
    }

    public Collection<V> values()
    {
        checkRefQueue();
        return map.values();
    }

    public int size()
    {
        checkRefQueue();
        return map.size();
    }

    public boolean isEmpty()
    {
        checkRefQueue();
        return map.isEmpty();
    }

    public Set<K> keySet()
    {
        checkRefQueue();
        throw new UnsupportedOperationException("keySet");
    }

    public Set<Map.Entry<K, V>> entrySet()
    {
        checkRefQueue();
        throw new UnsupportedOperationException("entrySet");
    }

    public V remove(Object key)
    {
        checkRefQueue();
        V value = map.remove(new WeakId<K>((K) key, queue));
        return value;
    }

    public V get(Object key)
    {
        checkRefQueue();
        V value = map.get(new WeakId<K>((K) key, queue));
        return value;
    }

    private void checkRefQueue()
    {
        Reference ref;
        while ((ref = queue.poll()) != null)
        {
            map.remove(ref);
            ref = null;
        }
    }
}
