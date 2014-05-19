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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.newisys.langschema.Name;
import com.newisys.langschema.Namespace;

/**
 * Ifgen schema object representing an object name before it has been resolved.
 * 
 * @author Trevor Robinson
 */
public final class IfgenUnresolvedName
{
    private final List<String> identifiers = new LinkedList<String>();

    public IfgenUnresolvedName()
    {
        // do nothing
    }

    public IfgenUnresolvedName(String id)
    {
        addIdentifier(id);
    }

    public IfgenUnresolvedName(Name name)
    {
        while (true)
        {
            identifiers.add(0, name.getIdentifier());
            final Namespace ns = name.getNamespace();
            if (ns == null) break;
            name = ns.getName();
        }
    }

    public static IfgenUnresolvedName parse(String image)
    {
        IfgenUnresolvedName qname = new IfgenUnresolvedName();
        String[] ids = image.split("[.]");
        for (String id : ids)
        {
            qname.addIdentifier(id);
        }
        return qname;
    }

    public List<String> getIdentifiers()
    {
        return identifiers;
    }

    public void addIdentifier(String identifier)
    {
        identifiers.add(identifier);
    }

    private static final Pattern idPattern = Pattern
        .compile("[a-zA-Z_][a-zA-Z_0-9]*");

    public boolean isValidIdentifiers()
    {
        for (String id : identifiers)
        {
            if (!idPattern.matcher(id).matches() || IfgenKeywords.isKeyword(id))
                return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IfgenUnresolvedName)
        {
            IfgenUnresolvedName other = (IfgenUnresolvedName) obj;
            return identifiers.equals(other.identifiers);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return identifiers.hashCode();
    }

    public String toString(int lastIndex)
    {
        StringBuffer buf = new StringBuffer();
        int index = 0;
        for (String id : identifiers)
        {
            if (index > 0) buf.append('.');
            buf.append(id);
            ++index;
            if (index > lastIndex) break;
        }
        return buf.toString();
    }

    @Override
    public String toString()
    {
        return toString(Integer.MAX_VALUE);
    }
}
