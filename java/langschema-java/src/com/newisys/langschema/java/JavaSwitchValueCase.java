/*
 * LangSchema-Java - Programming Language Modeling Classes for Java (TM)
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

package com.newisys.langschema.java;

import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.SwitchValueCase;
import com.newisys.langschema.Type;

/**
 * Represents a value (i.e. non-default) case in a Java switch statement.
 * 
 * @author Trevor Robinson
 */
public final class JavaSwitchValueCase
    extends JavaSwitchCase
    implements SwitchValueCase
{
    private List<JavaExpression> values = new LinkedList<JavaExpression>();

    JavaSwitchValueCase(JavaSwitchStatement switchStmt)
    {
        super(switchStmt);
    }

    public List<JavaExpression> getValues()
    {
        return values;
    }

    public void addValue(JavaExpression value)
    {
        values.add(value);
    }

    public static boolean isValidValue(JavaExpression value)
    {
        if (value.isConstant())
        {
            Type type = value.getResultType();
            return type instanceof JavaIntType || type instanceof JavaCharType
                || type instanceof JavaShortType
                || type instanceof JavaByteType;
        }
        return false;
    }
}
