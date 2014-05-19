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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.sf.javabdd.BDD;

import com.newisys.langschema.constraint.ConsRandomizableType;
import com.newisys.langschema.constraint.ConsVariableReference;
import com.newisys.langschema.java.JavaClass;
import com.newisys.langschema.java.JavaMemberVariable;
import com.newisys.langschema.java.JavaRawClass;
import com.newisys.langschema.java.JavaType;
import com.newisys.langschema.jove.JoveBitVectorType;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.verilog.util.BitVector;

public final class RandomVariable
{
    private final Field mField;
    private final RandVarMode mMode;

    // will be the true node if non-cyclic
    private BDD mCyclicConstraint;

    // the mSchema variable reference associated with this RandomVariable
    private ConsVariableReference mVarRef;

    // true if var is of type Randomizable
    private boolean mIsRandomizableType;

    // This List represents the field hierarchy to get to this random variable
    // relative to the object being randomized.
    private List<Field> mFieldHier;

    // mNumBits may not be valid for some datatypes (BitVector, Randomizable)
    private int mNumBits;

    /**
     * Create a RandomVariable representing field and with the declared mode.
     * @param field
     * @param mode
     */
    RandomVariable(Field field, RandVarMode mode)
    {
        this(field, mode, -1);
    }

    /**
     * Create a RandomVariable representing field and with the declared mode.
     * Also, the width of this variable is specified.
     * @param field
     * @param mode
     * @param numBits
     */
    RandomVariable(Field field, RandVarMode mode, int numBits)
    {
        mField = field;
        mMode = mode;
        mNumBits = numBits;
        mField.setAccessible(true);

        // create a ConstraintVariableRef
        initializeConstraintRef();

        // reset the cyclic BDD to consist solely of the true node
        resetCyclicConstraint();

        // initialize mFieldHier
        setFieldList(null);
    }

    /**
     * Create a RandomVariable with mode from a List representing a field
     * hierarchy. The last element in fieldList represents the Field of this
     * RandomVariable
     * @param fieldList
     * @param mode
     */
    RandomVariable(List<Field> fieldList, RandVarMode mode)
    {
        this(fieldList.get(fieldList.size() - 1), mode);
        setFieldList(fieldList);
    }

    /**
     * Create a RandomVariable that is a duplicate of rv
     * @param rv the RandomVariable to which this RandomVariable will be
     *            initialized
     */
    RandomVariable(RandomVariable rv)
    {
        // TODO deep copy mCyclicConstraint
        this.mCyclicConstraint = rv.mCyclicConstraint;
        this.mField = rv.mField;
        this.mIsRandomizableType = rv.mIsRandomizableType;
        this.mMode = rv.mMode;
        this.mNumBits = rv.mNumBits;
        this.mVarRef = rv.mVarRef;
        this.setFieldList(rv.mFieldHier);
    }

    public int getNumBits()
    {
        return mNumBits;
    }

    public Field getField()
    {
        return mField;
    }

    public Class getClassType()
    {
        return mField.getType();
    }

    public RandVarMode getMode()
    {
        return mMode;
    }

    public boolean isRandomizableType()
    {
        return mIsRandomizableType;
    }

    public void setFieldList(List<Field> l)
    {
        mFieldHier = new LinkedList<Field>();
        if (l == null) return;

        for (final Field f : l)
        {
            if (f.equals(mField)) continue;

            f.setAccessible(true);
            mFieldHier.add(f);
        }
    }

    public void prependFieldList(List<Field> l)
    {
        ListIterator<Field> fieldIter = l.listIterator(l.size());
        while (fieldIter.hasPrevious())
        {
            Field f = fieldIter.previous();
            assert (!f.equals(mField));

            f.setAccessible(true);
            mFieldHier.add(0, f);
        }
    }

    public ConsVariableReference getVarRef()
    {
        return mVarRef;
    }

    public Object getObjRef(Object _o)
        throws IllegalAccessException
    {
        if (mFieldHier.isEmpty())
        {
            return _o;
        }
        else
        {
            for (final Field f : mFieldHier)
            {
                // TODO: implement arrays
                assert (!_o.getClass().isArray());
                f.setAccessible(true);
                _o = f.get(_o);
            }

            return _o;
        }
    }

    public BDD getCyclicConstraint()
    {
        return mCyclicConstraint;
    }

