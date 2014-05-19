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
 * An exception to describe the condition when the {@link Solver} is asked to enable
 * or disable a random variable that does not exist.
 * 
 * @author Jon Nall
 *
 */
public final class InvalidRandomVarException
    extends RuntimeException
{
    private static final long serialVersionUID = 3690756185430963765L;

    public InvalidRandomVarException(Exception e)
    {
        super(e);
    }

    public InvalidRandomVarException(String msg)
    {
        super(msg);
    }
}
