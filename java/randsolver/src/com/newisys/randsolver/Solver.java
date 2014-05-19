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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import org.sf.javabdd.BDD;

import com.newisys.langschema.constraint.ConsExpression;
import com.newisys.langschema.constraint.ConsSchema;
import com.newisys.langschema.constraint.ConsVariableReference;
import com.newisys.langschema.constraint.ConsVariableReferenceComparator;
import com.newisys.random.PRNG;
import com.newisys.randsolver.annotation.Rand;
import com.newisys.randsolver.annotation.Randc;
import com.newisys.randsolver.annotation.Randomizable;
import com.newisys.randsolver.mappers.EnumMapper;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.Length;

/**
 * The Jove random constraint solver.
 * 
 * @author Jon Nall
 *
 */
public class Solver
{
    public final static ConsSchema schema = new ConsSchema();

    // initial RandVarSet and Constraints. These are passed to the Solver in
    // its constructor. At solve-time we'll query mVars which might result in
    // more RandomVariables and Constraints being added to these lists.
    private Constraint mConstraints;
    private RandVarSet mVars;
    private static Map<ConstraintVarKey, ExprSolver> mSolutionCache = new HashMap<ConstraintVarKey, ExprSolver>();
    private static Map<Class, RandInfo> mClassToRandInfo = new WeakHashMap<Class, RandInfo>();
    private static Map<Object, RandInfo> mObjToRandInfo = new WeakIdentityMap<Object, RandInfo>();

    private static final boolean DEBUG_VARS = false;

    private class ConstraintVarKey
    {
        private Constraint c;
        private RandVarSet rvs;
        private int hashCode = 0;

        ConstraintVarKey(Constraint c, RandVarSet rvs)
        {
            this.c = Constraint.newInstance(c);
            this.rvs = rvs;

            this.hashCode = c.hashCode() * 37;
            this.hashCode += rvs.hashCode() * 37;
        }

        public int hashCode()
        {
            return hashCode;
        }

        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (!(o instanceof ConstraintVarKey)) return false;

