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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.newisys.langschema.constraint.ConsConstraint;
import com.newisys.randsolver.parser.ConstraintParser;
import com.newisys.randsolver.parser.ParseException;

public final class ConstraintCompiler
{

    public final static Constraint compile(Class klass, String constrStr)
    {
        ConsConstraint constraint;
        Constraint cset = null;

        final InputStream istream = new ByteArrayInputStream(constrStr
            .getBytes());
        final ConstraintParser parser = ConstraintParser.getConstraintParser(
            istream, klass);

        try
        {
            Solver.schema.getTypeForClass(klass.getName());
        }
        catch (ClassNotFoundException e)
        {
            throw new InvalidConstraintException(e);
        }

        try
        {
            constraint = parser.Constraint();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            throw new InvalidConstraintException("Error parsing constraint: "
                + e.getMessage());
        }

        cset = new Constraint(klass, constraint.getVarList(), constraint);

        return cset;
    }

}
