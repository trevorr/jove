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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ifgen schema object for complex variable references.
 * 
 * @author Jon Nall
 */
public final class IfgenComplexVariableRef
    extends IfgenExpression
{
    private final IfgenUnresolvedName name;
    private final List<IfgenVariableRef> refs = new LinkedList<IfgenVariableRef>();
    private final List<IfgenExpression> expressions = new LinkedList<IfgenExpression>();

    private final Pattern varPattern = Pattern
        .compile("\\$\\{?[A-Za-z_][A-Za-z0-9_]*\\}?");

    public IfgenComplexVariableRef(IfgenSchema schema, IfgenUnresolvedName name)
    {
        super(schema);
        this.name = name;

        final String s = name.toString();
        int idx = 0;
        Matcher m = varPattern.matcher(s);
        while (m.find())
        {
            if (idx < m.start())
            {
                expressions.add(new IfgenStringLiteral(schema, s.substring(idx,
                    m.start())));
            }
            final String varID = s.substring(m.start(), m.end());
            IfgenUnresolvedName varName = new IfgenUnresolvedName(varID);
            IfgenVariableRef ref = new IfgenVariableRef(schema, varName);
            refs.add(ref);
            expressions.add(ref);
            idx = m.end();
        }

        if (idx < s.length())
        {
            expressions.add(new IfgenStringLiteral(schema, s.substring(idx, s
                .length())));
        }
    }

    public IfgenUnresolvedName getUnresolvedName()
    {
        return name;
    }

    public List<IfgenVariableRef> getVariableRefs()
    {
        return refs;
    }

    @Override
    public IfgenType getType()
    {
        // this always resolves to a string
        return getSchema().STRING_TYPE;
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public List<IfgenExpression> getExpressions()
    {
        return expressions;
    }

}
