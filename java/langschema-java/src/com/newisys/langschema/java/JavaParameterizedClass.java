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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Java parameterized class.
 * 
 * @author Trevor Robinson
 */
public final class JavaParameterizedClass
    extends JavaParameterizedTypeImpl<JavaClassMember>
    implements JavaClass
{
    protected JavaAbstractClass baseClass;

    JavaParameterizedClass(
        JavaRawClass rawType,
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        super(rawType, typeVarMap);
    }

    protected void resolveBaseClass()
    {
        final JavaAbstractClass baseClass = ((JavaRawClass) rawType).baseClass;
        if (baseClass != null)
        {
            this.baseClass = (JavaAbstractClass) resolveType(baseClass,
                typeVarMap);
        }
    }

    public JavaRawClass getRawType()
    {
        return (JavaRawClass) rawType;
    }

    public JavaParameterizedClass parameterize(JavaReferenceType... args)
    {
        return (JavaParameterizedClass) super.parameterize(args);
    }

    public JavaParameterizedClass parameterize(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        return (JavaParameterizedClass) super.parameterize(typeVarMap);
    }

    public JavaAbstractClass getBaseClass()
    {
        return baseClass;
    }

    public List<JavaAbstractClass> getBaseClasses()
    {
        return Collections.singletonList(baseClass);
    }

    protected boolean isSubtype(JavaStructuredType< ? > type)
    {
        return super.isSubtype(type)
            || (type instanceof JavaAbstractClass && superclassContainsArgs((JavaAbstractClass) type));
    }

    private boolean superclassContainsArgs(JavaAbstractClass type)
    {
        JavaAbstractClass baseCls = type.getBaseClass();
        while (baseCls != null)
        {
            if (baseCls == rawType) /* unchecked conversion */
            {
                return true;
            }
            else if (baseCls.getRawType() == rawType)
            {
                return captureContainsArgs((JavaParameterizedClass) baseCls);
            }
            baseCls = baseCls.getBaseClass();
        }
        return false;
    }

    public boolean isSuperclassOf(JavaAbstractClass other)
    {
        while (other != null)
        {
            if (equals(other)) return true;
            other = other.getBaseClass();
        }
        return false;
    }

    protected final JavaMemberVariable getFieldChecked(
        String id,
        JavaStructuredType typeContext)
    {
        JavaMemberVariable field = super.getFieldChecked(id, typeContext);
        if (field == null && baseClass != null)
        {
            JavaStructuredTypeImpl< ? > baseClassImpl = (JavaStructuredTypeImpl) baseClass;
            field = baseClassImpl.getFieldChecked(id, typeContext);
        }
        return field;
    }

    protected final void findMatchingMethods(
        String id,
        JavaType[] argTypes,
        boolean checkArgs,
        boolean allowConversion,
        boolean allowVarArgs,
        JavaStructuredType typeContext,
        Set<JavaFunction> foundMethods)
    {
        // look for matching methods in this class and base interfaces
        super.findMatchingMethods(id, argTypes, checkArgs, allowConversion,
            allowVarArgs, typeContext, foundMethods);

        // recursively look for matching methods in base classes
        if (baseClass != null)
        {
            JavaStructuredTypeImpl< ? > baseClassImpl = (JavaStructuredTypeImpl) baseClass;
            baseClassImpl.findMatchingMethods(id, argTypes, checkArgs,
                allowConversion, allowVarArgs, typeContext, foundMethods);
        }
    }

    public Collection<JavaFunction> getNonOverriddenMethods()
    {
        return JavaRawAbstractClass.getNonOverriddenMethods(this);
    }

    public Set<JavaConstructor> getConstructors(JavaStructuredType typeContext)
    {
        return super.getConstructors(typeContext);
    }

    public Set<JavaConstructor> getConstructors(
        JavaType[] argTypes,
        JavaStructuredType typeContext)
    {
        return super.getConstructors(argTypes, typeContext);
    }

    public Set<JavaConstructor> getConstructors(
        JavaType[] argTypes,
        boolean allowConversion,
        boolean allowVarArgs,
        JavaStructuredType typeContext)
    {
        return super.getConstructors(argTypes, allowConversion, allowVarArgs,
            typeContext);
    }

    public JavaConstructor getConstructor(
        JavaType[] argTypes,
        JavaStructuredType typeContext)
        throws ConstructorNotFoundException, AmbiguousConstructorException
    {
        return super.getConstructor(argTypes, typeContext);
    }

    public JavaConstructor getConstructor(JavaType... argTypes)
        throws ConstructorNotFoundException, AmbiguousConstructorException
    {
        return super.getConstructor(argTypes);
    }

    public void accept(JavaStructuredTypeMemberVisitor visitor)
    {
        visitor.visit(this);
    }
}
