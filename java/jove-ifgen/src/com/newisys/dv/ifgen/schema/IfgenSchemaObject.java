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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.Annotation;
import com.newisys.langschema.SchemaObject;

/**
 * Base class for Ifgen schema objects.
 * 
 * @author Trevor Robinson
 */
public abstract class IfgenSchemaObject
    implements SchemaObject
{
    private final IfgenSchema schema;
    private List<Annotation> annotations;

    public IfgenSchemaObject(IfgenSchema schema)
    {
        this.schema = schema;
    }

    public IfgenSchema getSchema()
    {
        return schema;
    }

    public List<Annotation> getAnnotations()
    {
        return annotations != null ? annotations : Collections.EMPTY_LIST;
    }

    public void addAnnotation(Annotation annotation)
    {
        if (annotations == null) annotations = new LinkedList<Annotation>();
        annotations.add(annotation);
    }

    public void addAnnotations(Collection<Annotation> c)
    {
        if (!c.isEmpty())
        {
            if (annotations == null)
                annotations = new LinkedList<Annotation>();
            annotations.addAll(c);
        }
    }

    public void addAnnotations(int index, Collection<Annotation> c)
    {
        if (!c.isEmpty())
        {
            if (annotations == null)
                annotations = new LinkedList<Annotation>();
            annotations.addAll(index, c);
        }
    }

    public abstract void accept(IfgenSchemaObjectVisitor visitor);

    public String toDebugString()
    {
        return super.toString();
    }

    public final String toSourceString()
    {
        return schema.getDefaultPrinter().toString(this);
    }

    @Override
    public final String toString()
    {
        return schema.isUseSourceString() ? toSourceString() : toDebugString();
    }
}