            ConstraintVarKey key = (ConstraintVarKey) o;
            return (key.c.equals(this.c) && key.rvs.equals(this.rvs));
        }
    }

    private static RandInfo getRandInfoOrThrow(Object o)
    {
        return getRandInfo(o, false);
    }

    private static RandInfo getRandInfoOrNull(Object o)
    {
        // this version can be called with o == null.
        if (o == null)
        {
            return null;
        }
        return getRandInfo(o, true);
    }

    private static RandInfo getRandInfo(Object o, boolean returnNullOnError)
    {
        Class< ? extends Object> klass = o.getClass();

        // does this object have a specific RandInfo?
        if (mObjToRandInfo.containsKey(o))
        {
            RandInfo rInfo = mObjToRandInfo.get(o);
            assert (rInfo != null);
            return rInfo;
        }

        // does this class have RandInfo already generated?
        if (mClassToRandInfo.containsKey(klass))
        {
            RandInfo rInfo = mClassToRandInfo.get(klass);
            assert (rInfo != null);
            return rInfo;
        }

        Class< ? > curClass = klass;
        RandInfo randInfo = new RandInfo();
        Set<String> cNames = new HashSet<String>();
        while (curClass != Object.class)
        {
            Randomizable aRand = curClass.getAnnotation(Randomizable.class);
            if (aRand == null)
            {
                if (curClass == klass)
                {
                    if (returnNullOnError)
                    {
                        return null;
                    }
                    else
                    {
                        // this class has no Randomizable annotation.
                        throw new InvalidConstraintException(
                            "No Randomizable annotation for class: " + curClass);
                    }
                }
            }
            else
            {
                com.newisys.randsolver.annotation.Constraint[] aCons = aRand
                    .value();
                for (com.newisys.randsolver.annotation.Constraint c : aCons)
                {
                    String cName = c.name();
                    if (cNames.contains(cName))
                    {
                        // if a derived class has a constraint with the same
                        // name as one of its base class's constraints, the
                        // derived class's constraint is used
                        continue;
                    }
                    Constraint cons = ConstraintCompiler.compile(curClass, c
                        .expr());
                    cons.setName(cName);
                    cNames.add(cName);
                    randInfo.addConstraint(cons);
                }
            }
            curClass = curClass.getSuperclass();
        }

        // now check random variables
        curClass = klass;
        while (curClass != Object.class)
        {
            Field[] fields = curClass.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);
            StringBuffer sBuf = new StringBuffer(128);
            for (Field f : fields)
            {
                boolean rand = f.isAnnotationPresent(Rand.class);
                boolean randc = f.isAnnotationPresent(Randc.class);
                Length length = f.getAnnotation(Length.class);
                boolean randomizeField = (rand || randc);

                if (rand && randc)
                {
                    throw new InvalidRandomVarException(
                        "It is invalid to specify both Rand and Randc on variable: "
                            + f.getDeclaringClass().getCanonicalName() + "."
                            + f.getName());
                }

                if (randomizeField)
                {
                    // check that the field isn't final (unless it's
                    // Randomizable, in which case final is OK)
                    if (Modifier.isFinal(f.getModifiers()))
                    {
                        if (!f.getType()
                            .isAnnotationPresent(Randomizable.class))
                        {
                            throw new InvalidRandomVarException(
                                "Final fields cannot be randomized: "
                                    + f.toGenericString());
                        }
                    }

                    // BitVectors being randomized must be annotated with
                    // a > 0 @Length
                    if (f.getType() == BitVector.class)
                    {
                        if (length == null)
                        {
                            throw new InvalidRandomVarException(
                                "Required Length annotation missing on variable: "
                                    + f.getDeclaringClass().getCanonicalName()
                                    + "." + f.getName());
                        }
                        else if (length.value() <= 0)
                        {
                            throw new InvalidRandomVarException(
                                "Invalid Length annotation (" + length.value()
                                    + ") on variable:"
                                    + f.getDeclaringClass().getCanonicalName()
                                    + "." + f.getName()
                                    + " (Length must be > 0)");
                        }
                    }

                    if (randc)
                    {
                        sBuf.append("cyclic ");
                    }
                    sBuf.append(f.getName());
                    if (length != null)
                    {
                        sBuf.append(":" + length.value());
                    }
                    sBuf.append(";");
                }
            }
            if (sBuf.length() > 0)
            {
                randInfo.addRandVars(new RandVarSet(curClass, sBuf.toString()));
            }
            curClass = curClass.getSuperclass();
        }

        mClassToRandInfo.put(klass, randInfo);
        assert (randInfo != null);
        return randInfo;
    }

    /**
     * Returns a random instance of the specified enumeration. This randomization
     * will respect any {@link com.newisys.randsolver.annotation.RandExclude
     * RandExclude} annotations included in the enumeration.
     *
     * @param <E> the enumeration type to randomize
     * @param enumType the enumeration type to randomize
     * @param randomStream the {@link PRNG} to use when randomizing
     * @return a random instance of type <code>enumType</code>
     */
    public static <E extends Enum<E>> E randomizeEnumType(
        Class<E> enumType,
        PRNG randomStream)
    {
        EnumMapper<E> mapper = (EnumMapper<E>) RandomMapperRegistry
            .getMapper(enumType);
        int idx = randomStream.nextInt(mapper.size());
        return mapper.getObject(idx);
    }

    public static void randomize(Object obj, PRNG randomStream)
    {
        RandInfo rInfo = getRandInfoOrThrow(obj);
        Solver s = new Solver(rInfo);
        s.execute(obj, rInfo, randomStream);
    }

    public Solver(RandInfo randInfo)
    {
        List constraints = randInfo.getConstraints();
        int numConstraints = constraints.size();
        if (numConstraints > 0)
        {
            Constraint c = Constraint.newInstance((Constraint) constraints
                .get(0));
            for (int i = 1; i < numConstraints; ++i)
            {
                c.and((Constraint) constraints.get(i));
            }
            mConstraints = c;
        }
        else
        {
            mConstraints = new Constraint();
        }

        mVars = randInfo.getRandVars();
    }

    public void execute(Object obj, RandInfo rInfo, PRNG randomStream)
    {
        try
        {
            solveIt(obj, rInfo, randomStream);
        }
        catch (IllegalAccessException e)
        {
            throw new InvalidConstraintException(e.getMessage());
        }
    }

    /**
     * Returns whether or not the specified random variable will be randomized
     * when the specified object is randomized. If the variable will be
     * randomized, <code>true</code> is returned. Otherwise <code>false</code>
     * is returned.
     *
     * @param o the object containing the random variable
     * @param varName the name of the variable to check
     * @return <code>true</code> if the variable is enabled, <code>false</code>
     *      otherwise
     */
    public static boolean isRandEnabled(Object o, String varName)
    {
        RandVarSet varSet = getRandInfoOrThrow(o).getRandVars();
        Iterator iter = varSet.iterator();
        while (iter.hasNext())
        {
            RandomVariable rv = (RandomVariable) iter.next();
            if (varName.equals(rv.getField().getName()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Enables all random variables for the specified Object instance. All
     * variables in the Object instance annotated with either
     * {@link Rand} or {@link Randc} will be enabled.
     *
     * @param o the Object instance in which to enable all random variables
     */
    public static void enableAllRand(Object o)
    {
        RandInfo info = getRandInfoOrThrow(o);
        RandInfo classInfo = mClassToRandInfo.get(o);
        assert (classInfo != null);

        RandInfo newInfo = new RandInfo();
        newInfo.addAllConstraints(info.getConstraints());
        newInfo.addRandVars(classInfo.getRandVars());
        mObjToRandInfo.put(o, newInfo);
    }

    /**
     * Enables the specified random variable for the specified Object instance. It
     * is harmless to call this method if the variable is already enabled.
     * The method returns whether or not the variable was enabled prior to
     * the method being called.
     *
     * @param o the Object instance in which to enable the random variable
     * @param varName a String containing the name of the variable to enable in
     *      <code>o</code>
     * @return <code>true</code> if the variable was enabled prior to enableRand
     *      being called, <code>false</code> otherwise.
     * @throws InvalidRandomVarException if <code>varName</code> doesn't exist in
     *      <code>o</code>
     */
    public static boolean enableRand(Object o, String varName)
    {
        try
        {
            RandInfo rInfo = getRandInfoOrThrow(o);

            assert (mClassToRandInfo.containsKey(o.getClass()));
            RandInfo classInfo = mClassToRandInfo.get(o.getClass());

            Field f = o.getClass().getDeclaredField(varName);
            f.setAccessible(true);
            RandVarSet newVars = new RandVarSet();
            newVars.addAll(rInfo.getRandVars());
            newVars.addRandVar(classInfo.getRandVars().getVarFromField(f));

            if (rInfo.getRandVars().size() == newVars.size())
            {
                return true;
            }

            RandInfo newInfo = new RandInfo();
            newInfo.addRandVars(newVars);
            newInfo.addAllConstraints(rInfo.getConstraints());
            mObjToRandInfo.put(o, newInfo);
            return false;
        }
        catch (NoSuchFieldException e)
        {
            throw new InvalidRandomVarException("No such variable: " + varName);
        }
        catch (InvalidConstraintException e1)
        {
            throw new InvalidRandomVarException("Object [" + o
                + "] is not randomizable");
        }
    }

    /**
     * Disables all random variables for the specified Object instance. All
     * variables in the Object instance annotated with either
     * {@link Rand} or {@link Randc} will be disabled.
     *
     * @param o the Object instance in which to disable all random variables
     */
    public static void disableAllRand(Object o)
    {
        RandInfo info = getRandInfoOrThrow(o);
        RandInfo newInfo = new RandInfo();
        newInfo.addAllConstraints(info.getConstraints());
        mObjToRandInfo.put(o, newInfo);
    }

    /**
     * Disables the specified random variable for the specified Object instance.
     * It is harmless to call this method if the variable is already disabled.
     * The method returns whether or not the variable was disabled prior to
     * the method being called.
     *
     * @param o the Object instance in which to disable the random variable
     * @param varName a String containing the name of the variable to disable in
     *      <code>o</code>
     * @return <code>true</code> if the variable was disabled prior to disableRand
     *      being called, <code>false</code> otherwise.
     * @throws InvalidRandomVarException if <code>varName</code> doesn't exist in
     *      <code>o</code>
     */
    public static boolean disableRand(Object o, String varName)
    {
        try
        {
            RandInfo rInfo = getRandInfoOrThrow(o);
            Field f = o.getClass().getDeclaredField(varName);
            f.setAccessible(true);
            RandVarSet newVars = rInfo.getRandVars().removeField(f);

            if (rInfo.getRandVars().size() == newVars.size())
            {
                return true;
            }

            RandInfo newInfo = new RandInfo();
            newInfo.addRandVars(newVars);
            newInfo.addAllConstraints(rInfo.getConstraints());
            mObjToRandInfo.put(o, newInfo);
            return false;
        }
        catch (NoSuchFieldException e)
        {
            throw new InvalidRandomVarException("No such variable: " + varName);
        }
        catch (InvalidConstraintException e1)
        {
            throw new InvalidRandomVarException("Object [" + o
                + "] is not randomizable");
        }
    }

    /**
     * Returns whether or not the specified constraint will be used
     * when the specified object is randomized. If the constraint will be
     * used, <code>true</code> is returned. Otherwise <code>false</code>
     * is returned.
     *
     * @param o the object containing the constraint
     * @param constraintName the name of the constraint to check
     * @return <code>true</code> if the constraint is enabled, <code>false</code>
     *      otherwise
     */
    public static boolean isConstraintEnabled(Object o, String constraintName)
    {
        List<Constraint> constraints = getRandInfoOrThrow(o).getConstraints();
        for (Constraint c : constraints)
        {
            if (constraintName.equals(c.getName()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Enables all constraints for the specified Object instance. All
     * constraints on the Object instance annotated with
     * {@link com.newisys.randsolver.annotation.Constraint Constraint}
     * will be enabled.
     *
     * @param o the Object instance in which to enable all constraints
     */
    public static void enableAllConstraints(Object o)
    {
        RandInfo info = getRandInfoOrThrow(o);
        RandInfo classInfo = mClassToRandInfo.get(o.getClass());
        assert (classInfo != null);

        RandInfo newInfo = new RandInfo();
        newInfo.addRandVars(info.getRandVars());
        newInfo.addAllConstraints(classInfo.getConstraints());
        mObjToRandInfo.put(o, newInfo);
    }

    /**
     * Enables the specified constraint for the specified Object instance. It
     * is harmless to call this method if the constraint is already enabled.
     * The method returns whether or not the constraint was enabled prior to
     * the method being called.
     *
     * To use this API, classes should specify names in their
     * {@link com.newisys.randsolver.annotation.Constraint Constraint} annotations.
     *
     * @param o the Object instance in which to enable the constraint
     * @param constraintName a String containing the name of the constraint to enable in
     *      <code>o</code>
     * @return <code>true</code> if the constraint was enabled prior to
     *      enableConstraint being called, <code>false</code> otherwise.
     * @throws InvalidConstraintException if constraintName doesn't exist in
     *      <code>o</code>
     */
    public static boolean enableConstraint(Object o, String constraintName)
    {
        RandInfo rInfo = getRandInfoOrThrow(o);

        RandInfo classInfo = mClassToRandInfo.get(o.getClass());
        assert (classInfo != null);
        List<Constraint> constraints = new LinkedList<Constraint>(rInfo
            .getConstraints());

        // check if this constraint is already enabled
        for (Object c : constraints)
        {
            Constraint cons = (Constraint) c;
            if (constraintName.equals(cons.getName()))
            {
                return true;
            }
        }

        for (Object c : classInfo.getConstraints())
        {
            Constraint cons = (Constraint) c;
            if (constraintName.equals(cons.getName()))
            {
                constraints.add(cons);
                break;
            }
        }

        RandInfo newInfo = new RandInfo();
        newInfo.addRandVars(rInfo.getRandVars());
        newInfo.addAllConstraints(constraints);
        mObjToRandInfo.put(o, newInfo);
        return false;
    }

    /**
     * Disables all constraints for the specified Object instance. All
     * constraints on the Object instance annotated with
     * {@link com.newisys.randsolver.annotation.Constraint Constraint}
     * will be disabled.
     *
     * @param o the Object instance in which to disable all constraints
     */
    public static void disableAllConstraints(Object o)
    {
        RandInfo info = getRandInfoOrThrow(o);
        RandInfo newInfo = new RandInfo();
        newInfo.addRandVars(info.getRandVars());
        mObjToRandInfo.put(o, newInfo);
    }

    /**
     * Disables the specified constraint for the specified Object instance. It
     * is harmless to call this method if the constraint is already disabled.
     * The method returns whether or not the constraint was disabled prior to
     * the method being called.
     *
     * To use this API, classes should specify names in their
     * {@link com.newisys.randsolver.annotation.Constraint Constraint} annotations.
     *
     * @param o the Object instance in which to disable the constraint
     * @param constraintName a String containing the name of the constraint to disable in
     *      <code>o</code>
     * @return <code>true</code> if the constraint was disabled prior to
     *      disableConstraint being called, <code>false</code> otherwise.
     * @throws InvalidConstraintException if constraintName doesn't exist in
     *      <code>o</code>
     */
    public static boolean disableConstraint(Object o, String constraintName)
    {
        RandInfo rInfo = getRandInfoOrThrow(o);
        List<Constraint> constraints = new LinkedList<Constraint>();
        List oldConstraints = rInfo.getConstraints();

        for (Object c : oldConstraints)
        {
            Constraint cons = (Constraint) c;
            if (constraintName.equals(cons.getName()))
            {
                continue;
            }
            constraints.add(cons);
        }

        if (constraints.size() == oldConstraints.size())
        {
            return true;
        }

        RandInfo newInfo = new RandInfo();
        newInfo.addRandVars(rInfo.getRandVars());
        newInfo.addAllConstraints(constraints);
        mObjToRandInfo.put(o, newInfo);
        return false;
    }

    private void solveIt(Object obj, RandInfo rInfo, PRNG randomStream)
        throws IllegalAccessException
    {
        RandVarSet topLevelRandVars = mConstraints.getVarSet();
        RandVarSet newVarSet = new RandVarSet();
        queryRandomizable(obj, rInfo, new LinkedList<Field>(), newVarSet);
        mVars = newVarSet;

        RandVarSet unconstrainedVars = mVars.removeAll(topLevelRandVars);
        unconstrainedVars.removeRandomizables(); // remove non-primitives

        RandVarSet unrandomizedVars = mConstraints.getVarSet().removeAll(mVars);
        unrandomizedVars.removeRandomizables(); // remove non-primitives

        // look at all variables being randomized and call the preRandomize()
        // method of any RandomHook's we find. This must be called before
        // we replace unrandomized variables with constants as the pre_randomize
        // might change those variables.
        executeRandomizeHooks(obj, true);

        //////////////////////////////////////////////////////////////
        // AT THIS POINT WE KNOW EVERYTHING ABOUT THE RANDOMIZATION //
        //////////////////////////////////////////////////////////////

        boolean needToRandomize = (mVars.size() != 0 || unconstrainedVars
            .size() != 0);

        if (needToRandomize)
        {
            // generate solvers
            ConstraintVarKey key = new ConstraintVarKey(mConstraints, mVars);
            ExprSolver exprSolver = mSolutionCache.get(key);
            boolean cached = (exprSolver != null);

            // if unrandomized variables change, we have to re-evaluate the
            // constraint. we'd like to avoid doing that so check if they've really
            // changed before pulling the trigger below.
            boolean unrandomizedVarsChanged = !cached
                || exprSolver.needsReevaluation(unrandomizedVars, obj);

            replaceVarsWithConstants(unrandomizedVars, obj);

            // replace any variables not being randomized with their current
            // values
            // removeNonRandVars(mConstraints.getConstraint(), _obj);
            // for each var we randomize, figure out if it's complex or not.
            // determineComplexity();

            // need to solve this if any of these are true:
            // 1) we've never solved this Constraint/RandVarSet before
            // 2) it contains a cyclic rand var which must be solved for each
            // time
            // 3) it contains unrandomized variables
            if (DEBUG_VARS)
            {
                System.out.println("Randomizing: " + obj.getClass());
                System.out.println("cached: " + cached);
                System.out.println("mVars: " + mVars);
                System.out.println("uVars: " + unconstrainedVars);
                System.out.println("vVars: " + unrandomizedVars);
                System.out.println("constraints: " + mConstraints);
            }

            if (!cached || mVars.containsCyclic() || unrandomizedVarsChanged)
            {
                exprSolver = new ComplexExprSolver("FIXME (complex)");
                List cyclicList = exprSolver.solve(mConstraints, mVars,
                    unconstrainedVars);
                // reset cyclic constraints back into randomizable
                Iterator rvIter = cyclicList.iterator();
                while (rvIter.hasNext())
                {
                    RandomVariable rv = (RandomVariable) rvIter.next();
                    RandVarSet rvSet = rInfo.getRandVars();
                    Iterator iter = rvSet.iterator();
                    while (iter.hasNext())
                    {
                        RandomVariable rvOrig = (RandomVariable) iter.next();
                        if (rv.equals(rvOrig))
                        {
                            rvOrig.addCyclicConstraint(BddUtils.getFactory()
                                .zero());
                        }
                    }
                }
            }
            Map cyclicMap = exprSolver.commit(obj, randomStream);

            // store cyclic constraints back into randomizable
            Iterator rvIter = cyclicMap.keySet().iterator();
            while (rvIter.hasNext())
            {
                RandomVariable rv = (RandomVariable) rvIter.next();
                BDD bdd = (BDD) cyclicMap.get(rv);
                RandVarSet rvSet = rInfo.getRandVars();
                Iterator iter = rvSet.iterator();
                while (iter.hasNext())
                {
                    RandomVariable rvOrig = (RandomVariable) iter.next();
                    if (rv.equals(rvOrig))
                    {
                        rvOrig.addCyclicConstraint(bdd);
                    }
                }
            }

            // initialize the map in the exprsolver
            exprSolver.needsReevaluation(unrandomizedVars, obj);
            mSolutionCache.put(key, exprSolver);
        }

        // look at all variables being randomized and call the preRandomize()
        // method of any RandomHooks's we find
        executeRandomizeHooks(obj, false);
    }

    /**
     * Query o's RandInfo if appropriate. TODO write more about this.
     * @param o The Object to query
     * @param rInfo The RandInfo of o
     * @param fieldStack This List functions as a representation of the member
     *            hierarchy. Upon entry to the method, fieldList contains a List
     *            of Fields describing the relative relationship of r to the
     *            object being randomized.
     * @param runningSet All new RandomVariables found during the query are
     *            added to this RandVarSet
     * @throws IllegalAccessException if reflection fails
     */
    private void queryRandomizable(
        Object o,
        RandInfo rInfo,
        List<Field> fieldStack,
        RandVarSet runningSet)
        throws IllegalAccessException
    {
        RandVarSet set = rInfo.getRandVars();
        Iterator setIter = set.iterator();
        while (setIter.hasNext())
        {
            // Make a copy of the RandomVariable since we don't want to go
            // modifying the one in the class's RandInfo
            RandomVariable var = new RandomVariable((RandomVariable) setIter
                .next());

            // If this is a Randomizable we need to recurse and get its
            // RandInfo, adding it to what we already have. Before we recurse
            // we push this field only the fieldStack to maintain the proper
            // hierarchy. Further, we throw an exception if the Randomizable is
            // null.
            Object oo = var.getField().get(o);
            RandInfo ooInfo = getRandInfoOrNull(oo);
            if (ooInfo != null)
            {
                fieldStack.add(var.getField());
                if (oo == null)
                {
                    var.setFieldList(fieldStack);
                    throw new InvalidConstraintException(
                        "Cannot randomize null object: " + var);
                }

                queryRandomizable(oo, ooInfo, fieldStack, runningSet);
            }

            // Set this RandomVariables position in the member hierarchy
            // and add it to the RandVarList
            var.setFieldList(fieldStack);
            runningSet.addRandVar(var);
        }

        // If fieldStack is not empty, we're in a subobject of the top-level
        // object being randomized. We need to get its constraints and add those
        // to the top-level constraints
        if (!fieldStack.isEmpty())
        {
            List constraints = rInfo.getConstraints();
            Iterator cIter = constraints.iterator();
            while (cIter.hasNext())
            {
                Constraint c = (Constraint) cIter.next();

                // The RandVarSet in a Constraint is relative to that constraint
                // We now set the FieldList in each RandomVariable of the
                // Constraint's RandVarSet to properly describe the member
                // hierarchy. Then we 'and' the Constraint into mConstraints
                Iterator rvIter = c.getVarSet().iterator();
                while (rvIter.hasNext())
                {
                    RandomVariable rv = (RandomVariable) rvIter.next();
                    rv.prependFieldList(fieldStack);
                }
                mConstraints.and(c);
            }

            // pop this level of hierarchy from the stack
            fieldStack.remove(fieldStack.size() - 1);
        }
        else
        {
            //            System.out.println(mConstraints);
        }
    }

    /**
     * Check _obj and then each RandomVariable in mVars to see if it implements
     * RandomHooks. If so, run RandomHooks.preRandomize() or
     * RandomHooks.postRandomize() as dictated by the _preRandomize boolean.
     * @param obj The Object being randomized
     * @param preRandomize True if we should call preRandomize() methods on
     *            RandomHooks. False if we should call postRandomize() methods
     *            on RandomHooks.
     * @throws IllegalAccessException if reflection fails
     */
    private void executeRandomizeHooks(Object obj, boolean preRandomize)
        throws IllegalAccessException
    {
        // first check the object we're randomizing
        if (obj instanceof RandomHooks)
        {
            if (preRandomize)
            {
                ((RandomHooks) obj).preRandomize();
            }
            else
            {
                ((RandomHooks) obj).postRandomize();
            }
        }

        // then iterate over random variables and look for RandomHooks objects
        Iterator varIter = mVars.iterator();
        while (varIter.hasNext())
        {
            RandomVariable rv = (RandomVariable) varIter.next();
            Field f = rv.getField();
            if (Arrays.asList(f.getType().getInterfaces()).contains(
                RandomHooks.class))
            {
                RandomHooks hooks = (RandomHooks) f.get(rv.getObjRef(obj));
                if (hooks == null)
                {
                    System.err.println("Warning: member variable "
                        + f.getName() + " of class " + obj.getClass().getName()
                        + " is null, " + "cannot "
                        + (preRandomize ? "pre" : "post") + "Randomize it");
                    continue;
                }
                if (preRandomize)
                {
                    hooks.preRandomize();
                }
                else
                {
                    hooks.postRandomize();
                }
            }
        }
    }

    /**
     * Replaces each RandomVariable in set with its respective constant. This
     * method is used to make constants of all the variables in a Constraint
     * that are not being randomized
     * @param set A RandVarSet of the variables that should be replaced by their
     *            solve-time values
     * @param obj The object we're randomizing which is used in the reflection
     *            calls
     */
    private void replaceVarsWithConstants(RandVarSet set, Object obj)
    {
        Map<ConsVariableReference, ConsExpression> replacementMap = new TreeMap<ConsVariableReference, ConsExpression>(
            ConsVariableReferenceComparator.INSTANCE);
        Iterator iter = set.iterator();
        while (iter.hasNext())
        {
            RandomVariable rv = (RandomVariable) iter.next();
            Field f = rv.getField();
            ConsVariableReference ref = rv.getVarRef();
            ConsExpression expr = null;
            try
            {
                expr = NameParser.getExpr(f.getName(), f.getDeclaringClass(),
                    null, rv.getObjRef(obj));
            }
            catch (NoSuchFieldException e)
            {
                throw new RuntimeException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
            replacementMap.put(ref, expr);
        }
        //        System.out.println("REPLACE: " + replacementMap);
        mConstraints.getSchemaConstraint().replace(replacementMap);
    }
}
