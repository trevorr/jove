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

import java.util.Iterator;
import java.util.List;

import com.newisys.langschema.Functor;

/**
 * Base class for Java "functors", which represent the common attributes of
 * methods and constructors.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaFunctor
    extends JavaSchemaObjectImpl
    implements Functor, JavaStructuredTypeMember
{
    protected final JavaFunctionType funcType;
    protected JavaVisibility visibility;
    protected JavaBlock body;
    protected JavaStructuredType< ? > container;

    public JavaFunctor(JavaFunctionType funcType)
    {
        super(funcType.schema);
        this.funcType = funcType;
        this.visibility = JavaVisibility.DEFAULT;
    }

    final void copyFrom(JavaFunctor other)
    {
        visibility = other.visibility;
        body = other.body;
    }

    public abstract JavaFunctor clone();

    public final JavaFunctionType getType()
    {
        return funcType;
    }

    public final JavaVisibility getVisibility()
    {
        return visibility;
    }

    public final void setVisibility(JavaVisibility visibility)
    {
        this.visibility = visibility;
    }

    public final boolean isAccessible(JavaStructuredType< ? > fromType)
    {
        return JavaStructuredTypeImpl.isAccessible(this, fromType);
    }

    public final JavaBlock getBody()
    {
        return body;
    }

    public final void setBody(JavaBlock body)
    {
        this.body = body;
    }

    public final JavaStructuredType< ? > getStructuredType()
    {
        return container;
    }

    public void setStructuredType(JavaStructuredType< ? > container)
    {
        this.container = container;
    }

    public final boolean signatureMatches(JavaFunctor other)
    {
        return funcType.signatureMatches(other.funcType);
    }

    public boolean isMoreSpecific(JavaFunctor other)
    {
        // signatures should not match exactly (i.e. unique signature and
        // override checking should have been done already)
        assert (!signatureMatches(other));

        // this method's argument types must be convertible to other method's
        // argument types
        List<JavaFunctionArgument> thisArgs = funcType.getArguments();
        List<JavaFunctionArgument> otherArgs = other.funcType.getArguments();
        assert (thisArgs.size() == otherArgs.size());
        Iterator<JavaFunctionArgument> thisArgIter = thisArgs.iterator();
        Iterator<JavaFunctionArgument> otherArgIter = otherArgs.iterator();
        while (thisArgIter.hasNext())
        {
            JavaFunctionArgument thisArg = thisArgIter.next();
            JavaFunctionArgument otherArg = otherArgIter.next();
            if (!otherArg.getType().isAssignableFrom(thisArg.getType()))
            {
                return false;
            }
        }

        return true;
    }
}
