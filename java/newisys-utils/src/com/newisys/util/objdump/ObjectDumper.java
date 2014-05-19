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

package com.newisys.util.objdump;

/**
 * Helper class used to dump the contents of an object.
 * This class is intended to help implement Object.toString() polymorphically,
 * rather than having each derived class build a string containing all base
 * class attributes. Derived classes can simply override a method defined in
 * the base class that dumps additional attributes to an ObjectDumper.
 * <pre>
 * protected void dumpAttributes(ObjectDumper od)
 * {
 *     // ...
 * }
 *
 * public String toString()
 * {
 *     ObjectDumper od = new ObjectDumper(getClass());
 *     dumpAttributes(od);
 *     return od.toString();
 * }
 * </pre>
 * 
 * @author Trevor Robinson
 */
public class ObjectDumper
{
    private final static int FINALIZED = -1;

    private final StringBuffer buf;
    private int attrCount;

    /**
     * Constructs a new object dumper for the given class, which is typically
     * obtained by calling Object.getClass().
     * @param cls the Class of the object being dumped
     */
    public ObjectDumper(Class cls)
    {
        buf = new StringBuffer(80);
        buf.append(cls.getSimpleName());
        attrCount = 0;
    }

    /**
     * Dumps the name and value of an object attribute to the String being
     * built by this ObjectDumper.
     * @param name the name of an attribute/field
     * @param value the value of the attribute
     */
    public void dumpAttr(String name, Object value)
    {
        newAttr();
        buf.append(name);
        buf.append('=');
        buf.append(value);
    }

    /**
     * Dumps an arbitrary String to the String being built by this
     * ObjectDumper.
     * @param s a String
     */
    public void dumpString(String s)
    {
        newAttr();
        buf.append(s);
    }

    private void newAttr()
    {
        if (attrCount == 0)
        {
            buf.append('{');
        }
        else if (attrCount > 0)
        {
            buf.append(',');
        }
        else
        {
            assert (attrCount == FINALIZED);
            throw new IllegalStateException(
                "Object dump has already been finalized");
        }
        ++attrCount;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if (attrCount > 0)
        {
            buf.append('}');
        }
        attrCount = FINALIZED;

        return buf.toString();
    }
}
