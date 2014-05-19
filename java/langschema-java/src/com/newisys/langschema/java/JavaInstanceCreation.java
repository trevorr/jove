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

import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.InstanceCreation;

/**
 * Represents a Java instance creation expression.
 * 
 * @author Trevor Robinson
 */
public final class JavaInstanceCreation
    extends JavaExpression
    implements InstanceCreation
{
    private final JavaStructuredType type;
    private final JavaConstructor ctor; // null if type is interface
    private final List<JavaExpression> arguments = new LinkedList<JavaExpression>();
    private JavaClass anonymousClass;

    public JavaInstanceCreation(JavaStructuredType type, JavaConstructor ctor)
    {
        super(type.getSchema());
        this.type = type;
        this.ctor = ctor;
    }

    public JavaType getResultType()
    {
        return type;
    }

    public JavaType getType()
    {
        return type;
    }

    public JavaConstructor getConstructor()
    {
        return ctor;
    }

    public List<JavaExpression> getArguments()
    {
        return arguments;
    }

    public void addArgument(JavaExpression expr)
    {
        arguments.add(expr);
    }

    public JavaClass getAnonymousClass()
    {
        return anonymousClass;
    }

    public void setAnonymousClass(JavaClass anonymousClass)
    {
        this.anonymousClass = anonymousClass;
    }

    public boolean isConstant()
    {
        return false;
    }

    public void accept(JavaExpressionVisitor visitor)
    {
        visitor.visit(this);
    }
}
