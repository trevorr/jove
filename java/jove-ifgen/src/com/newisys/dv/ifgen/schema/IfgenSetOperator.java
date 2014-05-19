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

/**
 * Base class for set operations.
 * 
 * @author Jon Nall
 */
public abstract class IfgenSetOperator
    extends IfgenExpression
{
    protected final IfgenExpression set1;
    protected final IfgenExpression set2;

    protected IfgenSetOperator(
        IfgenSchema schema,
        IfgenExpression set1,
        IfgenExpression set2)
    {
        super(schema);

        this.set1 = set1;
        this.set2 = set2;
    }

    public IfgenExpression getLHS()
    {
        return set1;
    }

    public IfgenExpression getRHS()
    {
        return set2;
    }

    abstract public String getOperatorString();

    @Override
    public String toDebugString()
    {
        return set1 + getOperatorString() + set2;
    }

    @Override
    public IfgenSetType getType()
    {
        if (set1.getType() instanceof IfgenSetType)
        {
            return (IfgenSetType) set1.getType();
        }
        else if (set2.getType() instanceof IfgenSetType)
        {
            IfgenSetType set2type = (IfgenSetType) set2.getType();
            assert (set2type.getMemberType() == set1.getType());
            return set2type;
        }
        else
        {
            assert (set1.getType() == set2.getType());
            return set1.getSchema().getSetType(set1.getType());
        }
    }

    @Override
    public void accept(IfgenSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }
}
