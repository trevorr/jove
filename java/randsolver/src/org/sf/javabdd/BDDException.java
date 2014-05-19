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
 * An exception caused by an invalid BDD operation.
 *
 * @author John Whaley
 */
public class BDDException
    extends RuntimeException
{
    public BDDException()
    {
        super();
    }

    public BDDException(String s)
    {
        super(s);
    }
}
