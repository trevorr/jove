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

/**
 * An exception to describe the condition where the {@link Solver} is asked to 
 * randomize an object, but the constraints of that object are such that no
 * solution exists.
 * 
 * @author Jon Nall
 *
 */
public class UnsolvableConstraintException
    extends RuntimeException
{
    private static final long serialVersionUID = 3691042079861977137L;

    public UnsolvableConstraintException(Exception e)
    {
        super(e);
    }

    public UnsolvableConstraintException(String msg)
    {
        super(msg);
    }
}
