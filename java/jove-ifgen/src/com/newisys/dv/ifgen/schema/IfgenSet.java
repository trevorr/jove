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
 * A set of expressions and ranges.
 * 
 * @author Jon Nall
 */
public final class IfgenSet
{
    List<IfgenExpression> expressions = new LinkedList<IfgenExpression>();
    List<IfgenRange> ranges = new LinkedList<IfgenRange>();

    public IfgenSet()
    {
        // default constructor
    }

    public IfgenSet(IfgenRange range)
    {
        addRange(range);
    }

    public void addAll(IfgenSet set)
    {
        for (final IfgenExpression e : set.expressions)
        {
            addExpression(e);
        }
        for (final IfgenRange r : set.ranges)
        {
            addRange(r);
        }
    }

    public void addExpression(IfgenExpression expr)
    {
        expressions.add(expr);
    }

    public void addRange(IfgenRange range)
    {
        ranges.add(range);
    }

    public List<IfgenExpression> getExpressions()
    {
        return expressions;
    }

    public List<IfgenRange> getRanges()
    {
        return ranges;
    }
}
