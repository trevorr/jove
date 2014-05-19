/*
 * Jove - The Open Verification Environment for the Java (TM) Platform
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

package com.newisys.dv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.VerilogReadValue;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.util.Bit;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.ValueConverter;

/**
 * Base implementation for signal monitors, such as input and clock monitors.
 * 
 * @author Trevor Robinson
 */
abstract class BaseMonitor
    implements ValueChangeMonitor, EdgeMonitor
{
    protected final String signalName;
    protected final VerilogReadValue signalRead;
    protected final int signalSize;
    private final LinkedList<Object> listeners = new LinkedList<Object>();

    public BaseMonitor(String signalName, VerilogReadValue signalRead)
    {
        // check arguments
        assert (signalName != null);
        assert (signalRead != null);

        // store attributes from constructor arguments
        this.signalName = signalName;
        this.signalRead = signalRead;

        // get signal size
        signalSize = signalRead.getSize();
    }

    public BaseMonitor(BaseMonitor baseMonitor)
    {
        this(baseMonitor.signalName, baseMonitor.signalRead);
    }

    public final String getSignalName()
    {
        return signalName;
    }

    public final VerilogReadValue getSignalRead()
    {
        return signalRead;
    }

    public final int getSignalSize()
    {
        return signalSize;
    }

    public final void addListener(EdgeListener l)
    {
        assert (l != null);

        synchronized (listeners)
        {
            listeners.add(l);
        }
    }

    public final void removeListener(EdgeListener l)
    {
        assert (l != null);

        synchronized (listeners)
        {
            listeners.remove(l);
        }
    }

    public final void addListener(ValueChangeListener l)
    {
        assert (l != null);

        synchronized (listeners)
        {
            listeners.add(l);
        }
    }

    public final void removeListener(ValueChangeListener l)
    {
        assert (l != null);

        synchronized (listeners)
        {
            listeners.remove(l);
        }
    }

    protected static EdgeSet getTransitionEdge(
        BitVector oldValue,
        BitVector newValue,
        int bit)
    {
        final Bit oldBit = oldValue.getBit(bit);
        final Bit newBit = newValue.getBit(bit);
        final EdgeSet edge = EdgeSet.getTransitionEdge(oldBit, newBit);
        return edge;
    }

    protected static EdgeSet getTransitionEdge(
        Object oldValue,
        Object newValue,
        int bit)
    {
        final Bit oldBit = ValueUtil.getBit(oldValue, bit);
        final Bit newBit = ValueUtil.getBit(newValue, bit);
        final EdgeSet edge = EdgeSet.getTransitionEdge(oldBit, newBit);
        return edge;
    }

    protected static boolean isChanged(Object oldValue, Object newValue)
    {
        if (oldValue instanceof BitVector && newValue instanceof BitVector)
        {
            return !((BitVector) oldValue).equalsExact((BitVector) newValue);
        }
        return !oldValue.equals(newValue);
    }

    private BitVector zeroValue = null;

    protected final boolean isChangedInMask(
        BitVector oldValue,
        BitVector newValue,
        BitVector mask)
    {
        if (mask != null)
        {
            if (zeroValue == null)
            {
                zeroValue = new BitVector(signalSize, Bit.ZERO);
            }
            final BitVector oldMasked = zeroValue.assignMask(oldValue, mask);
            final BitVector newMasked = zeroValue.assignMask(newValue, mask);
            return !oldMasked.equalsExact(newMasked);
        }
        else
        {
            return !oldValue.equalsExact(newValue);
        }
    }

    protected final boolean isChangedInMask(
        Object oldValue,
        Object newValue,
        BitVector mask)
    {
        final BitVector oldVector = ValueConverter.toBitVector(oldValue);
        final BitVector newVector = ValueConverter.toBitVector(newValue);
        return isChangedInMask(oldVector, newVector, mask);
    }

    protected final void notifyListeners(
        VerilogSimTime simTimeObj,
        Object oldValue,
        Object newValue)
    {
        assert (simTimeObj != null);
        assert (oldValue != null);
        assert (newValue != null);

        // do nothing if value has not actually changed
        if (!isChanged(oldValue, newValue)) return;

        // copy the listener list since listeners may be added or removed
        // during the notification iteration; as an optimization, if there are
        // currently no listeners, do not make a new list
        Collection<Object> curListeners = null;
        synchronized (listeners)
        {
            if (!listeners.isEmpty())
            {
                curListeners = new ArrayList<Object>(listeners);
            }
        }

        if (curListeners != null)
        {
            Iterator<Object> iter = curListeners.iterator();
            while (iter.hasNext())
            {
                Object l = iter.next();
                if (l instanceof ValueChangeListener)
                {
                    ValueChangeListener vcl = (ValueChangeListener) l;
                    BitVector mask = vcl.getMask();
                    if (mask == null
                        || isChangedInMask(oldValue, newValue, mask))
                    {
                        vcl.notifyChange(simTimeObj, newValue);
                    }
                }
                else
                {
                    assert (l instanceof EdgeListener);
                    EdgeListener el = (EdgeListener) l;
                    int bit = el.getBit();
                    EdgeSet edge = getTransitionEdge(oldValue, newValue, bit);
                    if (!edge.isEmpty() && el.getEdges().contains(edge))
                    {
                        el.notifyEdge(simTimeObj, edge);
                    }
                }
            }
        }
    }
}
