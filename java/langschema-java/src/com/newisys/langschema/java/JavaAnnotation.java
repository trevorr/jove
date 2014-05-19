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

import java.util.HashMap;

import com.newisys.langschema.Annotation;

/**
 * Represents a Java annotation.
 * 
 * @author Trevor Robinson
 */
public final class JavaAnnotation
    extends JavaSchemaObjectImpl
    implements Annotation, JavaAnnotationElementValue
{
    private final JavaAnnotationType type;
    private final HashMap<JavaFunction, JavaAnnotationElementValue> elementValueMap = new HashMap<JavaFunction, JavaAnnotationElementValue>();

    public JavaAnnotation(JavaAnnotationType type)
    {
        super(type.schema);
        this.type = type;
    }

    public JavaAnnotationType getType()
    {
        return type;
    }

    public JavaAnnotationType getResultType()
    {
        return type;
    }

    public JavaAnnotationElementValue getAssignedElementValue(
        JavaFunction element)
    {
        return elementValueMap.get(element);
    }

    public JavaAnnotationElementValue getEffectiveElementValue(
        JavaFunction element)
    {
        JavaAnnotationElementValue value = getAssignedElementValue(element);
        if (value == null) value = element.getDefaultValue();
        return value;
    }

    public void setElementValue(
        JavaFunction element,
        JavaAnnotationElementValue value)
    {
        elementValueMap.put(element, value);
    }

    public void setElementValue(String name, JavaAnnotationElementValue value)
    {
        final JavaFunction element = type.getMethod(name);
        setElementValue(element, value);
    }

    public boolean isLeading()
    {
        return true;
    }

    public void accept(JavaSchemaObjectVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toDebugString()
    {
        return type.getName() + " annotation";
    }
}
