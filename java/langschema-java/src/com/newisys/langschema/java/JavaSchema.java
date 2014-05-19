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

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.newisys.langschema.NameKind;
import com.newisys.langschema.NamedObject;
import com.newisys.langschema.Schema;
import com.newisys.langschema.Scope;
import com.newisys.langschema.util.NameTable;
import com.newisys.schemaprinter.java.JavaSchemaPrinter;

/**
 * Represents a complete Java schema.
 * 
 * @author Trevor Robinson
 */
public class JavaSchema
    implements Schema
{
    private final List<JavaSchemaMember> members = new LinkedList<JavaSchemaMember>();
    private final NameTable nameTable = new NameTable();
    private transient final Map<String, JavaRawStructuredType> nameTypeCache = new HashMap<String, JavaRawStructuredType>();

    private boolean useSourceString = true;
    private JavaSchemaPrinter defaultPrinter;

    // define schema-singleton primitive type objects
    public final JavaBooleanType booleanType = new JavaBooleanType(this);
    public final JavaByteType byteType = new JavaByteType(this);
    public final JavaCharType charType = new JavaCharType(this);
    public final JavaDoubleType doubleType = new JavaDoubleType(this);
    public final JavaFloatType floatType = new JavaFloatType(this);
    public final JavaIntType intType = new JavaIntType(this);
    public final JavaLongType longType = new JavaLongType(this);
    public final JavaNullType nullType = new JavaNullType(this);
    public final JavaShortType shortType = new JavaShortType(this);
    public final JavaVoidType voidType = new JavaVoidType(this);

    // define special class/interface type objects

    private JavaRawClass objectType;

    public JavaRawClass getObjectType()
    {
        if (objectType == null)
            objectType = (JavaRawClass) getTypeForSystemClass("java.lang.Object");
        return objectType;
    }

    private JavaRawInterface cloneableType;

    public JavaRawInterface getCloneableType()
    {
        if (cloneableType == null)
            cloneableType = (JavaRawInterface) getTypeForSystemClass("java.lang.Cloneable");
        return cloneableType;
    }

    private JavaRawInterface serializableType;

    public JavaRawInterface getSerializableType()
    {
        if (serializableType == null)
            serializableType = (JavaRawInterface) getTypeForSystemClass("java.io.Serializable");
        return serializableType;
    }

    private JavaRawClass stringType;

    public JavaRawClass getStringType()
    {
        if (stringType == null)
            stringType = (JavaRawClass) getTypeForSystemClass("java.lang.String");
        return stringType;
    }

    private JavaRawClass classType;

    public JavaRawClass getClassType()
    {
        if (classType == null)
            classType = (JavaRawClass) getTypeForSystemClass("java.lang.Class");
        return classType;
    }

    private JavaRawClass enumType;

    public JavaRawClass getEnumType()
    {
        if (enumType == null)
            enumType = (JavaRawClass) getTypeForSystemClass("java.lang.Enum");
        return enumType;
    }

    private JavaRawInterface annotationType;

    public JavaRawInterface getAnnotationType()
    {
        if (annotationType == null)
            annotationType = (JavaRawInterface) getTypeForSystemClass("java.lang.annotation.Annotation");
        return annotationType;
    }

    // define primitive wrapper class type objects
    public final JavaRawClass booleanWrapperType = (JavaRawClass) getTypeForSystemClass("java.lang.Boolean");
    public final JavaRawClass byteWrapperType = (JavaRawClass) getTypeForSystemClass("java.lang.Byte");
    public final JavaRawClass characterWrapperType = (JavaRawClass) getTypeForSystemClass("java.lang.Character");
    public final JavaRawClass doubleWrapperType = (JavaRawClass) getTypeForSystemClass("java.lang.Double");
    public final JavaRawClass floatWrapperType = (JavaRawClass) getTypeForSystemClass("java.lang.Float");
    public final JavaRawClass integerWrapperType = (JavaRawClass) getTypeForSystemClass("java.lang.Integer");
    public final JavaRawClass longWrapperType = (JavaRawClass) getTypeForSystemClass("java.lang.Long");
    public final JavaRawClass shortWrapperType = (JavaRawClass) getTypeForSystemClass("java.lang.Short");
    public final JavaRawClass voidWrapperType = (JavaRawClass) getTypeForSystemClass("java.lang.Void");
    {
        booleanType.wrapperClass = booleanWrapperType;
        byteType.wrapperClass = byteWrapperType;
        charType.wrapperClass = characterWrapperType;
        doubleType.wrapperClass = doubleWrapperType;
        floatType.wrapperClass = floatWrapperType;
        intType.wrapperClass = integerWrapperType;
        longType.wrapperClass = longWrapperType;
        shortType.wrapperClass = shortWrapperType;
        voidType.wrapperClass = voidWrapperType;
    }

    public List<JavaSchemaMember> getMembers()
    {
        return members;
    }

    public void addMember(JavaSchemaMember member)
    {
        members.add(member);
        if (member instanceof NamedObject)
        {
            nameTable.addObject((NamedObject) member);
        }
    }

    public void addName(NamedObject obj)
    {
        nameTable.addObject(obj);
    }

    public Iterator<NamedObject> lookupObjects(String identifier, NameKind kind)
    {
        return nameTable.lookupObjects(identifier, kind);
    }

    public final boolean isUseSourceString()
    {
        return useSourceString;
    }

    public final void setUseSourceString(boolean useSourceString)
    {
        this.useSourceString = useSourceString;
    }

    protected JavaSchemaPrinter createDefaultPrinter()
    {
        return new JavaSchemaPrinter();
    }

    public final JavaSchemaPrinter getDefaultPrinter()
    {
        if (defaultPrinter == null)
        {
            defaultPrinter = createDefaultPrinter();
            defaultPrinter.setCollapseBodies(true);
        }
        return defaultPrinter;
    }

    public final void setDefaultPrinter(JavaSchemaPrinter printer)
    {
        defaultPrinter = printer;
    }

    public JavaPackage getPackage(String qname, boolean create)
    {
        JavaPackage pkg = null;
        JavaPackage outerPkg = null;
        Scope scope = this;
        String[] names = qname.split("\\.");
        for (int i = 0; i < names.length; ++i)
        {
            String name = names[i];
            Iterator< ? > iter = scope
                .lookupObjects(name, JavaNameKind.PACKAGE);
            if (iter.hasNext())
            {
                pkg = (JavaPackage) iter.next();
                assert (!iter.hasNext());
            }
            else if (create)
            {
                pkg = new JavaPackage(this, name, outerPkg);
                if (outerPkg != null)
                {
                    outerPkg.addMember(pkg);
                }
                else
                {
                    addMember(pkg);
                }
            }
            else
            {
                // package not found and not okay to create
                return null;
            }
            scope = outerPkg = pkg;
        }
        return pkg;
    }

    public JavaRawStructuredType getTypeForSystemClass(String qname)
    {
        try
        {
            return getTypeForClass(qname);
        }
        catch (ClassNotFoundException e)
        {
            throw new InternalError("Unable to load class: " + qname);
        }
    }

    public JavaRawStructuredType getTypeForClass(String qname)
        throws ClassNotFoundException
    {
        JavaRawStructuredType type = findTypeForClass(qname);
        if (type == null)
        {
            type = buildTypeForClass(qname);
        }
        return type;
    }

    private JavaRawStructuredType getTypeForClass(Class cls)
    {
        JavaPackage pkg = getPackageForClass(cls);
        return getTypeForClass(cls, pkg);
    }

    private JavaRawStructuredType getTypeForClass(Class cls, JavaPackage pkg)
    {
        String qname = cls.getName();
        JavaRawStructuredType type = findTypeForClass(qname);
        if (type == null)
        {
            type = buildTypeForClass(cls, pkg);
        }
        return type;
    }

    private JavaRawStructuredType findTypeForClass(String qname)
    {
        JavaRawStructuredType type = nameTypeCache.get(qname);
        if (type != null) return type;

        Scope scope = this;
        String[] names = qname.split("\\.|\\$");
        for (int i = 0; i < names.length; ++i)
        {
            String name = names[i];
            Iterator< ? > iter = scope.lookupObjects(name, JavaNameKind.TYPE);
            if (iter.hasNext())
            {
                scope = type = (JavaRawStructuredType) iter.next();
                assert (!iter.hasNext());
            }
            else
            {
                iter = scope.lookupObjects(name, JavaNameKind.PACKAGE);
                if (iter.hasNext())
                {
                    assert (type == null);
                    scope = (JavaPackage) iter.next();
                    assert (!iter.hasNext());
                }
                else
                {
                    type = null;
                    break;
                }
            }
        }
        if (type != null) nameTypeCache.put(qname, type);
        return type;
    }

    private JavaRawStructuredType buildTypeForClass(String qname)
        throws ClassNotFoundException
    {
        // get (uninitialized) Class object for given name
        ClassLoader loader = getClass().getClassLoader();
        Class cls = Class.forName(qname, false, loader);

        // get JavaPackage object corresponding to package of class
        JavaPackage pkg = getPackageForClass(cls);

        return buildTypeForClass(cls, pkg);
    }

    private JavaRawStructuredType buildTypeForClass(
        Class< ? > cls,
        JavaPackage pkg)
    {
        // get name of type
        String typeID = cls.getName();
        int lastDot = typeID.lastIndexOf('.');
        int lastDollar = typeID.lastIndexOf('$');
        int lastDelim = Math.max(lastDot, lastDollar);
        if (lastDelim >= 0) typeID = typeID.substring(lastDelim + 1);

        // create type object
        final JavaRawStructuredType type;
        if (cls.isAnnotation())
        {
            type = new JavaAnnotationType(this, typeID, pkg);
        }
        else if (cls.isInterface())
        {
            type = new JavaRawInterface(this, typeID, pkg);
        }
        else if (cls.isEnum())
        {
            type = new JavaEnum(this, typeID, pkg);
        }
        else
        {
            assert (!cls.isPrimitive() && !cls.isArray());

            type = new JavaRawClass(this, typeID, pkg);
        }

        // get type of outer class, if any
        final Class outerCls = cls.getDeclaringClass();
        if (outerCls != null)
        {
            // add nested type to outer type
            JavaRawStructuredType outerType = getTypeForClass(outerCls, pkg);
            outerType.addMember(type);
        }
        else if (pkg != null)
        {
            // add top-level type to package
            pkg.addMember(type);
        }
        else
        {
            // add top-level type in unnamed package to schema
            addMember(type);
        }

        // get type variables
        final TypeVariable[] typeVars = cls.getTypeParameters();
        final Map<String, JavaTypeVariable> jtypeVars = new LinkedHashMap<String, JavaTypeVariable>();
        for (final TypeVariable typeVar : typeVars)
        {
            final String name = typeVar.getName();
            final JavaTypeVariable var = new JavaTypeVariable(this, name);
            ((JavaGenericDeclaration) type).addTypeVariable(var);
            jtypeVars.put(name, var);

            final Type[] bounds = typeVar.getBounds();
            for (Type bound : bounds)
            {
                final JavaTypeBound jbound = (JavaTypeBound) convertType(bound,
                    jtypeVars);
                var.addUpperBound(jbound);
            }
        }

        // get modifiers
        int mod = cls.getModifiers();
        if (Modifier.isPublic(mod))
        {
            type.setVisibility(JavaVisibility.PUBLIC);
        }
        else if (Modifier.isProtected(mod))
        {
            type.setVisibility(JavaVisibility.PROTECTED);
        }
        else if (Modifier.isPrivate(mod))
        {
            type.setVisibility(JavaVisibility.PRIVATE);
        }
        if (Modifier.isAbstract(mod))
        {
            type.addModifier(JavaTypeModifier.ABSTRACT);
        }
        if (Modifier.isFinal(mod))
        {
            type.addModifier(JavaTypeModifier.FINAL);
        }
        if (Modifier.isStatic(mod))
        {
            type.addModifier(JavaTypeModifier.STATIC);
        }
        if (Modifier.isStrict(mod))
        {
            type.addModifier(JavaTypeModifier.STRICTFP);
        }

        // get base class, if any
        if (type instanceof JavaRawClass)
        {
            JavaRawClass jcls = (JavaRawClass) type;
            Type baseCls = cls.getGenericSuperclass();
            if (baseCls != null)
            {
                JavaClass jbaseCls = (JavaClass) convertType(baseCls, jtypeVars);
                jcls.setBaseClass(jbaseCls);
            }
        }

        // get base interfaces
        if (!(type instanceof JavaAnnotationType))
        {
            final Type[] intfs = cls.getGenericInterfaces();
            for (int i = 0; i < intfs.length; ++i)
            {
                JavaInterface jintf = (JavaInterface) convertType(intfs[i],
                    jtypeVars);
                type.addBaseInterface(jintf);
            }
        }

        // get member fields
        final Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i)
        {
            buildField(type, fields[i], jtypeVars);
        }

        // get member constructors
        final Constructor[] ctors = cls.getDeclaredConstructors();
        for (int i = 0; i < ctors.length; ++i)
        {
            buildConstructor((JavaRawAbstractClass) type, ctors[i], jtypeVars);
        }

        // get member methods
        final Method[] methods = cls.getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i)
        {
            buildMethod(type, methods[i], jtypeVars);
        }

        // add annotations (must be done after methods are added for
        // self-annotated annotations, such as Retention)
        addAnnotations(type, cls);

        return type;
    }

    private void buildField(
        JavaRawStructuredType type,
        Field field,
        Map<String, JavaTypeVariable> typeVars)
    {
        // ignore synthetic fields
        if (field.isSynthetic()) return;

        // ignore private fields
        final int mod = field.getModifiers();
        if (Modifier.isPrivate(mod)) return;

        // create schema field
        final JavaMemberVariable var;
        if (field.isEnumConstant())
        {
            var = ((JavaEnum) type).newValue(field.getName(), false);
        }
        else
        {
            var = type.newField(field.getName(), convertType(field
                .getGenericType(), typeVars));
        }

        // get modifiers
        if (Modifier.isPublic(mod))
        {
            var.setVisibility(JavaVisibility.PUBLIC);
        }
        else if (Modifier.isProtected(mod))
        {
            var.setVisibility(JavaVisibility.PROTECTED);
        }
        if (Modifier.isFinal(mod))
        {
            var.addModifier(JavaVariableModifier.FINAL);
        }
        if (Modifier.isStatic(mod))
        {
            var.addModifier(JavaVariableModifier.STATIC);
        }
    }

    private void buildConstructor(
        JavaRawAbstractClass type,
        Constructor< ? > ctor,
        Map<String, JavaTypeVariable> typeVars)
    {
        // ignore synthetic constructors
        if (ctor.isSynthetic()) return;

        // ignore private constructors
        final int mod = ctor.getModifiers();
        if (Modifier.isPrivate(mod)) return;

        // create schema constructor
        final JavaConstructor jctor = type.newConstructor();
        final JavaFunctionType funcType = jctor.getType();

        // get modifiers
        if (Modifier.isPublic(mod))
        {
            jctor.setVisibility(JavaVisibility.PUBLIC);
        }
        else if (Modifier.isProtected(mod))
        {
            jctor.setVisibility(JavaVisibility.PROTECTED);
        }

        // get arguments
        final Type[] paramTypes = ctor.getGenericParameterTypes();
        for (int i = 0; i < paramTypes.length; ++i)
        {
            funcType.newArgument("arg" + i,
                convertType(paramTypes[i], typeVars));
        }
        if (ctor.isVarArgs())
        {
            funcType.setVarArgs(true);
        }

        // get exception types
        final Type[] exceptionTypes = ctor.getGenericExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; ++i)
        {
            funcType.addExceptionType((JavaClass) convertType(
                exceptionTypes[i], typeVars));
        }
    }

    private void buildMethod(
        JavaRawStructuredType type,
        Method method,
        Map<String, JavaTypeVariable> typeVars)
    {
        // ignore synthetic methods
        if (method.isSynthetic()) return;

        // ignore private methods
        final int mod = method.getModifiers();
        if (Modifier.isPrivate(mod)) return;

        // create schema method
        final JavaFunction func = type.newMethod(method.getName(), convertType(
            method.getGenericReturnType(), typeVars));
        final JavaFunctionType funcType = func.getType();

        // get modifiers
        if (Modifier.isPublic(mod))
        {
            func.setVisibility(JavaVisibility.PUBLIC);
        }
        else if (Modifier.isProtected(mod))
        {
            func.setVisibility(JavaVisibility.PROTECTED);
        }
        if (Modifier.isAbstract(mod))
        {
            func.addModifier(JavaFunctionModifier.ABSTRACT);
        }
        if (Modifier.isFinal(mod))
        {
            func.addModifier(JavaFunctionModifier.FINAL);
        }
        if (Modifier.isNative(mod))
        {
            func.addModifier(JavaFunctionModifier.NATIVE);
        }
        if (Modifier.isStatic(mod))
        {
            func.addModifier(JavaFunctionModifier.STATIC);
        }
        if (Modifier.isStrict(mod))
        {
            func.addModifier(JavaFunctionModifier.STRICTFP);
        }
        if (Modifier.isSynchronized(mod))
        {
            func.addModifier(JavaFunctionModifier.SYNCHRONIZED);
        }

        // get arguments
        final Type[] paramTypes = method.getGenericParameterTypes();
        for (int i = 0; i < paramTypes.length; ++i)
        {
            funcType.newArgument("arg" + i,
                convertType(paramTypes[i], typeVars));
        }
        if (method.isVarArgs())
        {
            funcType.setVarArgs(true);
        }

        // get exception types
        final Type[] exceptionTypes = method.getGenericExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; ++i)
        {
            funcType.addExceptionType((JavaClass) convertType(
                exceptionTypes[i], typeVars));
        }

        // get default value (for annotation elements)
        final Object defaultValue = method.getDefaultValue();
        if (defaultValue != null)
        {
            func.setDefaultValue(getValueForObject(defaultValue));
        }
    }

    private void addAnnotations(JavaSchemaObject obj, AnnotatedElement elem)
    {
        final Annotation[] anns = elem.getDeclaredAnnotations();
        for (final Annotation ann : anns)
        {
            final JavaAnnotation jann = buildAnnotation(ann);
            obj.addAnnotation(jann);
        }
    }

    private JavaAnnotation buildAnnotation(Annotation ann)
    {
        final Class< ? extends Annotation> annType = ann.annotationType();
        final JavaAnnotationType jannType = (JavaAnnotationType) getTypeForClass(annType);
        final JavaAnnotation jann = new JavaAnnotation(jannType);
        final Method[] methods = annType.getDeclaredMethods();
        for (final Method method : methods)
        {
            final String name = method.getName();
            try
            {
                final Object value = method.invoke(ann);
                if (value != null)
                {
                    final JavaFunction jmethod = jannType.getMethod(name);
                    final JavaAnnotationElementValue jvalue = getValueForObject(value);
                    jann.setElementValue(jmethod, jvalue);
                }
            }
            catch (Exception e)
            {
                throw new Error(
                    "Error accessing annotation value for element '" + name
                        + "' of '" + annType.getName() + "'", e);
            }
        }
        return jann;
    }

    private JavaAnnotationElementValue getValueForObject(Object o)
    {
        final Class< ? > cls = o.getClass();
        if (cls.isArray())
        {
            final Class< ? > elemType = cls.getComponentType();
            final JavaRawStructuredType jelemType = getTypeForClass(elemType);
            final JavaArrayType arrayType = getArrayType(jelemType, 1);
            final JavaAnnotationArrayInitializer arrayInit = new JavaAnnotationArrayInitializer(
                arrayType);
            final int len = Array.getLength(o);
            for (int i = 0; i < len; ++i)
            {
                final Object elem = Array.get(o, i);
                arrayInit.addElement(getValueForObject(elem));
            }
            return arrayInit;
        }
        else if (o instanceof Annotation)
        {
            return buildAnnotation((Annotation) o);
        }
        else
        {
            return getExpressionForObject(o);
        }
    }

    private JavaExpression getExpressionForObject(Object o)
    {
        final JavaExpression expr;
        if (o instanceof Double)
        {
            expr = new JavaDoubleLiteral(this, ((Double) o).doubleValue());
        }
        else if (o instanceof Float)
        {
            expr = new JavaFloatLiteral(this, ((Float) o).floatValue());
        }
        else if (o instanceof Long)
        {
            expr = new JavaLongLiteral(this, ((Long) o).longValue());
        }
        else if (o instanceof Integer || o instanceof Short
            || o instanceof Byte)
        {
            expr = new JavaLongLiteral(this, ((Number) o).intValue());
        }
        else if (o instanceof Character)
        {
            expr = new JavaCharLiteral(this, ((Character) o).charValue());
        }
        else if (o instanceof Boolean)
        {
            expr = new JavaBooleanLiteral(this, ((Boolean) o).booleanValue());
        }
        else if (o instanceof String)
        {
            expr = new JavaStringLiteral(this, o.toString());
        }
        else if (o instanceof Class)
        {
            expr = new JavaTypeLiteral(getTypeForClass((Class) o));
        }
        else if (o instanceof Enum)
        {
            final Enum< ? > e = (Enum) o;
            final JavaEnum je = (JavaEnum) getTypeForClass(e
                .getDeclaringClass());
            final JavaMemberVariable field = je.getField(e.name());
            expr = new JavaVariableReference(field);
        }
        else
        {
            throw new RuntimeException(
                "Cannot generate constant expression for object '" + o
                    + "' of type '" + o.getClass().getName() + "'");
        }
        return expr;
    }

    private JavaPackage getPackageForClass(Class cls)
    {
        JavaPackage pkg = null;
        // Class.getPackage() returns null for classes in "unnamed package"
        Package clsPkg = cls.getPackage();
        if (clsPkg != null)
        {
            String pkgName = clsPkg.getName();
            pkg = getPackage(pkgName, true);
        }
        return pkg;
    }

    private JavaType convertType(Type t, Map<String, JavaTypeVariable> typeVars)
    {
        JavaType type;
        if (t instanceof ParameterizedType)
        {
            type = convertParamType((ParameterizedType) t, typeVars);
        }
        else if (t instanceof GenericArrayType)
        {
            type = convertArrayType((GenericArrayType) t, typeVars);
        }
        else if (t instanceof TypeVariable)
        {
            type = convertTypeVar((TypeVariable) t, typeVars);
        }
        else if (t instanceof WildcardType)
        {
            type = convertWildcardType((WildcardType) t, typeVars);
        }
        else if (t instanceof Class)
        {
            type = convertClass((Class) t);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported type "
                + t.getClass().getName());
        }
        return type;
    }

    private JavaParameterizedType< ? > convertParamType(
        ParameterizedType t,
        Map<String, JavaTypeVariable> typeVars)
    {
        final Class< ? > rawCls = (Class) t.getRawType();
        final JavaRawStructuredType< ? > jrawCls = getTypeForClass(rawCls);
        final Type[] typeArgs = t.getActualTypeArguments();
        final JavaReferenceType[] jtypeArgs = new JavaReferenceType[typeArgs.length];
        for (int i = 0; i < typeArgs.length; ++i)
        {
            jtypeArgs[i] = (JavaReferenceType) convertType(typeArgs[i],
                typeVars);
        }
        return jrawCls.parameterize(jtypeArgs);
    }

    private JavaArrayType convertArrayType(
        GenericArrayType t,
        Map<String, JavaTypeVariable> typeVars)
    {
        final Type compType = t.getGenericComponentType();
        final JavaType jcompType = convertType(compType, typeVars);
        return getArrayType(jcompType, 1);
    }

    private JavaTypeVariable convertTypeVar(
        TypeVariable t,
        Map<String, JavaTypeVariable> typeVars)
    {
        final String name = t.getName();
        final JavaTypeVariable existingVar = typeVars.get(name);
        if (existingVar != null) return existingVar;
        final JavaTypeVariable var = new JavaTypeVariable(this, name);
        typeVars.put(name, var);
        for (final Type b : t.getBounds())
        {
            var.addUpperBound((JavaTypeBound) convertType(b, typeVars));
        }
        return var;
    }

    private JavaWildcardType convertWildcardType(
        WildcardType t,
        Map<String, JavaTypeVariable> typeVars)
    {
        final JavaWildcardType var = new JavaWildcardType(this);
        for (final Type b : t.getUpperBounds())
        {
            var.addUpperBound((JavaTypeBound) convertType(b, typeVars));
        }
        for (final Type b : t.getLowerBounds())
        {
            var.addLowerBound((JavaTypeBound) convertType(b, typeVars));
        }
        return var;
    }

    private JavaType convertClass(Class< ? > cls)
    {
        JavaType type;
        if (cls.isPrimitive())
        {
            type = getTypeForPrimitive(cls);
        }
        else if (cls.isArray())
        {
            JavaType elementType = convertClass(cls.getComponentType());
            int dimensions = 0;
            String name = cls.getName();
            while (name.charAt(dimensions) == '[')
                ++dimensions;
            assert (dimensions > 0);
            type = getArrayType(elementType, dimensions);
        }
        else
        {
            type = getTypeForClass(cls);
        }
        return type;
    }

    private JavaType getTypeForPrimitive(Class cls)
    {
        assert (cls.isPrimitive());
        if (primitiveTypeMap == null)
        {
            buildPrimitiveTypeMap();
        }
        return primitiveTypeMap.get(cls);
    }

    private Map<Class, JavaType> primitiveTypeMap;

    private void buildPrimitiveTypeMap()
    {
        primitiveTypeMap = new HashMap<Class, JavaType>();
        primitiveTypeMap.put(void.class, voidType);
        primitiveTypeMap.put(boolean.class, booleanType);
        primitiveTypeMap.put(byte.class, byteType);
        primitiveTypeMap.put(char.class, charType);
        primitiveTypeMap.put(short.class, shortType);
        primitiveTypeMap.put(int.class, intType);
        primitiveTypeMap.put(long.class, longType);
        primitiveTypeMap.put(float.class, floatType);
        primitiveTypeMap.put(double.class, doubleType);
    }

    public JavaArrayType getArrayType(JavaType elementType, int dimensions)
    {
        // normalize element type so that it is not an array
        while (elementType instanceof JavaArrayType)
        {
            JavaArrayType elemArrType = (JavaArrayType) elementType;
            elementType = elemArrType.getElementType();
            dimensions += elemArrType.getIndexTypes().length;
        }

        // look for type in cache first
        String id = JavaArrayType.generateID(elementType, dimensions);
        JavaArrayType type = (JavaArrayType) nameTypeCache.get(id);
        if (type != null) return type;

        // create new type
        type = new JavaArrayType(id, elementType, dimensions);

        nameTypeCache.put(id, type);
        return type;
    }

    public final boolean isStructuredType(JavaType type, String canonicalName)
    {
        if (type instanceof JavaStructuredType)
        {
            String typeName = ((JavaStructuredType< ? >) type).getName()
                .getCanonicalName();
            return typeName.equals(canonicalName);
        }
        return false;
    }

    protected final boolean isPrimitiveType(
        JavaType type,
        Class< ? > typeClass,
        String wrapperName)
    {
        return type != null
            && (typeClass.isAssignableFrom(type.getClass()) || isStructuredType(
                type, wrapperName));
    }

    public final boolean isBoolean(JavaType type)
    {
        return isPrimitiveType(type, JavaBooleanType.class, "java.lang.Boolean");
    }

    public final boolean isByte(JavaType type)
    {
        return isPrimitiveType(type, JavaByteType.class, "java.lang.Byte");
    }

    public final boolean isChar(JavaType type)
    {
        return isPrimitiveType(type, JavaCharType.class, "java.lang.Character");
    }

    public final boolean isDouble(JavaType type)
    {
        return isPrimitiveType(type, JavaDoubleType.class, "java.lang.Double");
    }

    public final boolean isFloat(JavaType type)
    {
        return isPrimitiveType(type, JavaFloatType.class, "java.lang.Float");
    }

    public final boolean isInt(JavaType type)
    {
        return isPrimitiveType(type, JavaIntType.class, "java.lang.Integer");
    }

    public final boolean isLong(JavaType type)
    {
        return isPrimitiveType(type, JavaLongType.class, "java.lang.Long");
    }

    public final boolean isShort(JavaType type)
    {
        return isPrimitiveType(type, JavaShortType.class, "java.lang.Short");
    }

    private final Set<String> integralTypes = builtIntegralTypeSet();

    protected Set<String> builtIntegralTypeSet()
    {
        Set<String> integralTypes = new HashSet<String>();
        integralTypes.add("java.lang.Byte");
        integralTypes.add("java.lang.Character");
        integralTypes.add("java.lang.Integer");
        integralTypes.add("java.lang.Long");
        integralTypes.add("java.lang.Short");
        return integralTypes;
    }

    public final boolean isIntegral(JavaType type)
    {
        if (type instanceof JavaIntegralType)
        {
            return true;
        }
        else if (type instanceof JavaRawClass)
        {
            String name = ((JavaRawClass) type).getName().getCanonicalName();
            return integralTypes.contains(name);
        }
        return false;
    }

    private final Set<String> numericTypes = builtNumericTypeSet();

    protected Set<String> builtNumericTypeSet()
    {
        Set<String> numericTypes = builtIntegralTypeSet();
        numericTypes.add("java.lang.Double");
        numericTypes.add("java.lang.Float");
        return numericTypes;
    }

    public final boolean isNumeric(JavaType type)
    {
        if (type instanceof JavaNumericType)
        {
            return true;
        }
        else if (type instanceof JavaRawClass)
        {
            String name = ((JavaRawClass) type).getName().getCanonicalName();
            return numericTypes.contains(name);
        }
        return false;
    }

    protected static class IntegralTypeInfo
    {
        public final int bits;
        public final boolean signed;
        public final boolean vector;

        public IntegralTypeInfo(int bits, boolean signed, boolean vector)
        {
            this.bits = bits;
            this.signed = signed;
            this.vector = vector;
        }
    }

    private static final IntegralTypeInfo BOOLEAN_INFO = new IntegralTypeInfo(
        1, false, false);
    private static final IntegralTypeInfo BYTE_INFO = new IntegralTypeInfo(8,
        true, false);
    private static final IntegralTypeInfo CHAR_INFO = new IntegralTypeInfo(16,
        false, false);
    private static final IntegralTypeInfo SHORT_INFO = new IntegralTypeInfo(16,
        true, false);
    private static final IntegralTypeInfo INT_INFO = new IntegralTypeInfo(32,
        true, false);
    private static final IntegralTypeInfo LONG_INFO = new IntegralTypeInfo(64,
        true, false);

    protected IntegralTypeInfo getIntegralTypeInfo(JavaType type)
    {
        if (isLong(type))
        {
            return LONG_INFO;
        }
        else if (isInt(type))
        {
            return INT_INFO;
        }
        else if (isShort(type))
        {
            return SHORT_INFO;
        }
        else if (isChar(type))
        {
            return CHAR_INFO;
        }
        else if (isByte(type))
        {
            return BYTE_INFO;
        }
        else if (isBoolean(type))
        {
            return BOOLEAN_INFO;
        }
        throw new IllegalArgumentException("Integral type expected");
    }

    public int getTypeWidth(JavaType type)
    {
        return getIntegralTypeInfo(type).bits;
    }

    protected IntegralTypeInfo getIntegralTypeInfo(
        JavaType type1,
        JavaType type2)
    {
        IntegralTypeInfo info1 = getIntegralTypeInfo(type1);
        IntegralTypeInfo info2 = getIntegralTypeInfo(type2);
        int bits;
        if (info1.bits > info2.bits)
        {
            bits = info1.bits + (!info1.signed && info2.signed ? 1 : 0);
        }
        else if (info2.bits > info1.bits)
        {
            bits = info2.bits + (!info2.signed && info1.signed ? 1 : 0);
        }
        else
        {
            bits = info1.bits + (info1.signed != info2.signed ? 1 : 0);
        }
        boolean signed = info1.signed || info2.signed;
        boolean vector = info1.vector || info2.vector;
        return new IntegralTypeInfo(bits, signed, vector);
    }

    protected JavaType getIntegralType(IntegralTypeInfo info)
    {
        int bits = info.bits;
        if (info.signed)
        {
            if (bits <= 8)
            {
                return byteType;
            }
            else if (bits <= 16)
            {
                return shortType;
            }
            else if (bits <= 32)
            {
                return intType;
            }
            else
            {
                assert (bits <= 64);
                return longType;
            }
        }
        else
        {
            if (bits == 1)
            {
                return booleanType;
            }
            else
            {
                assert (bits <= 16);
                return charType;
            }
        }
    }

    public JavaType promote(JavaType type1, JavaType type2)
    {
        IntegralTypeInfo info = getIntegralTypeInfo(type1, type2);
        return getIntegralType(info);
    }
}
