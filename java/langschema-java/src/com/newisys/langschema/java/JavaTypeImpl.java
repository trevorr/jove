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

import java.util.*;
import java.util.Map.Entry;

import com.newisys.langschema.Type;
import com.newisys.langschema.TypeModifier;

/**
 * Base implementation for all Java types.
 * 
 * @author Trevor Robinson
 */
public abstract class JavaTypeImpl
    extends JavaSchemaObjectImpl
    implements JavaType
{
    public JavaTypeImpl(JavaSchema schema)
    {
        super(schema);
    }

    public boolean hasTypeVariables()
    {
        return false;
    }

    public JavaType getErasure()
    {
        return this;
    }

    public boolean contains(JavaType type)
    {
        return equals(type);
    }

    public Set< ? extends TypeModifier> getModifiers()
    {
        return Collections.emptySet();
    }

    public boolean isAssignableFrom(Type other)
    {
        return other == this
            || (other instanceof JavaType && isSubtype((JavaType) other));
    }

    public boolean isStrictIntegral()
    {
        return false;
    }

    public boolean isIntegralConvertible()
    {
        return isStrictIntegral();
    }

    public void accept(JavaSchemaObjectVisitor visitor)
    {
        accept((JavaTypeVisitor) visitor);
    }

    public String toDebugString()
    {
        // for non-complex types, the short string is the reference string with
        // " type" appended
        return toReferenceString() + " type";
    }

    protected static Map<JavaTypeVariable, JavaReferenceType> createTypeVarMap(
        final List<JavaTypeVariable> typeVars,
        final List<JavaReferenceType> typeArgs)
    {
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap = new LinkedHashMap<JavaTypeVariable, JavaReferenceType>();
        buildTypeVarMap(typeVarMap, typeVars, typeArgs);
        return typeVarMap;
    }

    protected static Map<JavaTypeVariable, JavaReferenceType> extendTypeVarMap(
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        final List<JavaTypeVariable> typeVars,
        final List<JavaReferenceType> typeArgs)
    {
        final Map<JavaTypeVariable, JavaReferenceType> newTypeVarMap = new LinkedHashMap<JavaTypeVariable, JavaReferenceType>(
            typeVarMap);
        buildTypeVarMap(newTypeVarMap, typeVars, typeArgs);
        return newTypeVarMap;
    }

    private static void buildTypeVarMap(
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        final List<JavaTypeVariable> typeVars,
        final List<JavaReferenceType> typeArgs)
    {
        if (typeVars.size() != typeArgs.size())
        {
            throw new IllegalArgumentException(typeVars.size()
                + " type arguments expected; " + typeArgs.size() + " provided");
        }
        final Iterator<JavaTypeVariable> typeVarIter = typeVars.iterator();
        for (final JavaReferenceType typeArg : typeArgs)
        {
            final JavaTypeVariable typeVar = typeVarIter.next();
            typeVarMap.put(typeVar, typeArg);
        }
    }

    protected static void checkTypeVarMap(
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        for (final Map.Entry<JavaTypeVariable, JavaReferenceType> e : typeVarMap
            .entrySet())
        {
            final JavaTypeVariable typeVar = e.getKey();
            final JavaReferenceType typeArg = e.getValue();
            if (typeArg instanceof JavaNullType
                || !typeVar.isAssignableFrom(typeArg))
            {
                throw new IllegalArgumentException(
                    "Invalid type for type argument " + typeVar.getName()
                        + ": " + typeArg.toReferenceString());
            }
        }
    }

    protected static void addTypeArgWildcards(
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        final Collection<JavaTypeVariable> typeVars)
    {
        for (final JavaTypeVariable typeVar : typeVars)
        {
            JavaReferenceType typeArg = typeVarMap.get(typeVar);
            if (typeArg == null)
            {
                typeArg = typeVar.getWildcard();
                typeVarMap.put(typeVar, typeArg);
            }
        }
    }

    protected static Map<JavaTypeVariable, JavaReferenceType> restrictTypeVarMap(
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        final List<JavaTypeVariable> typeVars)
    {
        final Map<JavaTypeVariable, JavaReferenceType> newTypeVarMap = new LinkedHashMap<JavaTypeVariable, JavaReferenceType>(
            typeVarMap);
        newTypeVarMap.keySet().retainAll(typeVars);
        return newTypeVarMap;
    }

    protected static List<JavaReferenceType> extractTypeArgs(
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        final List<JavaTypeVariable> typeVars)
    {
        final List<JavaReferenceType> typeArgs = new ArrayList<JavaReferenceType>(
            typeVars.size());
        for (final JavaTypeVariable typeVar : typeVars)
        {
            JavaReferenceType typeArg = typeVarMap.get(typeVar);
            if (typeArg == null) typeArg = typeVar;
            typeArgs.add(typeArg);
        }
        return typeArgs;
    }

    protected static JavaType resolveType(
        JavaType type,
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        return resolveType(type, typeVarMap, null);
    }

    protected static JavaType resolveType(
        final JavaType type,
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        Set<JavaTypeVariable> unmappedVars)
    {
        if (false)
        {
            System.out.println("resolveType: " + type.toReferenceString() + "@"
                + System.identityHashCode(type) + " "
                + printTypeVarMap(typeVarMap));
        }

        if (type instanceof JavaTypeVariable)
        {
            JavaTypeVariable typeVar = (JavaTypeVariable) type;
            JavaType typeArg = typeVarMap.get(typeVar);
            if (typeArg != null) return typeArg;

            // avoid infinite recursion on self-referential unmapped variables
            if (unmappedVars == null)
            {
                unmappedVars = new LinkedHashSet<JavaTypeVariable>();
            }
            if (!unmappedVars.contains(typeVar))
            {
                unmappedVars.add(typeVar);
                final JavaTypeVariable resolvedVar = new JavaTypeVariable(type
                    .getSchema(), typeVar.getName().getIdentifier());
                if (resolveBoundedType(typeVar, resolvedVar, typeVarMap,
                    unmappedVars))
                {
                    return resolvedVar;
                }
            }
        }
        else if (type instanceof JavaParameterizedType)
        {
            final JavaParameterizedType< ? > paramType = (JavaParameterizedType) type;
            final JavaRawStructuredType< ? > rawType = paramType.getRawType();
            final List<JavaReferenceType> typeArgs = paramType
                .getActualTypeArguments();
            final List<JavaType> resolvedTypeArgs = new LinkedList<JavaType>();
            boolean argChanged = false;
            for (final JavaType typeArg : typeArgs)
            {
                final JavaType resolvedTypeArg = resolveType(typeArg,
                    typeVarMap, unmappedVars);
                argChanged |= resolvedTypeArg != typeArg;
                resolvedTypeArgs.add(resolvedTypeArg);
            }
            if (argChanged)
            {
                final JavaReferenceType[] typeArgArray = new JavaReferenceType[resolvedTypeArgs
                    .size()];
                resolvedTypeArgs.toArray(typeArgArray);
                final JavaParameterizedType< ? > resolvedType = rawType
                    .parameterize(typeArgArray);
                return resolvedType;
            }
        }
        else if (type instanceof JavaWildcardType)
        {
            final JavaWildcardType boundedType = (JavaWildcardType) type;
            final JavaWildcardType resolvedType = new JavaWildcardType(type
                .getSchema());
            if (resolveBoundedType(boundedType, resolvedType, typeVarMap,
                unmappedVars))
            {
                return resolvedType;
            }
        }

        return type;
    }

    private static boolean resolveBoundedType(
        final JavaBoundedType boundedType,
        final JavaBoundedType resolvedType,
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        final Set<JavaTypeVariable> unmappedVars)
    {
        boolean boundChanged = false;
        for (final JavaTypeBound bound : boundedType.upperBounds)
        {
            final JavaType resolvedBound = resolveType(bound, typeVarMap,
                unmappedVars);
            boundChanged |= resolvedBound != bound;
            applyUpperBound(resolvedType, resolvedBound);
        }
        for (final JavaTypeBound bound : boundedType.lowerBounds)
        {
            final JavaType resolvedBound = resolveType(bound, typeVarMap,
                unmappedVars);
            boundChanged |= resolvedBound != bound;
            applyLowerBound(resolvedType, resolvedBound);
        }
        return boundChanged;
    }

    private static void applyUpperBound(
        final JavaBoundedType boundedType,
        final JavaType bound)
    {
        if (bound instanceof JavaWildcardType)
        {
            JavaWildcardType wildType = (JavaWildcardType) bound;
            boundedType.addUpperBounds(wildType.getUpperBounds());
        }
        else
        {
            boundedType.addUpperBound((JavaTypeBound) bound);
        }
    }

    private static void applyLowerBound(
        final JavaBoundedType boundedType,
        final JavaType bound)
    {
        if (bound instanceof JavaWildcardType)
        {
            JavaWildcardType wildType = (JavaWildcardType) bound;
            boundedType.addLowerBounds(wildType.getLowerBounds());
        }
        else
        {
            boundedType.addLowerBound((JavaTypeBound) bound);
        }
    }

    private static String printTypeVarMap(
        final Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        final StringBuilder buf = new StringBuilder();
        buf.append('{');
        boolean first = true;
        for (final Entry<JavaTypeVariable, JavaReferenceType> e : typeVarMap
            .entrySet())
        {
            final JavaTypeVariable var = e.getKey();
            final JavaReferenceType type = e.getValue();
            if (!first) buf.append(", ");
            buf.append(var.getName().getIdentifier());
            buf.append('=');
            buf.append(type.toReferenceString());
            first = false;
        }
        buf.append('}');
        return buf.toString();
    }
}
