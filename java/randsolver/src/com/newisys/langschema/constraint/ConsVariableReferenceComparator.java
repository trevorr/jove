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

package com.newisys.langschema.constraint;

import java.util.Comparator;

import com.newisys.langschema.java.JavaMemberVariable;

public class ConsVariableReferenceComparator<T>
    implements Comparator<T>
{

    public final static ConsVariableReferenceComparator< ? super ConsVariableReference> INSTANCE = new ConsVariableReferenceComparator<ConsVariableReference>();

    public int compare(T t1, T t2)
    {
        ConsVariableReference ref1 = (ConsVariableReference) t1;
        ConsVariableReference ref2 = (ConsVariableReference) t2;

        if (ref1 == ref2) return 0;

        JavaMemberVariable var1 = (JavaMemberVariable) ref1.getVariable();
        JavaMemberVariable var2 = (JavaMemberVariable) ref2.getVariable();

        String canonName1 = var1.getStructuredType().getName().getIdentifier()
            + "." + var1.getName().getIdentifier();
        String canonName2 = var2.getStructuredType().getName().getIdentifier()
            + "." + var2.getName().getIdentifier();

        return canonName1.compareTo(canonName2);
    }

}
