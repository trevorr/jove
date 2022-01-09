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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.newisys.langschema.constraint.ConsVariableReference;
import com.newisys.langschema.constraint.ConsVariableReferenceComparator;

public final class RandVarSet
{
    private final static Pattern mRandVarPattern = Pattern
        .compile("^(\\S+?)(:([0-9]+))?;?$");
    // Random patterns include cyclic for now e.g.
    // foo, bar, baz // randomize foo, bar, and baz
    // foo, cyclic bar, baz // as above, but bar is cyclic

    private final Set mRandVars;
    private final Map mRandVarToIdx;
    private int mCurIndex;
    private static Class curClass;

    /**
     * Constructs a RandVarSet without any entries
     */
    public RandVarSet()
    {
        mCurIndex = 0;
        mRandVars = new HashSet();
        mRandVarToIdx = new HashMap();
    }

    /**
     * Constructs a RandVarSet containing the entries described by _str where
     * _str is of the form: [modifier] <varname>[: <numbits>] and modifier if
     * present is: cyclic _class is the class containing the variables to be
     * randomized
     * @param klass
     * @param str
     */
    public RandVarSet(Class klass, String str)
    {
        mCurIndex = 0;
        mRandVars = new HashSet();
        mRandVarToIdx = new HashMap();

        if (klass == curClass)
        {
            return;
        }
        // pull this class into the schema
        try
        {
            Solver.schema.getTypeForClass(klass.getName());
        }
        catch (ClassNotFoundException e)
        {
            throw new InvalidConstraintException(e);
        }

        // get rid of leading whitespace and convert all multispaces to 1 space
        // each
        str = str.replaceAll("\\s+", " ").trim();
        String[] vars = str.split("[,;]");

        for (int i = 0; i < vars.length; i++)
        {
            addRandVar(makeRandVar(klass, vars[i]));
        }
    }

    /**
     * Returns the number of RandomVariables in this RandVarSet
     * @return
     */
    public int size()
    {
        return mRandVars.size();
    }

    /**
     * Returns the iterator for this RandVarSet
     * @return
     */
    public Iterator iterator()
    {
        return mRandVars.iterator();
    }

    /**
     * Add _rv to this RandVarSet
     * @param rv
     */
    public void addRandVar(RandomVariable rv)
    {
        int size = mRandVars.size();
        mRandVars.add(rv);

        // only create an index if we actually added something to the set
        if (size != mRandVars.size())
        {
            mRandVarToIdx.put(rv, Integer.valueOf(mCurIndex));
            ++mCurIndex;
        }
    }

    /**
     * Add all of the RandomVariables in _set to this RandVarSet
     * @param set
     */
    public void addAll(RandVarSet set)
    {
        Iterator iter = set.mRandVars.iterator();
        while (iter.hasNext())
        {
            RandomVariable rv = (RandomVariable) iter.next();
            addRandVar(rv);
        }
    }

    /**
     * Returns a copy of this RandVarSet with the RandomVariables contained ni
     * _rv removed.
     * @param rv
     * @return
     */
    public RandVarSet removeAll(RandVarSet rv)
    {
        RandVarSet newRvs = new RandVarSet();
        newRvs.mRandVars.addAll(this.mRandVars);
        newRvs.mRandVars.removeAll(rv.mRandVars);
        return newRvs;
    }

    /**
     * Removes any RandomVariables with the specified Field from this RandVarSet.
     * A new RandVarSet is returned which is identical to this RandVarSet
     * with the exception of RandomVariables matching the specified Field are
     * not included.
     *
     * @param f the Field to check
     */
    RandVarSet removeField(Field f)
    {
        RandVarSet newSet = new RandVarSet();
        newSet.addAll(this);

        HashSet removeSet = new HashSet();
        Iterator iter = newSet.mRandVars.iterator();
        while (iter.hasNext())
        {
            RandomVariable rv = (RandomVariable) iter.next();
            if (rv.getField().equals(f))
            {
                newSet.mRandVarToIdx.remove(rv);
                removeSet.add(rv);
            }
        }
        newSet.mRandVars.removeAll(removeSet);
        newSet.resetIndices();

        return newSet;
    }

    RandomVariable getVarFromField(Field f)
    {
        Iterator iter = mRandVars.iterator();
        while (iter.hasNext())
        {
            RandomVariable rv = (RandomVariable) iter.next();
            if (rv.getField().equals(f))
            {
                return rv;
            }
        }
        return null;
    }

