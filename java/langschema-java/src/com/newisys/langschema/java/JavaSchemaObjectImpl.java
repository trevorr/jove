/*
 * LangSchema-Java - Programming Language Modeling Classes for Java (TM)
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

package com.newisys.langschema.java;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.Annotation;

/**
 * Base implementation for Java schema objects.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaSchemaObjectImpl
    implements JavaSchemaObject
{
    protected final JavaSchema schema;
    private List<Annotation> annotations;

    public JavaSchemaObjectImpl(JavaSchema schema)
    {
        this.schema = schema;
    }

    public final JavaSchema getSchema()
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

    public void addAnnotations(Collection< ? extends Annotation> c)
    {
        if (!c.isEmpty())
        {
            if (annotations == null)
                annotations = new LinkedList<Annotation>();
            annotations.addAll(c);
        }
    }

    public void addAnnotations(int index, Collection< ? extends Annotation> c)
    {
        if (!c.isEmpty())
        {
            if (annotations == null)
                annotations = new LinkedList<Annotation>();
            annotations.addAll(index, c);
        }
    }

    public final String toSourceString()
    {
        return schema.getDefaultPrinter().toString(this);
    }

    public final String toString()
    {
        return schema.isUseSourceString() ? toSourceString() : toDebugString();
    }
}
