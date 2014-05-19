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

/**
 * Ifgen schema object for concatenated signal references.
 * 
 * @author Trevor Robinson
 */
public final class IfgenConcatSignalRef
    implements IfgenSignalRef
{
    private final List<IfgenSignalRef> members = new LinkedList<IfgenSignalRef>();

    public List<IfgenSignalRef> getMembers()
    {
        return members;
    }

    public void addMember(IfgenSignalRef member)
    {
        members.add(member);
    }

    public void accept(IfgenSignalRefVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        boolean first = true;
        for (IfgenSignalRef member : members)
        {
            if (!first) buf.append(", ");
            buf.append(member);
            first = false;
        }
        buf.append(" }");
        return buf.toString();
    }
}
