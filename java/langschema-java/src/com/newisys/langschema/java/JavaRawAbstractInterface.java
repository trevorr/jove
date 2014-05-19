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

/**
 * Represents a raw Java interface or similar construct, such as an annotation
 * type.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaRawAbstractInterface
    extends JavaRawStructuredTypeImpl<JavaInterfaceMember>
    implements JavaAbstractInterface, JavaInterfaceMember, JavaClassMember
{
    protected JavaRawAbstractInterface(
        JavaSchema schema,
        String id,
        JavaPackage pkg,
        JavaStructuredType< ? > outerType)
    {
        super(schema, id, pkg, outerType);
    }

    public boolean isSubtype(JavaType type)
    {
        return super.isSubtype(type)
            || (type instanceof JavaStructuredType && ((JavaStructuredType) type)
                .implementsInterface(this));
    }

    public final JavaMemberVariable newField(String id, JavaType type)
    {
        JavaMemberVariable var = new JavaMemberVariable(id, type);
        var.setVisibility(JavaVisibility.PUBLIC);
        addMember(var);
        return var;
    }

    public final JavaFunction newMethod(String id, JavaType returnType)
    {
        JavaFunctionType funcType = new JavaFunctionType(returnType);
        JavaFunction func = new JavaFunction(id, funcType);
        func.setVisibility(JavaVisibility.PUBLIC);
        func.addModifier(JavaFunctionModifier.ABSTRACT);
        addMember(func);
        return func;
    }
}
