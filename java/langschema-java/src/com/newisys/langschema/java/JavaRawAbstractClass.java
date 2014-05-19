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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a raw Java class or similar construct, such as an array or enum.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaRawAbstractClass
    extends JavaRawStructuredTypeImpl<JavaClassMember>
    implements JavaAbstractClass
{
    protected JavaAbstractClass baseClass;

    protected JavaRawAbstractClass(
        JavaSchema schema,
        String id,
        JavaPackage pkg,
        JavaStructuredType< ? > outerType)
    {
        super(schema, id, pkg, outerType);
    }

    final void copyFrom(JavaRawAbstractClass other)
    {
        super.copyFrom(other);
        baseClass = other.baseClass;
    }

    public JavaAbstractClass getBaseClass()
    {
        return baseClass;
    }

    public List<JavaAbstractClass> getBaseClasses()
    {
        return Collections.singletonList(baseClass);
    }

    public boolean isSubtype(JavaType type)
    {
        return super.isSubtype(type)
            || (type instanceof JavaAbstractClass && isSuperclassOf((JavaAbstractClass) type));
    }

    public boolean isSuperclassOf(JavaAbstractClass other)
    {
        while (other != null)
        {
            if (other.getRawType() == this) return true;
            other = other.getBaseClass();
        }
        return false;
    }

    public final JavaMemberVariable newField(String id, JavaType type)
    {
        JavaMemberVariable var = new JavaMemberVariable(id, type);
        addMember(var);
        return var;
    }

    public final JavaFunction newMethod(String id, JavaType returnType)
    {
        JavaFunctionType funcType = new JavaFunctionType(returnType);
        JavaFunction func = new JavaFunction(id, funcType);
        addMember(func);
        return func;
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
        return getNonOverriddenMethods(this);
    }

    static Collection<JavaFunction> getNonOverriddenMethods(
        JavaAbstractClass cls)
    {
        Map<MethodSignature, JavaFunction> methodSigMap = new HashMap<MethodSignature, JavaFunction>();

        // first, add methods declared in classes, starting with most-derived
        JavaAbstractClass cur = cls;
        while (cur != null)
        {
            addDeclaredNonOverriddenMethods(cur, methodSigMap);
            cur = cur.getBaseClass();
        }

        // then, add methods declared in interfaces
        cur = cls;
        while (cur != null)
        {
            addNonOverriddenMethodsInInterfaces(cur, methodSigMap);
            cur = cur.getBaseClass();
        }

        return methodSigMap.values();
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

    public JavaConstructor newConstructor()
    {
        JavaConstructor ctor = new JavaConstructor(schema);
        addMember(ctor);
        return ctor;
    }

    public void addBaseConstructors(JavaSchema schema, JavaVisibility vis)
    {
        if (baseClass == null) return;

        for (final JavaStructuredTypeMember member : baseClass.getMembers())
        {
            if (!(member instanceof JavaConstructor)) continue;

            JavaConstructor baseCtor = (JavaConstructor) member;
            JavaFunctionType baseFuncType = baseCtor.getType();

            // create new constructor
            JavaFunctionType funcType = new JavaFunctionType(baseFuncType);
            JavaConstructor newCtor = new JavaConstructor(funcType);
            newCtor.setVisibility(vis);
            addMember(newCtor);

            // create constructor body
            JavaBlock body = new JavaBlock(schema);
            newCtor.setBody(body);

            // call base constructor
            JavaConstructorInvocation baseCtorCall = new JavaConstructorInvocation(
                new JavaConstructorReference(baseCtor));
            for (final JavaFunctionArgument arg : funcType.getArguments())
            {
                baseCtorCall.addArgument(new JavaVariableReference(arg));
            }
            body.addMember(new JavaExpressionStatement(baseCtorCall));
        }
    }
}
