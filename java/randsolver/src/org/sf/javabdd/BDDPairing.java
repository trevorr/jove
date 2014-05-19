/*
 * JavaBDD (http://javabdd.sourceforge.net/)
 * Copyright (C) 2003 John Whaley <jwhaley@alum.mit.edu>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/*
 * Modified by Jon Nall 04-Nov-2004
 */

package org.sf.javabdd;

/**
 * Encodes a table of variable pairs. This is used for replacing variables in a
 * BDD.
 *
 * @author John Whaley
 */
public abstract class BDDPairing
{

    /**
     * Adds the pair (oldvar, newvar) to this table of pairs. This results in
     * oldvar being substituted with newvar in a call to BDD.replace().
     *
     * Compare to bdd_setpair.
     */
    public abstract void set(int oldvar, int newvar);

    /**
     * Like set(), but with a whole list of pairs.
     *
     * Compare to bdd_setpairs.
     */
    public void set(int[] oldvar, int[] newvar)
    {
        if (oldvar.length != newvar.length) throw new BDDException();

        for (int n = 0; n < oldvar.length; n++)
            this.set(oldvar[n], newvar[n]);
    }

    /**
     * Adds the pair (oldvar, newvar) to this table of pairs. This results in
     * oldvar being substituted with newvar in a call to bdd.replace(). The
     * variable oldvar is substituted with the BDD newvar. The possibility to
     * substitute with any BDD as newvar is utilized in BDD.compose(), whereas
     * only the topmost variable in the BDD is used in BDD.replace().
     *
     * Compare to bdd_setbddpair.
     */
    public abstract void set(int oldvar, BDD newvar);

    /**
     * Like set(), but with a whole list of pairs.
     *
     * Compare to bdd_setbddpairs.
     */
    public void set(int[] oldvar, BDD[] newvar)
    {
        if (oldvar.length != newvar.length) throw new BDDException();

        for (int n = 0; n < newvar.length; n++)
            this.set(oldvar[n], newvar[n]);
    }

    /**
     * Defines each variable in the finite domain block p1 to be paired with the
     * corresponding variable in p2.
     *
     * Compare to fdd_setpair.
     */
    public void set(BDDDomain p1, BDDDomain p2)
    {
        if (p1.varNum() != p2.varNum()) throw new BDDException();

        int[] ivar1 = p1.vars();
        int[] ivar2 = p2.vars();
        for (int n = 0; n < ivar1.length; n++)
        {
            this.set(ivar1[n], ivar2[n]);
        }
    }

    /**
     * Like set(), but with a whole list of pairs.
     *
     * Compare to fdd_setpairs.
     */
    public void set(BDDDomain[] p1, BDDDomain[] p2)
    {
        if (p1.length != p2.length) throw new BDDException();

        for (int n = 0; n < p1.length; n++)
            if (p1[n].varNum() != p2[n].varNum()) throw new BDDException();

        for (int n = 0; n < p1.length; n++)
        {
            this.set(p1[n], p2[n]);
        }
    }

    /**
     * Resets this table of pairs by setting all substitutions to their default
     * values (that is, no change).
     *
     * Compare to bdd_resetpair.
     */
    public abstract void reset();

}
