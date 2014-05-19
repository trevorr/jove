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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.newisys.randsolver.annotation.RandExclude;
import com.newisys.randsolver.mappers.EnumMapper;
import com.newisys.randsolver.mappers.TwoStateBitMapper;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;

/**
 * A registry which contains mappings from classes to {@link RandomMapper} and
 * {@link RandomMapperFactory} objects.
 *
 * @author Jon Nall
 * 
 */
public class RandomMapperRegistry
{
    private static Map<Class, RandomMapper> mClassToMapperMap = new HashMap<Class, RandomMapper>();
    private static Set<RandomMapperFactory> mFactories = new HashSet<RandomMapperFactory>();

    static
    {
        // register internal random mappers
        registerMapper(Bit.class, new TwoStateBitMapper());
    }

    public static void registerMapper(Class c, RandomMapper mapper)
    {
        mClassToMapperMap.put(c, mapper);
    }

    public static void registerMapperFactory(RandomMapperFactory factory)
    {
        mFactories.add(factory);
    }

    public static RandomMapper getMapper(Class c)
    {
        // look for a random mapper in two passes, to avoid an unnecessary
        // call to Class.forName()
        RandomMapper mapper = null;
        for (int i = 0; i < 2; ++i)
        {
            mapper = mClassToMapperMap.get(c);
            if (mapper != null)
            {
                return mapper;
            }

            // make sure class is initialized, since it may contain a static
            // initializer that registers a mapper
            try
            {
                Class.forName(c.getName());
            }
            catch (ClassNotFoundException e)
            {
                // ignored
            }
        }

        Iterator iter = mFactories.iterator();
        while (iter.hasNext())
        {
            RandomMapperFactory factory = (RandomMapperFactory) iter.next();
            RandomMapper tmpMapper = factory.getConstraintMapper(c);

            // Make sure only one factory can create a mapper for this class.
            // (or that none can)
            assert (tmpMapper == null || mapper == null);
            mapper = tmpMapper;
        }

        if (mapper == null && Enum.class.isAssignableFrom(c))
        {
            // this is a built-in enum -- make a mapper on the fly
            mapper = buildEnumMapper(c);
        }

        if (mapper != null)
        {
            mClassToMapperMap.put(c, mapper);
        }
        return mapper;
    }

    private static RandomMapper buildEnumMapper(Class< ? extends Enum> enumType)
    {

        // Determine which, if any, enumeration values should be excluded
        // from randomization. Then add these to the enum's constraint.
        Set< ? extends Enum> excludedSet = getExcludedEnums(enumType);
        boolean hasExcludedEnums = excludedSet.size() > 0;
        final EnumSet excludedEnums = hasExcludedEnums ? EnumSet
            .copyOf(excludedSet) : EnumSet.noneOf(enumType);
        final EnumSet usableEnums = EnumSet.complementOf(excludedEnums);

        final int size = usableEnums.size();
        if (size == 0)
        {
            throw new InvalidConstraintException("No enumeration values in: "
                + enumType);
        }

        // figure out least number of bits required for all values
        int numBits = 32 - Integer.numberOfLeadingZeros(size);
        assert (numBits > 0);

        // If the registry doesn't know about this enumeration, make a
        // mapper dynamically, and register it.
        final int enumBits = numBits;
        return new EnumMapper(enumType, excludedEnums)
        {
            public BitVector vect = new BitVector(enumBits);
            public Constraint constraint = ConstraintCompiler.compile(
                getClass(), "vect in {0:" + (size - 1) + "};");

            public Constraint getConstraint()
            {
                return constraint;
            }
        };
    }

    private static <E extends Enum<E>> Set<E> getExcludedEnums(Class<E> enumType)
    {
        Field[] enums = enumType.getDeclaredFields();
        AccessibleObject.setAccessible(enums, true);
        Set<E> excludedSet = new HashSet<E>();
        for (Field f : enums)
        {
            if (f.getType() == enumType
                && f.getAnnotation(RandExclude.class) != null)
            {
                excludedSet.add(Enum.valueOf(enumType, f.getName()));
            }
        }
        return excludedSet;
    }

}
