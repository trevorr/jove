/*
 * Jove Constraint-based Random Solver
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

package com.newisys.randsolver;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.newisys.langschema.constraint.ConsBitVectorLiteral;
import com.newisys.langschema.constraint.ConsBooleanLiteral;
import com.newisys.langschema.constraint.ConsCharLiteral;
import com.newisys.langschema.constraint.ConsDoubleLiteral;
import com.newisys.langschema.constraint.ConsExpression;
import com.newisys.langschema.constraint.ConsFloatLiteral;
import com.newisys.langschema.constraint.ConsIntLiteral;
import com.newisys.langschema.constraint.ConsLongLiteral;
import com.newisys.langschema.constraint.ConsSchema;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.util.BitVector;

public final class NameParser
{
    private final static boolean DEBUG = false;

    private static Field getField(Class c, String name)
        throws NoSuchFieldException
    {
        Field f = null;
        while (c != Object.class)
        {
            try
            {
                f = c.getDeclaredField(name);
                break;
            }
            catch (NoSuchFieldException e)
            {
                c = c.getSuperclass();
                if (DEBUG)
                    System.out.println(name + " not found. Trying superclass: "
                        + c);
            }
        }
        if (f == null)
        {
            throw new NoSuchFieldException(name);
        }
        return f;
    }

    private static List getField(String name, Class defaultClass)
        throws NoSuchFieldException
    {
        List<Field> fieldList = new LinkedList<Field>();
        Field field = null;
        Class klass = defaultClass;

        if (DEBUG)
            System.out.println("getClassAndField(" + name + ") [default: "
                + defaultClass + "]");
        String pieces[] = name.split("\\.");
        int curPiece = 0;

        // some checks for stuff we don't support:
        for (int i = 0; i < pieces.length; i++)
        {
            String piece = pieces[i];
            if (piece.equals("this") || piece.equals("super"))
            {
                throw new InvalidConstraintException("Use of '" + piece
                    + "' in constraints is not yet supported.");
            }
        }

        // For a single identifier, we should be an ExpressionType since
        // TypeName's aren't supported. and i don't know what the point
        // of a single identifier PackageName would be.
        if (pieces.length > 1)
        {
            StringBuffer cPath = new StringBuffer("");
            for (int i = pieces.length - 1; i >= 0; --i)
            {
                cPath.delete(0, cPath.length());
                for (int j = 0; j < i; ++j)
                {
                    cPath.append(((j == 0) ? "" : ".") + pieces[j]);
                }
                String qName = cPath.toString();

                Package pkg = Package.getPackage(qName);
                if (pkg != null)
                {
                    if (DEBUG) System.out.println("PackageName(0): " + pkg);

                    try
                    {
                        klass = Class.forName(qName + "." + pieces[i]);
                        if (DEBUG) System.out.println("TypeName(0): " + klass);

                        // Types are not supported at this time
                        assert (i < (pieces.length - 1));
                        field = getField(klass, pieces[i + 1]);
                        assert (Modifier.isStatic(field.getModifiers()));
                        fieldList.add(field);
                        if (DEBUG)
                            System.out.println("ExpressionName(0): " + field);
                        curPiece = i + 2;
                        break;
                    }
                    catch (ClassNotFoundException e)
                    {
                        klass = null;
                    }
                }
            }
        }
        else
        {
            // unqualified name -- must be a field since we don't support types.
            assert (pieces.length == 1);
            field = getField(klass, pieces[0]);
            klass = defaultClass;
            fieldList.add(field);
            curPiece = 1;
            if (DEBUG) System.out.println("ExpressionName(1): " + field);
        }

        if (field == null)
        {
            // haven't found a field yet. try all of the package prefixes we
            // know of
            List<Package> packages = new LinkedList<Package>();
            packages.add(defaultClass.getPackage());
            packages.add(Package.getPackage("java.lang"));

            Iterator pkgIter = packages.iterator();
            while (pkgIter.hasNext())
            {
                try
                {
                    Package pkg = (Package) pkgIter.next();
                    String qName = pkg.getName();
                    if (DEBUG) System.out.println("PackageName(2): " + qName);
                    klass = Class.forName(qName + "." + pieces[0]);
                    if (DEBUG) System.out.println("TypeName(2): " + klass);

                    field = getField(klass, pieces[1]);
                    assert (Modifier.isStatic(field.getModifiers()));
                    fieldList.add(field);
                    if (DEBUG)
                        System.out.println("ExpressionName(2): " + field);
                    curPiece = 2;
                    break;
                }
                catch (ClassNotFoundException e)
                {
                    klass = null;
                }
            }
        }

        // if we haven't grabbed everything yet, this is an expression of the
        // form:
        // ...member.submember...
        if (curPiece < pieces.length)
        {

            // If class is null, we may need to add another package to the
            // package list?
            if (klass == null)
            {
                klass = defaultClass;
            }
            if (DEBUG) System.out.println("Assuming default class: " + klass);
            for (int i = curPiece; i < pieces.length; ++i)
            {
                assert (klass != null);

                field = getField(klass, pieces[i]);
                klass = field.getType();
                fieldList.add(field);
            }
            if (DEBUG) System.out.println("ExpressionName(3): " + field);

        }

        assert (klass != null);
        assert (field != null);

        if (DEBUG)
            System.out.println("getClassAndField() -> Class: " + klass
                + ", Field: " + field);
        return fieldList;
    }

    private static ConsExpression getExpr(
        List fieldList,
        RandVarSet varList,
        Object obj)
        throws IllegalAccessException
    {
        ConsSchema schema = Solver.schema;
        Field field = (Field) fieldList.get(fieldList.size() - 1);
        ConsExpression returnVal = null;
        Class< ? > fType = field.getType();
        boolean makeLiteral = false;
        Object o = null;

        // Check for a final member. If so, we can replace it with a constant
        // TBD: is final sufficient?
        if (Modifier.isFinal(field.getModifiers())
            && Modifier.isStatic(field.getModifiers()) || obj != null)
        {
            makeLiteral = true;
            field.setAccessible(true);
            o = field.get(obj);
        }

        // if we're making a literal, o can't be null
        if (makeLiteral && o == null)
        {
            throw new InvalidConstraintException(
                "X/Z values are not allowed on state variables");
        }

        // Discern the type
        if (fType == boolean.class || fType == Boolean.class)
        {
            if (makeLiteral)
            {
                returnVal = new ConsBooleanLiteral(schema, ((Boolean) o)
                    .booleanValue());
            }
        }
        else if (fType == byte.class || fType == Byte.class)
        {
            if (makeLiteral)
            {
                returnVal = new ConsIntLiteral(schema, ((Byte) o).byteValue());
            }
        }
        else if (fType == char.class)
        {
            if (makeLiteral)
            {
                returnVal = new ConsCharLiteral(schema, field.getChar(null));
            }
        }
        else if (fType == double.class || fType == Double.class)
        {
            if (makeLiteral)
            {
                returnVal = new ConsDoubleLiteral(schema, ((Double) o)
                    .doubleValue());
            }
        }
        else if (fType == float.class || fType == Float.class)
        {
            if (makeLiteral)
            {
                returnVal = new ConsFloatLiteral(schema, ((Float) o)
                    .floatValue());
            }
        }
        else if (fType == int.class || fType == Integer.class)
        {
            if (makeLiteral)
            {
                returnVal = new ConsIntLiteral(schema, ((Integer) o).intValue());
            }
        }
        else if (fType == long.class | fType == Long.class)
        {
            if (makeLiteral)
            {
                returnVal = new ConsLongLiteral(schema, ((Long) o).longValue());
            }
        }
        else if (fType == short.class | fType == Short.class)
        {
            if (makeLiteral)
            {
                returnVal = new ConsIntLiteral(schema, ((Short) o).shortValue());
            }
        }
        else if (fType == BitVector.class)
        {
            if (makeLiteral)
            {
                returnVal = new ConsBitVectorLiteral(schema, (BitVector) o);
            }
        }
        else if (fType.getAnnotation(Randomizable.class) != null)
        {
            // must be a Randomizable object.
        }
        else
        {
            RandomMapper mapper = RandomMapperRegistry.getMapper(fType);
            if (mapper == null)
            {
                throw new RuntimeException("Unsupported type: " + fType);
            }
            if (makeLiteral)
            {
                int constant = mapper.getID(o);
                returnVal = new ConsIntLiteral(schema, constant);
            }
        }

        if (returnVal == null)
        {
            RandomVariable rv = new RandomVariable(fieldList, RandVarMode.NONE);
            returnVal = rv.getVarRef();
            if (varList != null)
            {
                varList.addRandVar(rv);
            }
        }
        return returnVal;

    }

    public static ConsExpression getExpr(
        String name,
        Class defaultClass,
        RandVarSet varList,
        Object obj)
        throws NoSuchFieldException, IllegalAccessException
    {
        List fieldList = null;
        ConsExpression expr = null;

        fieldList = getField(name, defaultClass);
        expr = getExpr(fieldList, varList, obj);
        return expr;
    }

    public static ConsExpression getExpr(
        String name,
        Class defaultClass,
        RandVarSet varList)
        throws NoSuchFieldException, IllegalAccessException
    {
        return getExpr(name, defaultClass, varList, null);
    }
}
