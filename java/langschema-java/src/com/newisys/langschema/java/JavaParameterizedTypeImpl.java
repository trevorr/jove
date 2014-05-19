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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.newisys.langschema.Annotation;

/**
 * Base implementation for Java parameterized types.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaParameterizedTypeImpl<M extends JavaStructuredTypeMember>
    extends JavaStructuredTypeImpl<M>
    implements JavaParameterizedType<M>
{
    protected final JavaRawStructuredType<M> rawType;
    protected final Map<JavaTypeVariable, JavaReferenceType> typeVarMap;
    private transient JavaParameterizedTypeImpl<M> captureType;

    public JavaParameterizedTypeImpl(
        JavaRawStructuredType<M> rawType,
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        super(rawType.getSchema(), null);
        this.rawType = rawType;
        this.typeVarMap = typeVarMap;
    }

    protected void resolveBaseInterfaces()
    {
        // resolve type variables in base interfaces
        for (final JavaAbstractInterface baseIntf : rawType.getBaseInterfaces())
        {
            addBaseInterface((JavaAbstractInterface) resolveType(baseIntf,
                typeVarMap));
        }
    }

    protected void resolveMembers(
        Map<JavaTypeVariable, JavaReferenceType> memberTypeVarMap)
    {
        // resolve type variables in members
        for (final M member : rawType.getMembers())
        {
            M newMember = member;
            if (member instanceof JavaMemberVariable)
            {
                JavaMemberVariable memberVar = (JavaMemberVariable) member;
                JavaMemberVariable newVar = new JavaMemberVariable(memberVar
                    .getName().getIdentifier(), resolveType(
                    memberVar.getType(), typeVarMap));
                newVar.copyFrom(memberVar);
                newMember = (M) newVar;
            }
            else if (member instanceof JavaFunction)
            {
                JavaFunction memberFunc = (JavaFunction) member;
                final JavaFunctionType funcType = memberFunc.getType();
                final JavaFunctionType newFuncType = funcType
                    .hasTypeVariables() ? funcType
                    .parameterize(memberTypeVarMap) : funcType;
                JavaFunction newFunc = new JavaFunction(memberFunc.getName()
                    .getIdentifier(), newFuncType);
                newFunc.copyFrom(memberFunc);
                newMember = (M) newFunc;
            }
            else if (member instanceof JavaConstructor)
            {
                JavaConstructor memberCtor = (JavaConstructor) member;
                final JavaFunctionType funcType = memberCtor.getType();
                final JavaFunctionType newFuncType = funcType
                    .hasTypeVariables() ? funcType
                    .parameterize(memberTypeVarMap) : funcType;
                JavaConstructor newCtor = new JavaConstructor(newFuncType);
                newCtor.copyFrom(memberCtor);
                newMember = (M) newCtor;
            }
            else if (member instanceof JavaRawStructuredType)
            {
                JavaRawStructuredType< ? > memberType = (JavaRawStructuredType) member;
                if (!memberType.getModifiers()
                    .contains(JavaTypeModifier.STATIC)
                    && memberType.hasTypeVariables())
                {
                    newMember = (M) memberType.parameterize(memberTypeVarMap);
                }
                else
                {
                    newMember = (M) memberType.clone();
                }
            }
            else
            {
                assert (member instanceof JavaInitializerBlock);
            }
            if (newMember != null)
            {
                final List< ? extends Annotation> ann = member.getAnnotations();
                if (!ann.isEmpty())
                {
                    newMember.addAnnotations(ann);
                }
            }
            addMember(newMember);
        }
    }

    public JavaName getName()
    {
        return rawType.getName();
    }

    public JavaPackage getPackage()
    {
        return rawType.getPackage();
    }

    public JavaVisibility getVisibility()
    {
        return rawType.getVisibility();
    }

    public boolean isAccessible(JavaStructuredType< ? > fromType)
    {
        return rawType.isAccessible(fromType);
    }

    public Set<JavaTypeModifier> getModifiers()
    {
        return rawType.getModifiers();
    }

    public JavaRawStructuredType<M> getRawType()
    {
        return rawType;
    }

    public List<JavaTypeVariable> getTypeVariables()
    {
        return rawType.getTypeVariables();
    }

    public void addTypeVariable(JavaTypeVariable var)
    {
        throw new UnsupportedOperationException(
            "Cannot add type variables to parameterized type");
    }

    public Map<JavaTypeVariable, JavaReferenceType> getFullTypeArguments()
    {
        return typeVarMap;
    }

    public List<JavaReferenceType> getActualTypeArguments()
    {
        return extractTypeArgs(typeVarMap, getTypeVariables());
    }

    public JavaReferenceType getTypeArgument(JavaTypeVariable typeVar)
    {
        final JavaReferenceType typeArg = typeVarMap.get(typeVar);
        if (typeArg == null)
        {
            throw new IllegalArgumentException("Type variable not present: "
                + typeVar.getName());
        }
        return typeArg;
    }

    public JavaParameterizedType<M> parameterize(JavaReferenceType... args)
    {
        final List<JavaTypeVariable> typeVars = getTypeVariables();

        if (typeVars.isEmpty())
        {
            throw new UnsupportedOperationException("Not a generic type");
        }

        return parameterize(extendTypeVarMap(typeVarMap, typeVars, Arrays
            .asList(args)));
    }

    public JavaParameterizedType<M> parameterize(
        Map<JavaTypeVariable, JavaReferenceType> fullTypeVarMap)
    {
        return rawType.parameterize(fullTypeVarMap);
    }

    private static JavaTypeBound tryParameterize(
        JavaTypeBound type,
        Map<JavaTypeVariable, JavaReferenceType> fullTypeVarMap)
    {
        if (type instanceof JavaStructuredType && type.hasTypeVariables())
        {
            return ((JavaStructuredType< ? >) type)
                .parameterize(fullTypeVarMap);
        }
        return type;
    }

    private static List<JavaTypeBound> tryParameterize(
        List< ? extends JavaTypeBound> types,
        Map<JavaTypeVariable, JavaReferenceType> fullTypeVarMap)
    {
        final LinkedList<JavaTypeBound> result = new LinkedList<JavaTypeBound>();
        for (final JavaTypeBound type : types)
        {
            result.add(tryParameterize(type, fullTypeVarMap));
        }
        return result;
    }

    private static List<JavaTypeBound> mergeBounds(
        List< ? extends JavaTypeBound>... typeLists)
    {
        JavaAbstractClass baseCls = null;
        for (final List< ? extends JavaTypeBound> types : typeLists)
        {
            for (final JavaTypeBound type : types)
            {
                if (type instanceof JavaAbstractClass)
                {
                    JavaAbstractClass cls = (JavaAbstractClass) type;
                    if (baseCls == null || cls.isSuperclassOf(baseCls))
                    {
                        baseCls = cls;
                    }
                    else if (!baseCls.isSuperclassOf(cls))
                    {
                        throw new JavaSemanticException(
                            "Unrelated class types in capture conversion: "
                                + baseCls.getName() + " and " + cls.getName());
                    }
                }
            }
        }
        final LinkedList<JavaTypeBound> result = new LinkedList<JavaTypeBound>();
        if (baseCls != null)
        {
            result.add(baseCls);
        }
        for (final List< ? extends JavaTypeBound> types : typeLists)
        {
            for (final JavaTypeBound type : types)
            {
                if (type instanceof JavaAbstractInterface)
                {
                    result.add(type);
                }
            }
        }
        return result;
    }

    public JavaParameterizedTypeImpl<M> capture()
    {
        if (captureType != null) return captureType;

        // create capture variables for each wildcard type argument
        final LinkedHashMap<JavaTypeVariable, JavaReferenceType> newTypeVarMap = new LinkedHashMap<JavaTypeVariable, JavaReferenceType>();
        LinkedHashMap<JavaTypeVariable, JavaTypeVariable> captureVarMap = null;
        for (final Map.Entry<JavaTypeVariable, JavaReferenceType> e : typeVarMap
            .entrySet())
        {
            final JavaTypeVariable typeVar = e.getKey();
            JavaReferenceType typeArg = e.getValue();
            if (typeArg instanceof JavaWildcardType)
            {
                final String captureVarID = "$"
                    + typeVar.getName().getIdentifier();
                final JavaTypeVariable captureVar = new JavaTypeVariable(
                    schema, captureVarID);
                if (captureVarMap == null)
                {
                    captureVarMap = new LinkedHashMap<JavaTypeVariable, JavaTypeVariable>();
                }
                captureVarMap.put(typeVar, captureVar);
                typeArg = captureVar;
            }
            newTypeVarMap.put(typeVar, typeArg);
        }

        if (captureVarMap != null)
        {
            // place proper bounds on capture variables
            for (final Map.Entry<JavaTypeVariable, JavaReferenceType> e : typeVarMap
                .entrySet())
            {
                final JavaType typeArg = e.getValue();
                if (typeArg instanceof JavaWildcardType)
                {
                    final JavaTypeVariable typeVar = e.getKey();
                    final List<JavaTypeBound> vb = typeVar.getUpperBounds();
                    List<JavaTypeBound> captureVB = tryParameterize(vb,
                        newTypeVarMap);
                    final JavaTypeVariable captureVar = captureVarMap
                        .get(typeVar);
                    final JavaWildcardType wcType = (JavaWildcardType) typeArg;
                    final List<JavaTypeBound> ub = wcType.getUpperBounds();
                    if (!ub.isEmpty())
                    {
                        captureVB = mergeBounds(ub, captureVB);
                    }
                    captureVar.addUpperBounds(captureVB);
                    final List<JavaTypeBound> lb = wcType.getLowerBounds();
                    if (!lb.isEmpty())
                    {
                        captureVar.addLowerBounds(lb);
                    }
                }
            }

            // return new parameterization based on capture variables
            captureType = (JavaParameterizedTypeImpl<M>) parameterize(newTypeVarMap);
        }
        else
        {
            // no wildcard type arguments present
            captureType = this;
        }
        return captureType;
    }

    public boolean hasTypeVariables()
    {
        for (final JavaType typeArg : typeVarMap.values())
        {
            if (typeArg.hasTypeVariables()) return true;
        }
        return false;
    }

    public JavaType getErasure()
    {
        return rawType;
    }

    public boolean isSubtype(JavaType type)
    {
        return super.isSubtype(type)
            || type == rawType /* unchecked conversion */
            || (type instanceof JavaStructuredType && isSubtype((JavaStructuredType) type));
    }

    protected boolean isSubtype(JavaStructuredType< ? > type)
    {
        if (type.getRawType() == rawType)
        {
            return captureContainsArgs((JavaParameterizedType< ? >) type);
        }
        else if (interfaceContainsArgs(type))
        {
            return true;
        }
        return false;
    }

    private boolean interfaceContainsArgs(JavaStructuredType< ? > type)
    {
        final List<JavaAbstractInterface> otherIntfs = type.getBaseInterfaces();
        for (final JavaAbstractInterface otherIntf : otherIntfs)
        {
            if (otherIntf == rawType) /* unchecked conversion */
            {
                return true;
            }
            else if (otherIntf.getRawType() == rawType)
            {
                return captureContainsArgs((JavaParameterizedInterface) otherIntf);
            }
            else if (interfaceContainsArgs(otherIntf))
            {
                return true;
            }
        }
        return false;
    }

    protected final boolean captureContainsArgs(JavaParameterizedType< ? > type)
    {
        return capture().containsArgs(type.capture());
    }

    private boolean containsArgs(JavaParameterizedType< ? > other)
    {
        final List<JavaReferenceType> thisArgs = getActualTypeArguments();
        final List<JavaReferenceType> otherArgs = other
            .getActualTypeArguments();
        if (thisArgs.size() != otherArgs.size()) return false;
        final Iterator<JavaReferenceType> thisIter = thisArgs.iterator();
        final Iterator<JavaReferenceType> otherIter = otherArgs.iterator();
        while (thisIter.hasNext())
        {
            assert (otherIter.hasNext());
            final JavaReferenceType thisArg = thisIter.next();
            final JavaReferenceType otherArg = otherIter.next();
            if (!thisArg.contains(otherArg)) return false;
        }
        return true;
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (obj instanceof JavaParameterizedTypeImpl)
        {
            JavaParameterizedTypeImpl other = (JavaParameterizedTypeImpl) obj;
            return (rawType == other.rawType && typeVarMap
                .equals(other.typeVarMap));
        }
        return false;
    }

    public int hashCode()
    {
        return rawType.hashCode() ^ typeVarMap.hashCode();
    }

    public void accept(JavaTypeVisitor visitor)
    {
        visitor.visit(this);
    }

    public String toInternalName()
    {
        return rawType.toInternalName();
    }

    public String toReferenceString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append(rawType.toReferenceString());
        buf.append('<');
        boolean first = true;
        for (final JavaReferenceType typeArg : getActualTypeArguments())
        {
            if (!first) buf.append(", ");
            buf.append(typeArg.toReferenceString());
            first = false;
        }
        buf.append('>');
        return buf.toString();
    }
}
