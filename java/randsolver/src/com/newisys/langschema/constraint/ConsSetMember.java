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

public abstract class ConsSetMember
{
    private ConsExpression weight;
    private boolean weightPerItem;

    public abstract void accept(ConsConstraintExpressionVisitor visitor);

    public ConsExpression getWeight()
    {
        return weight;
    }

    public boolean isWeightPerItem()
    {
        return weightPerItem;
    }

    public void setWeight(ConsExpression weight, boolean weightPerItem)
    {
        this.weight = weight;
        this.weightPerItem = weightPerItem;
    }

    protected String getWeightString()
    {
        if (weight == null)
        {
            return "";
        }
        else
        {
            if (weightPerItem)
            {
                return ":= " + weight;
            }
            else
            {
                return ":/ " + weight;
            }
        }
    }
}
