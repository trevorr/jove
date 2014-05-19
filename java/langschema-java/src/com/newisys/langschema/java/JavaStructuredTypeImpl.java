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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.util.NameTable;

/**
 * Base implementation for Java structured types, such as classes and
 * interfaces.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaStructuredTypeImpl<M extends JavaStructuredTypeMember>
    extends JavaReferenceTypeImpl
    implements JavaStructuredType<M>
{
    protected JavaStructuredType< ? > outerType;
    private final LinkedList<JavaAbstractInterface> baseInterfaces;
    private final LinkedList<M> members;
    private final NameTable nameTable;

    public JavaStructuredTypeImpl(
        JavaSchema schema,
        JavaStructuredType< ? > outerType)
    {
        super(schema);
        this.outerType = outerType;
        this.baseInterfaces = new LinkedList<JavaAbstractInterface>();
        this.members = new LinkedList<M>();
        this.nameTable = new NameTable();
    }

    final void copyFrom(JavaStructuredTypeImpl<M> other)
    {
        baseInterfaces.addAll(other.baseInterfaces);
        for (final M otherMember : other.members)
        {
            if (!(otherMember instanceof JavaInitializerBlock))
            {
                addMember((M) otherMember.clone());
            }
        }
    }

    public JavaStructuredType<M> clone()
    {
        throw new UnsupportedOperationException(getClass().getName()
            + " cannot be cloned");
    }

    public final JavaStructuredType< ? > getStructuredType()
    {
        return outerType;
    }

    public void setStructuredType(JavaStructuredType< ? > outerType)
    {
        this.outerType = outerType;
    }

    public final boolean isContainingTypeOf(JavaStructuredTypeMember other)
    {
        final JavaRawStructuredType<M> rawThis = getRawType();
        JavaStructuredType< ? > container = other.getStructuredType();
        while (container != null)
        {
            JavaRawStructuredType< ? > rawContainer = container.getRawType();
            if (rawContainer == rawThis) return true;
            container = rawContainer.getStructuredType();
        }
        return false;
    }

    public abstract Set<JavaTypeModifier> getModifiers();

    public boolean hasTypeVariables()
    {
        return outerType != null && outerType.hasTypeVariables();
    }

    public List<JavaTypeVariable> getFullTypeVariables()
    {
        final List<JavaTypeVariable> vars = new LinkedList<JavaTypeVariable>();
        addTypeVarsFrom(this, vars);
        return vars;
    }

    private static void addTypeVarsFrom(
        JavaStructuredType< ? > type,
        List<JavaTypeVariable> vars)
    {
        // non-static nested classes inherit type variables from outer classes
        final JavaRawStructuredType< ? > rawType = type.getRawType();
        if (rawType instanceof JavaRawClass
            && !rawType.getModifiers().contains(JavaTypeModifier.STATIC))
        {
            final JavaStructuredType outerType = rawType.getStructuredType();
            if (outerType != null)
            {
                addTypeVarsFrom(outerType, vars);
            }
        }
        vars.addAll(rawType.getTypeVariables());
    }

    public final List<JavaAbstractInterface> getBaseInterfaces()
    {
        return baseInterfaces;
    }

    protected void addBaseInterface(JavaAbstractInterface baseInterface)
    {
        baseInterfaces.add(baseInterface);
    }

    public final boolean implementsInterface(JavaAbstractInterface intf)
    {
        // return true if this type IS the given interface
        if (equals(intf)) return true;

        // check whether this type implements the given interface
        for (final JavaAbstractInterface curIntf : baseInterfaces)
        {
            if (curIntf.equals(intf) || curIntf.getRawType() == intf
                || curIntf.implementsInterface(intf)) return true;
        }
        return false;
    }

    public final List<M> getMembers()
    {
        return members;
    }

    protected void addMember(M member)
    {
        addMemberAt(member, members.size());
    }

    protected void addMemberBefore(M member, JavaStructuredTypeMember other)
    {
        addMemberAt(member, members.indexOf(other));
    }

    protected void addMemberAfter(M member, JavaStructuredTypeMember other)
    {
        addMemberAt(member, members.indexOf(other) + 1);
    }

    private void addMemberAt(M member, int index)
    {
        final JavaStructuredType curType = member.getStructuredType();
        assert (curType == null || curType == this);
        member.setStructuredType(this);
        members.add(index, member);
        if (member instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) member);
        }
    }

    protected void addName(NamedObject obj)
    {
        nameTable.addObject(obj);
    }

    static boolean isAccessible(
        JavaStructuredTypeMember member,
        JavaStructuredType< ? > fromType)
    {
        JavaVisibility vis = member.getVisibility();
        if (vis == JavaVisibility.PUBLIC)
        {
            // public members are always accessible
            return true;
        }
        else if (fromType != null)
        {
            // interface members must be public, so this must be a class member
            JavaAbstractClass memberType = (JavaAbstractClass) member
                .getStructuredType();
            if (vis == JavaVisibility.PRIVATE)
            {
                // private members are accessible within the same top-level type
                return fromType == memberType
                    || fromType.isContainingTypeOf(memberType.getRawType())
                    || memberType.isContainingTypeOf(fromType.getRawType());
            }
            else
            {
                // protected or default member
                JavaPackage memberPkg = memberType.getPackage();
                JavaPackage fromPkg = fromType.getPackage();
                if (fromPkg == memberPkg)
                {
                    // protected or default members are accessible
                    // within same package
                    return true;
                }

                // protected members are also accessible from derived classes
                if (vis == JavaVisibility.PROTECTED
                    && fromType instanceof JavaClass)
                {
                    return memberType.isSuperclassOf((JavaClass) fromType);
                }
            }
        }
        return false;
    }

    public final JavaMemberVariable getField(
        String id,
        JavaStructuredType typeContext)
        throws FieldNotFoundException
    {
        JavaMemberVariable field = getFieldChecked(id, typeContext);
        if (field == null)
        {
            throw new FieldNotFoundException(this, id);
        }
        return field;
    }

    public final JavaMemberVariable getField(String id)
        throws FieldNotFoundException
    {
        return getField(id, null);
    }

    protected JavaMemberVariable getFieldChecked(
        String id,
        JavaStructuredType typeContext)
    {
        // look for accessible fields in this type
        final Iterator< ? > iter = lookupObjects(id, JavaNameKind.EXPRESSION);
        if (iter.hasNext())
        {
            final JavaMemberVariable field = (JavaMemberVariable) iter.next();
            assert (!iter.hasNext());
            if (isAccessible(field, typeContext)) return field;
        }

        // look for accessible fields in base interfaces
        for (final JavaAbstractInterface intf : baseInterfaces)
        {
            JavaStructuredTypeImpl< ? > intfImpl = (JavaStructuredTypeImpl) intf;
            JavaMemberVariable field = intfImpl
                .getFieldChecked(id, typeContext);
            if (field != null) return field;
        }
        return null;
    }

    public final Set<JavaFunction> getMethods(
        String id,
        JavaStructuredType typeContext)
    {
        // find all applicable and accessible methods with matching identifier
        Set<JavaFunction> foundMethods = new HashSet<JavaFunction>();
        findMatchingMethods(id, null, false, true, true, typeContext,
            foundMethods);
        return foundMethods;
    }

    public final Set<JavaFunction> getMethods(
        String id,
        JavaType[] argTypes,
        JavaStructuredType typeContext)
    {
        // find all applicable and accessible methods with matching identifier
        // and argument types, allowing conversions and varargs in phases
        Set<JavaFunction> foundMethods = new HashSet<JavaFunction>();
        findMatchingMethods(id, argTypes, true, false, false, typeContext,
            foundMethods);
        if (foundMethods.isEmpty())
        {
            findMatchingMethods(id, argTypes, true, true, false, typeContext,
                foundMethods);
            if (foundMethods.isEmpty())
            {
                findMatchingMethods(id, argTypes, true, true, true,
                    typeContext, foundMethods);
            }
        }
        return foundMethods;
    }

    public final Set<JavaFunction> getMethods(
        String id,
        JavaType[] argTypes,
        boolean allowConversion,
        boolean allowVarArgs,
        JavaStructuredType typeContext)
    {
        // find all applicable and accessible methods with matching identifier
        // and argument types, allowing conversions and varargs as specified
        Set<JavaFunction> foundMethods = new HashSet<JavaFunction>();
        findMatchingMethods(id, argTypes, true, allowConversion, allowVarArgs,
            typeContext, foundMethods);
        return foundMethods;
    }

    public final JavaFunction getMethod(
        String id,
        JavaType[] argTypes,
        JavaStructuredType typeContext)
        throws MethodNotFoundException, AmbiguousMethodException
    {
        // find all applicable and accessible methods
        Set<JavaFunction> foundMethods = getMethods(id, argTypes, typeContext);

        int count = foundMethods.size();
        if (count == 1)
        {
            // found exactly one method; return it
            return foundMethods.iterator().next();
        }
        else if (count == 0)
        {
            // found no methods; return null
            throw new MethodNotFoundException(this, id, argTypes);
        }
        else
        {
            // found multiple methods; try to find the most specific
            Set<JavaFunction> mostSpecific = getMostSpecific(foundMethods);

            // check for ambiguous method reference
            if (mostSpecific.size() > 1)
            {
                throw new AmbiguousMethodException(this, id, argTypes,
                    mostSpecific);
            }

            // return most specific method
            return mostSpecific.iterator().next();
        }
    }

    public final JavaFunction getMethod(String id, JavaType... argTypes)
        throws MethodNotFoundException, AmbiguousMethodException
    {
        return getMethod(id, argTypes, null);
    }

    protected void findMatchingMethods(
        String id,
        JavaType[] argTypes,
        boolean checkArgs,
        boolean allowConversion,
        boolean allowVarArgs,
        JavaStructuredType typeContext,
        Set<JavaFunction> foundMethods)
    {
        // look for applicable and accessible methods in this type
        Iterator<NamedObject> iter = lookupObjects(id, JavaNameKind.METHOD);
        outerWhile: while (iter.hasNext())
        {
            JavaFunction method = (JavaFunction) iter.next();
            if (isAccessible(method, typeContext)
                && (!checkArgs || argTypesMatch(method.getType(), argTypes,
                    allowConversion, allowVarArgs)))
            {
                // ignore overridden methods
                for (final JavaFunction foundMethod : foundMethods)
                {
                    if (foundMethod.signatureMatches(method))
                    {
                        foundMethod.checkValidOverride(method);
                        continue outerWhile;
                    }
                }

                // found applicable, accessible, non-overridden method
                foundMethods.add(method);
            }
        }

        // recursively look for matching methods in base interfaces
        for (final JavaAbstractInterface intf : baseInterfaces)
        {
            JavaStructuredTypeImpl< ? > intfImpl = (JavaStructuredTypeImpl) intf;
            intfImpl.findMatchingMethods(id, argTypes, checkArgs,
                allowConversion, allowVarArgs, typeContext, foundMethods);
        }
    }

    protected static void addNonOverriddenMethodsInInterfaces(
        JavaStructuredType< ? > type,
        Map<MethodSignature, JavaFunction> methodSigMap)
    {
        // iterate through direct base interfaces of type
        for (final JavaAbstractInterface intf : type.getBaseInterfaces())
        {
            // first, add methods declared in this interface
            addDeclaredNonOverriddenMethods(intf, methodSigMap);

            // then, recursively add methods in its base interfaces
            addNonOverriddenMethodsInInterfaces(intf, methodSigMap);
        }
    }

    protected static void addDeclaredNonOverriddenMethods(
        JavaStructuredType< ? > type,
        Map<MethodSignature, JavaFunction> methodSigMap)
    {
        // iterate through all methods of type
        for (final JavaStructuredTypeMember obj : type.getMembers())
        {
            if (obj instanceof JavaFunction)
            {
                JavaFunction method = (JavaFunction) obj;
                MethodSignature sig = new MethodSignature(method);

                // add method to map if signature is unique
                if (!methodSigMap.containsKey(sig))
                {
                    methodSigMap.put(sig, method);
                }
            }
        }
    }

    protected Set<JavaConstructor> getConstructors(
        JavaStructuredType typeContext)
    {
        // find all applicable and accessible constructors
        Set<JavaConstructor> foundCtors = new HashSet<JavaConstructor>();
        findMatchingConstructors(null, false, true, true, typeContext,
            foundCtors);
        return foundCtors;
    }

    protected Set<JavaConstructor> getConstructors(
        JavaType[] argTypes,
        JavaStructuredType typeContext)
    {
        // find all applicable and accessible constructors with matching
        // argument types, allowing conversions and varargs in phases
        Set<JavaConstructor> foundCtors = new HashSet<JavaConstructor>();
        findMatchingConstructors(argTypes, true, false, false, typeContext,
            foundCtors);
        if (foundCtors.isEmpty())
        {
            findMatchingConstructors(argTypes, true, true, false, typeContext,
                foundCtors);
            if (foundCtors.isEmpty())
            {
                findMatchingConstructors(argTypes, true, true, true,
                    typeContext, foundCtors);
            }
        }
        return foundCtors;
    }

    protected Set<JavaConstructor> getConstructors(
        JavaType[] argTypes,
        boolean allowConversion,
        boolean allowVarArgs,
        JavaStructuredType typeContext)
    {
        // find all applicable and accessible constructors with matching
        // argument types, allowing conversions and varargs as specified
        Set<JavaConstructor> foundCtors = new HashSet<JavaConstructor>();
        findMatchingConstructors(argTypes, true, allowConversion, allowVarArgs,
            typeContext, foundCtors);
        return foundCtors;
    }

    protected JavaConstructor getConstructor(
        JavaType[] argTypes,
        JavaStructuredType typeContext)
        throws ConstructorNotFoundException, AmbiguousConstructorException
    {
        // find all applicable and accessible constructors
        Set<JavaConstructor> foundCtors = getConstructors(argTypes, typeContext);

        int count = foundCtors.size();
        if (count == 1)
        {
            // found exactly one constructor; return it
            return foundCtors.iterator().next();
        }
        else if (count == 0)
        {
            // found no constructors; return null
            throw new ConstructorNotFoundException(this, argTypes);
        }
        else
        {
            // found multiple constructors; try to find the most specific
            Set<JavaConstructor> mostSpecific = getMostSpecific(foundCtors);

            // check for ambiguous constructor reference
            if (mostSpecific.size() > 1)
            {
                throw new AmbiguousConstructorException(this, argTypes,
                    mostSpecific);
            }

            // return most specific constructor
            return mostSpecific.iterator().next();
        }
    }

    protected JavaConstructor getConstructor(JavaType... argTypes)
        throws ConstructorNotFoundException, AmbiguousConstructorException
    {
        return getConstructor(argTypes, null);
    }

    private void findMatchingConstructors(
        JavaType[] argTypes,
        boolean checkArgs,
        boolean allowConversion,
        boolean allowVarArgs,
        JavaStructuredType typeContext,
        Set<JavaConstructor> foundCtors)
    {
        // look for applicable and accessible constructors in this type
        for (final JavaStructuredTypeMember member : getMembers())
        {
            if (!(member instanceof JavaConstructor)) continue;

            JavaConstructor ctor = (JavaConstructor) member;
            if (isAccessible(ctor, typeContext)
                && (!checkArgs || argTypesMatch(ctor.getType(), argTypes,
                    allowConversion, allowVarArgs)))
            {
                // found applicable, accessible constructor
                foundCtors.add(ctor);
            }
        }
    }

    protected static boolean argTypesMatch(
        JavaFunctionType funcType,
        JavaType[] argTypes,
        boolean allowConversion,
        boolean allowVarArgs)
    {
        final int argTypeCount = argTypes != null ? argTypes.length : 0;
        final List<JavaFunctionArgument> formalArgs = funcType.getArguments();
        final int formalArgCount = formalArgs.size();
        final boolean useVarArgs = allowVarArgs && funcType.isVarArgs();
        if (useVarArgs ? argTypeCount >= formalArgCount - 1
            : argTypeCount == formalArgCount)
        {
            final Iterator<JavaFunctionArgument> formalArgIter = formalArgs
                .iterator();
            JavaType formalArgType = null;
            for (int argIndex = 0; argIndex < argTypeCount; ++argIndex)
            {
                JavaType actualArgType = argTypes[argIndex];
                if (argIndex < formalArgCount)
                {
                    JavaFunctionArgument formalArg = formalArgIter.next();
                    formalArgType = formalArg.getType();
                    if (useVarArgs && argIndex == formalArgCount - 1)
                    {
                        formalArgType = ((JavaArrayType) formalArgType)
                            .getAccessType(1);
                    }
                }
                boolean match = allowConversion ? formalArgType
                    .isAssignableFrom(actualArgType) : formalArgType
                    .equals(actualArgType);
                if (!match) return false;
            }
            return true;
        }
        return false;
    }

    protected static <T extends JavaFunctor> Set<T> getMostSpecific(
        Set<T> functors)
    {
        final Set<T> mostSpecific = new HashSet<T>();
        outerLoop: for (final T cur : functors)
        {
            if (mostSpecific.isEmpty())
            {
                // first functor in iteration
                mostSpecific.add(cur);
            }
            else
            {
                // compare this functor against most specific functors so far
                final Iterator<T> iter2 = mostSpecific.iterator();
                while (iter2.hasNext())
                {
                    final JavaFunctor msm = iter2.next();
                    if (cur.isMoreSpecific(msm))
                    {
                        // remove less specific functors from set
                        iter2.remove();
                    }
                    else if (msm.isMoreSpecific(cur))
                    {
                        // most specific functor set already contains a more
                        // specific functor; ignore this functor
                        continue outerLoop;
                    }
                }

                // most specific functor set did not contain a more specific
                // functor; add this functor to it
                mostSpecific.add(cur);
            }
        }
        return mostSpecific;
    }

    public final Iterator<NamedObject> lookupObjects(
        String identifier,
        NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }
}
