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

import com.newisys.langschema.java.JavaAnnotationType;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.jove.JoveSchema;

public class ConsSchema
    extends JoveSchema
{
    private static final String RANDOMIZABLE_CLASS = "com.newisys.randsolver.annotation.Randomizable";
    public final JavaAnnotationType randomizableType = (JavaAnnotationType) getTypeForSystemClass(RANDOMIZABLE_CLASS);

    public final boolean isRandomizable(JavaType type)
    {
        return isStructuredType(type, RANDOMIZABLE_CLASS);
    }

    public final int getRandomizableSize(JavaType type)
    {
        return 32;
    }

    public boolean isDVIntegral(JavaType type)
    {
        if (type instanceof ConsRandomizableType)
        {
            return true;
        }
        return super.isDVIntegral(type);
    }

    public boolean isDVNumeric(JavaType type)
    {
        if (type instanceof ConsRandomizableType)
        {
            return true;
        }
        return super.isDVNumeric(type);
    }

    protected IntegralTypeInfo getIntegralTypeInfo(JavaType type)
    {
        if (isRandomizable(type))
        {
            return new IntegralTypeInfo(getRandomizableSize(type), false, false);
        }
        return super.getIntegralTypeInfo(type);
    }
}
