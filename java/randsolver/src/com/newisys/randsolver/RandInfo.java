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

package com.newisys.randsolver;

import java.util.LinkedList;
import java.util.List;

public class RandInfo
{
    private List<Constraint> mConstraints;
    private RandVarSet mVars;

    public RandInfo()
    {
        mConstraints = new LinkedList<Constraint>();
        mVars = new RandVarSet();
    }

    public RandInfo(RandInfo randInfo)
    {
        this();
        mConstraints.addAll(randInfo.getConstraints());
        mVars.addAll(randInfo.getRandVars());
    }

    public void addConstraint(Constraint c)
    {
        mConstraints.add(c);
    }

    public void addAllConstraints(List<Constraint> l)
    {
        mConstraints.addAll(l);
    }

    public void addRandVars(RandVarSet l)
    {
        mVars.addAll(l);
    }

    public List<Constraint> getConstraints()
    {
        return mConstraints;
    }

    public RandVarSet getRandVars()
    {
        return mVars;
    }

}
