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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.newisys.langschema.FunctionType;
import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.util.NameTable;

/**
 * Represents a Java function type.
 * 
 * @author Trevor Robinson
 */
public final class JavaFunctionType
    extends JavaTypeImpl
    implements FunctionType, JavaGenericDeclaration
{
    private final List<JavaTypeVariable> typeVariables = new LinkedList<JavaTypeVariable>();
    private final List<JavaFunctionArgument> arguments = new LinkedList<JavaFunctionArgument>();
    private boolean varArgs;
    private JavaType returnType;
    private final Set<JavaClass> exceptionTypes = new LinkedHashSet<JavaClass>();
    private final NameTable nameTable = new NameTable();

    // TODO: weakly reference cached parameterizations
    private transient Map<Map<JavaTypeVariable, JavaReferenceType>, JavaFunctionType> paramCache;

    public JavaFunctionType(JavaType returnType)
    {
        super(returnType.getSchema());
        this.returnType = returnType;
    }

    public JavaFunctionType(JavaType returnType, List<JavaFunctionArgument> args)
    {
        this(returnType);
        if (args != null) addArguments(args);
    }

    public JavaFunctionType(JavaFunctionType other)
    {
        this(other.returnType);

        // clone arguments
        for (final JavaFunctionArgument otherArg : other.arguments)
        {
            JavaFunctionArgument newArg = newArgument(otherArg.getName()
                .getIdentifier(), otherArg.getType());
            newArg.addModifiers(otherArg.getModifiers());
        }

        // clone exception types
        for (final JavaClass otherThrow : other.exceptionTypes)
        {
            addExceptionType(otherThrow);
        }
    }

    public List<JavaTypeVariable> getTypeVariables()
    {
        return typeVariables;
    }

    public void addTypeVariable(JavaTypeVariable var)
    {
        typeVariables.add(var);
    }

    private JavaFunctionType lookupParameterization(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        return paramCache != null ? paramCache.get(typeVarMap) : null;
    }

    private void cacheParameterization(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap,
        JavaFunctionType type)
    {
        if (paramCache == null)
        {
            paramCache = new HashMap<Map<JavaTypeVariable, JavaReferenceType>, JavaFunctionType>();
        }
        paramCache.put(typeVarMap, type);
    }

    private JavaFunctionType createParameterization(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        final JavaFunctionType result = new JavaFunctionType(resolveType(
            returnType, typeVarMap));
        for (final JavaFunctionArgument arg : arguments)
        {
            result.addArgument(new JavaFunctionArgument(arg.getName()
                .getIdentifier(), resolveType(arg.getType(), typeVarMap)));
        }
        result.setVarArgs(varArgs);
        for (final JavaClass et : exceptionTypes)
        {
            result.addExceptionType((JavaClass) resolveType(et, typeVarMap));
        }
        return result;
    }

    public JavaFunctionType parameterize(JavaReferenceType... args)
    {
        if (typeVariables.isEmpty())
        {
            throw new UnsupportedOperationException("Not a generic type");
        }

        return parameterize(createTypeVarMap(typeVariables, Arrays.asList(args)));
    }

    public JavaFunctionType parameterize(
        Map<JavaTypeVariable, JavaReferenceType> typeVarMap)
    {
        if (!hasTypeVariables())
        {
            throw new UnsupportedOperationException("Not a generic type");
        }

        JavaFunctionType result = lookupParameterization(typeVarMap);
        if (result == null)
        {
            result = createParameterization(typeVarMap);
            cacheParameterization(typeVarMap, result);
        }
        return result;
    }

    public List<JavaFunctionArgument> getArguments()
    {
        return arguments;
    }

    public void addArgument(JavaFunctionArgument arg)
    {
        arguments.add(arg);
        nameTable.addObject(arg);
    }

    public void addArguments(List<JavaFunctionArgument> args)
    {
        for (final JavaFunctionArgument arg : args)
        {
            addArgument(arg);
        }
    }

    public JavaFunctionArgument newArgument(String id, JavaType type)
    {
        JavaFunctionArgument arg = new JavaFunctionArgument(id, type);
        addArgument(arg);
        return arg;
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public boolean isVarArgs()
    {
        return varArgs;
    }

    public void setVarArgs(boolean varArgs)
    {
        this.varArgs = varArgs;
    }

    public JavaType getReturnType()
    {
        return returnType;
    }

    public void setReturnType(JavaType returnType)
    {
        this.returnType = returnType;
    }

    public Set<JavaClass> getExceptionTypes()
    {
        return exceptionTypes;
    }

    public void addExceptionType(JavaClass exception)
    {
        exceptionTypes.add(exception);
    }

    public boolean hasTypeVariables()
    {
        if (!typeVariables.isEmpty()) return true;
        for (final JavaFunctionArgument arg : arguments)
        {
            if (arg.getType().hasTypeVariables()) return true;
        }
        if (returnType.hasTypeVariables()) return true;
        for (final JavaClass et : exceptionTypes)
        {
            if (et.hasTypeVariables()) return true;
        }
        return false;
    }

    public boolean isSubtype(JavaType type)
    {
        return type == this;
    }

    public boolean signatureMatches(JavaFunctionType other)
    {
        final List<JavaFunctionArgument> otherArgs = other.getArguments();
        if (arguments.size() == otherArgs.size())
        {
            Iterator<JavaFunctionArgument> thisArgIter = arguments.iterator();
            Iterator<JavaFunctionArgument> otherArgIter = otherArgs.iterator();
            while (thisArgIter.hasNext())
            {
                JavaFunctionArgument thisArg = thisArgIter.next();
                JavaFunctionArgument otherArg = otherArgIter.next();
                if (!thisArg.getType().equals(otherArg.getType()))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void accept(JavaTypeVisitor visitor)
    {
        visitor.visit(this);
    }

    void printArguments(StringBuffer buf, boolean internalName)
    {
        buf.append('(');
        boolean first = true;
        for (final JavaFunctionArgument arg : arguments)
        {
            if (!first) buf.append(", ");
            JavaType argType = arg.getType();
            buf.append(internalName ? argType.toInternalName() : argType
                .toReferenceString());
            first = true;
        }
        buf.append(')');
    }

    public String toInternalName()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(returnType.toInternalName());
        printArguments(buf, true);
        return buf.toString();
    }

    public String toReferenceString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(returnType.toReferenceString());
        printArguments(buf, false);
        return buf.toString();
    }
}