    /**
     * Removes all RandomVariables representing Randomizable types from this
     * RandVarList
     */
    public void removeRandomizables()
    {
        HashSet removeSet = new HashSet();
        Iterator iter = mRandVars.iterator();
        while (iter.hasNext())
        {
            RandomVariable rv = (RandomVariable) iter.next();
            if (rv.isRandomizableType())
            {
                mRandVarToIdx.remove(rv);
                removeSet.add(rv);
            }
        }
        mRandVars.removeAll(removeSet);
        resetIndices();
    }

    private void resetIndices()
    {
        Iterator iter = mRandVars.iterator();
        int newIdx = 0;

        while (iter.hasNext())
        {
            RandomVariable rv = (RandomVariable) iter.next();
            mRandVarToIdx.put(rv, Integer.valueOf(newIdx));
            ++newIdx;
        }
    }

    /**
     * Get the RandomVariable in this RandVarSet associated with _idx
     * @param index
     * @return
     */
    public RandomVariable get(int index)
    {
        RandomVariable rv = null;
        Iterator iter = mRandVarToIdx.keySet().iterator();
        while (iter.hasNext())
        {
            rv = (RandomVariable) iter.next();
            Integer idx = (Integer) mRandVarToIdx.get(rv);
            if (idx.intValue() == index)
            {
                break;
            }
        }

        return rv;
    }

    /**
     * Get the index associated with the RandomVariable in this RandVarSet
     * representing ref
     * @param ref
     * @return
     */
    public int getIndex(ConsVariableReference ref)
    {
        RandomVariable rv = get(ref);
        return getIndex(rv);
    }

    /**
     * Get the index associated with rv
     * @param rv
     * @return
     */
    public int getIndex(RandomVariable rv)
    {
        Integer idx = (Integer) mRandVarToIdx.get(rv);
        if (idx == null)
        {
            return -1;
        }
        else
        {
            return idx.intValue();
        }
    }

    /**
     * Return true if this RandVarSet contains cyclic RandomVariables
     * @return
     */
    public boolean containsCyclic()
    {
        Iterator iter = mRandVars.iterator();
        while (iter.hasNext())
        {
            RandomVariable rv = (RandomVariable) iter.next();
            if (rv.getMode() == RandVarMode.CYCLIC)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a String representation of this RandVarSet
     *
     * @return a String representation of this RandVarSet
     */
    public String toString()
    {
        StringBuffer sBuffer = new StringBuffer();
        Iterator iter = mRandVars.iterator();
        while (iter.hasNext())
        {
            sBuffer.append(iter.next());
            sBuffer.append(iter.hasNext() ? ", " : " ");
        }

        return sBuffer.toString();
    }

    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof RandVarSet)) return false;

        RandVarSet set = (RandVarSet) obj;
        if (this.size() != set.size())
        {
            return false;
        }
        else if (this.mCurIndex != set.mCurIndex)
        {
            return false;
        }
        else
        {
            return (this.mRandVarToIdx.equals(set.mRandVarToIdx) && this.mRandVars
                .equals(set.mRandVars));
        }
    }

    public int hashCode()
    {
        int hashCode = mCurIndex * 37;
        hashCode += 37 * mRandVars.hashCode();
        hashCode += 37 * mRandVarToIdx.hashCode();

        return hashCode;
    }

    private RandomVariable makeRandVar(Class klass, String s)
    {
        s = s.replaceFirst("^\\s+", "");
        RandVarMode mode = RandVarMode.NONE;

        if (s.startsWith("cyclic"))
        {
            mode = RandVarMode.CYCLIC;
            s = s.replaceFirst("cyclic\\s+", "");
        }

        s = s.replaceAll("\\s", "");
        Matcher match = mRandVarPattern.matcher(s);
        RandomVariable rv = null;
        if (match.find() == false)
        {
            throw new IllegalArgumentException(
                "Invalid RandomVariable declaration: " + s);
        }
        try
        {
            if (match.groupCount() == 3 && match.group(3) != null)
            {
                Field f = klass.getDeclaredField(match.group(1));
                int size = Integer.parseInt(match.group(3));
                rv = new RandomVariable(f, mode, size);
            }
            else
            {
                Field f = klass.getDeclaredField(match.group(1));
                rv = new RandomVariable(f, mode);
            }
        }
        catch (NoSuchFieldException e)
        {
            throw new InvalidConstraintException("Field not found in " + klass
                + ": " + e.getMessage());
        }

        return rv;
    }

    private RandomVariable get(ConsVariableReference ref)
    {
        Iterator iter = mRandVars.iterator();
        while (iter.hasNext())
        {
            RandomVariable rv = (RandomVariable) iter.next();
            if (ConsVariableReferenceComparator.INSTANCE.compare(
                rv.getVarRef(), ref) == 0)
            {
                return (rv);
            }
        }

        return null;
    }

}