    private void resetCyclicConstraint()
    {
        // System.out.println("--> CYCLIC RESET [" + mField + "]");
        mCyclicConstraint = BddUtils.getFactory().one();
    }

    public void addCyclicConstraint(BDD constraint)
    {
        mCyclicConstraint = mCyclicConstraint.and(constraint);
        // if we've exhausted our state space, restart.
        if (mCyclicConstraint.calcWeights() == 0.0)
        {
            resetCyclicConstraint();
        }
    }

    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof RandomVariable)) return false;

        // don't compare mode
        RandomVariable rv = (RandomVariable) obj;
        boolean equals = false;
        equals = this.mField.equals(rv.mField)
            && this.mFieldHier.equals(rv.mFieldHier);

        return equals;
    }

    public int hashCode()
    {
        int hashCode = mField.hashCode();
        hashCode ^= mFieldHier.hashCode();

        return hashCode;
    }

    public String toString()
    {
        return getFullyQualifiedName();
    }

    private String getFullyQualifiedName()
    {
        StringBuffer fqn = new StringBuffer(mField.getType().getName());
        fqn.append(" <RANDVAR>.");
        Iterator iter = mFieldHier.iterator();
        while (iter.hasNext())
        {
            fqn.append(((Field) iter.next()).getName());
            fqn.append(".");
        }
        fqn.append(mField.getName());

        return fqn.toString();
    }

    private void initializeConstraintRef()
    {
        int numBits = 0;
        Class< ? > dataType = mField.getType();
        JavaType type = null;
        RandomMapper mapper = null;

        assert (!dataType.isArray());

        if (dataType == boolean.class || dataType == Boolean.class)
        {
            numBits = 1;
            type = Solver.schema.booleanType;
        }
        else if (dataType == byte.class || dataType == Byte.class)
        {
            numBits = Solver.schema.byteType.getWidth();
            type = Solver.schema.byteType;
        }
        else if (dataType == char.class)
        {
            numBits = Solver.schema.charType.getWidth();
            type = Solver.schema.charType;
        }
        else if (dataType == int.class || dataType == Integer.class)
        {
            numBits = Solver.schema.intType.getWidth();
            type = Solver.schema.intType;
        }
        else if (dataType == long.class || dataType == Long.class)
        {
            numBits = Solver.schema.longType.getWidth();
            type = Solver.schema.longType;
        }
        else if (dataType == short.class || dataType == Short.class)
        {
            numBits = Solver.schema.shortType.getWidth();
            type = Solver.schema.shortType;
        }
        else if (dataType == BitVector.class)
        {
            // If we don't know the length of a BitVector, assume it's 1.
            type = Solver.schema.bitVectorType;
            if (mNumBits == -1)
            {
                mNumBits = 1;
            }
            else
            {
                type = new JoveBitVectorType((JavaClass) type, mNumBits);
            }
        }
        else if (Enum.class.isAssignableFrom(dataType))
        {
            mapper = RandomMapperRegistry.getMapper(dataType);
            assert (mapper != null);

            numBits = mapper.getConstraint().getMinWidth();
            assert (numBits > 0);
            type = new ConsRandomizableType(Solver.schema, dataType, numBits);
        }
        else if ((mapper = RandomMapperRegistry.getMapper(dataType)) != null)
        {
            numBits = mapper.getConstraint().getMinWidth();
            type = new ConsRandomizableType(Solver.schema, dataType, numBits);
        }
        else if (dataType.getAnnotation(Randomizable.class) != null)
        {
            mIsRandomizableType = true;
        }
        else
        {
            throw new InvalidConstraintException("Class is not supported as a "
                + "random type: " + dataType);
        }
        if (mNumBits == -1)
        {
            mNumBits = numBits;
        }

        try
        {
            Class klass = mField.getDeclaringClass();
            JavaRawClass jKlass = (JavaRawClass) Solver.schema
                .getTypeForClass(klass.getName());
            if (mIsRandomizableType)
            {
                type = Solver.schema.getTypeForClass(dataType.getName());
            }
            assert (type != null);
            JavaMemberVariable member = jKlass.newField(mField.getName(), type);
            mVarRef = new ConsVariableReference(Solver.schema, member);
        }
        catch (ClassNotFoundException e)
        {
            throw new InvalidConstraintException(e);
        }
    }
}
