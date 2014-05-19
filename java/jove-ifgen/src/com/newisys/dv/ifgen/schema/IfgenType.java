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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.newisys.langschema.Annotation;
import com.newisys.langschema.Schema;
import com.newisys.langschema.Type;
import com.newisys.langschema.TypeModifier;

/**
 * Base class for all types in an ifgen schema.
 * 
 * @author Jon Nall
 */
public abstract class IfgenType
    implements Type
{
    private final IfgenSchema schema;

    protected IfgenType(IfgenSchema schema)
    {
        this.schema = schema;
    }

    public Set< ? extends TypeModifier> getModifiers()
    {
        return Collections.emptySet();
    }

    public boolean isAssignableFrom(Type other)
    {
        // there are no type conversions in Ifgen
        return other.getClass() == this.getClass();
    }

    public boolean isStrictIntegral()
    {
        return false;
    }

    public boolean isIntegralConvertible()
    {
        return isStrictIntegral();
    }

    public Schema getSchema()
    {
        return schema;
    }

    public List< ? extends Annotation> getAnnotations()
    {
        return Collections.emptyList();
    }

    @Override
    public final String toString()
    {
        return schema.isUseSourceString() ? toSourceString() : toDebugString();
    }

}
