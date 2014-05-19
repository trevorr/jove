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

import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.ValueType;
import com.newisys.verilog.VerilogAbsVar;
import com.newisys.verilog.VerilogSimTime;
import com.newisys.verilog.VerilogSimulation;
import com.newisys.verilog.VerilogWriteValue;
import com.newisys.verilog.util.BitVector;
import com.newisys.verilog.util.BitVectorBuffer;
import com.newisys.verilog.util.ValueConverter;

/**
 * Schedules and combines drives for future time slices.
 * 
 * @author Trevor Robinson
 */
final class OutputScheduler
{
    // default access for efficient access by inner class
    final DVEventManager dvEventManager;
    final VerilogSimulation verilogSim;
    final String signalName;
    final VerilogWriteValue signalWrite;
    final InputMonitor inputMonitor; // null OK
    private final ClockMonitor clockMonitor;
    final EdgeSet clockEdges;
    final boolean onlyDriver;
    BitVector currentValue;

    private final CycleEventRecordFactory cycleEventFactory = new CycleEventRecordFactory();
    final DriveEventRecordFactory driveEventFactory = new DriveEventRecordFactory();

    final UniqueTimeEventScheduler<CycleEventRecord> cycleScheduler = new UniqueTimeEventScheduler<CycleEventRecord>(
        cycleEventFactory);
    long lastCycleTime = -1;

    public OutputScheduler(
        DVEventManager dvEventManager,
        VerilogSimulation verilogSim,
        String signalName,
        VerilogWriteValue signalWrite,
        boolean onlyDriver,
        InputMonitor inputMonitor, // null OK
        ClockMonitor clockMonitor,
        EdgeSet clockEdges)
    {
        this.dvEventManager = dvEventManager;
        this.verilogSim = verilogSim;
        this.signalName = signalName;
        this.signalWrite = signalWrite;
        this.onlyDriver = onlyDriver;
        this.inputMonitor = inputMonitor;
        this.clockMonitor = clockMonitor;
        this.clockEdges = clockEdges;

        // if we are caching the current value of the signal,
        // we need to read the initial value
        if (onlyDriver)
        {
            currentValue = (BitVector) signalWrite.getValue(ValueType.VECTOR);
        }

        clockMonitor.addListener(new ClockEdgeCallback());
    }

    public void drive(
        long cycles,
        long skew,
        BitVector value,
        BitVector writeMask,
        BitVector strongMask)
    {
        assert (cycles >= 0 && skew >= 0);

        synchronized (cycleScheduler)
        {
            long simTime = verilogSim.getSimTime();

            // get the current cycle/skew according to the scheduler
            long schedCycle = cycleScheduler.getCurrentTime();
            long curSkew = simTime - lastCycleTime;

            // check for an edge that scheduler has not seen yet
            long realCycle = schedCycle;
            if (lastCycleTime < simTime
                && clockMonitor.atEdge(simTime, clockEdges))
            {
                ++realCycle;
                curSkew = 0;
            }

            // calculate drive cycle/skew
            long driveCycle = realCycle + cycles;
            long driveSkew = skew;
            if (cycles == 0)
            {
                driveSkew += curSkew;
            }

            // schedule cycle/skew
            CycleEventRecord ce = cycleScheduler.getEvent(driveCycle, true);
            DriveEventRecord de = ce.getDriveEvent(driveSkew, true);
            de.mergeDrive(value, writeMask, strongMask);

            // driving in current cycle
            if (driveCycle == schedCycle)
            {
                ce.scheduleDrive(de, simTime);
            }
        }
    }

    private final class ClockEdgeCallback
        implements EdgeListener
    {
        public EdgeSet getEdges()
        {
            return clockEdges;
        }

        public int getBit()
        {
            return 0;
        }

        public void notifyEdge(VerilogSimTime time, EdgeSet edge)
        {
            synchronized (cycleScheduler)
            {
                long simTime = time.getSimTime();

                if (Debug.enabled)
                {
                    Debug.out.println("OutputScheduler.ClockEdgeCallback("
                        + ((VerilogAbsVar) signalWrite).getFullName()
                        + "): simTime = " + simTime);
                }

                lastCycleTime = simTime;

                cycleScheduler
                    .setCurrentTime(cycleScheduler.getCurrentTime() + 1);

                CycleEventRecord ce = cycleScheduler.getCurrentEvent();
                if (ce != null)
                {
                    ce.scheduleAll(simTime);
                }
            }
        }
    }

    private final class CycleEventRecord
        extends EventRecord
    {
        // default access for efficient access by inner class
        final UniqueTimeEventScheduler<DriveEventRecord> driveScheduler = new UniqueTimeEventScheduler<DriveEventRecord>(
            driveEventFactory);

        public CycleEventRecord(long time)
        {
            super(time);
        }

        public DriveEventRecord getDriveEvent(long time, boolean createNew)
        {
            return driveScheduler.getEvent(time, createNew);
        }

        public void scheduleAll(long simTime)
        {
            synchronized (cycleScheduler)
            {
                for (final DriveEventRecord de : driveScheduler.getEventList())
                {
                    scheduleDrive(de, simTime);
                }
            }
        }

        public void scheduleDrive(DriveEventRecord de, long simTime)
        {
            long eventTime = lastCycleTime + de.time;

            if (Debug.enabled)
            {
                Debug.out.println("OutputScheduler.scheduleDrive("
                    + ((VerilogAbsVar) signalWrite).getFullName()
                    + "): simTime = " + simTime + ", eventTime = " + eventTime);
            }

            if (eventTime == simTime)
            {
                de.execute();
            }
            else if (!de.scheduled)
            {
                dvEventManager.registerSimTimeCallback(new VerilogSimTime(
                    eventTime), new DriveCallbackHandler(de));
                de.scheduled = true;
            }
        }

        private final class DriveCallbackHandler
            implements DelayListener
        {
            final DriveEventRecord de;

            public DriveCallbackHandler(DriveEventRecord de)
            {
                this.de = de;
            }

            public void notifyDelay()
            {
                if (Debug.enabled)
                {
                    Debug.out.println("OutputScheduler.DriveCallbackHandler("
                        + ((VerilogAbsVar) signalWrite).getFullName()
                        + "): skew = " + de.time);
                }

                synchronized (cycleScheduler)
                {
                    driveScheduler.setCurrentTime(de.time);

                    de.execute();
                }
            }
        }
    }

    private final class CycleEventRecordFactory
        implements EventRecordFactory<CycleEventRecord>
    {
        public CycleEventRecord newEvent(long time)
        {
            return new CycleEventRecord(time);
        }
    }

    private final class DriveEventRecord
        extends EventRecord
    {
        boolean scheduled = false;
        // value may contain 0/1/X/Z
        // value & ~writeMask must be zero
        BitVector value;
        // writeMask contains only 0 or 1
        BitVector writeMask;
        // strongMask contains only 0 or 1
        // strongMask & ~writeMask must be zero
        // strongMask & value.getZMask() must be zero
        BitVector strongMask;

        public DriveEventRecord(long time)
        {
            super(time);
        }

        public void mergeDrive(
            BitVector _value,
            BitVector _writeMask,
            BitVector _strongMask)
        {
            if (value == null)
            {
                value = _value;
                writeMask = _writeMask;
                strongMask = _strongMask;
            }
            else
            {
                // build combined strong mask
                BitVector newStrongMask = strongMask.or(_strongMask);

                // build mask of overlapping non-Z writes
                BitVector conflictMask = null;
                BitVector writeMaskNoZ = writeMask.andNot(value.getZMask());
                BitVector _writeMaskNoZ = _writeMask.andNot(_value.getZMask());
                BitVector overlapMask = writeMaskNoZ.and(_writeMaskNoZ);
                if (overlapMask.isNotZero())
                {
                    // build mask of conflicting writes
                    conflictMask = value.equalsExactMask(_value).not().and(
                        overlapMask);

                    // check for conflicting strong bits
                    if (conflictMask.and(newStrongMask).isNotZero())
                    {
                        throw new DVRuntimeException(
                            "Conflicting strong drives: signal = " + signalName
                                + ", old value = " + value + ", new value = "
                                + _value + ", strong mask = " + newStrongMask);
                    }
                }

                // build combined value
                BitVectorBuffer buf = new BitVectorBuffer(value);
                buf.assignMask(_value, _writeMask.andNot(strongMask));
                if (conflictMask != null)
                {
                    buf.setX(conflictMask);
                }
                value = buf.toBitVector();

                // update masks
                writeMask = writeMask.or(_writeMask);
                strongMask = newStrongMask;
            }
        }

        public void execute()
        {
            // get old/current value of this signal
            final BitVector oldValue;
            if (onlyDriver)
            {
                // if this output scheduler is the only driver of this signal
                // (e.g. the signal is an output register in the Verilog shell)
                // then we can assume the current value is the last value we
                // drove
                assert (currentValue != null);
                oldValue = currentValue;
            }
            else if (inputMonitor != null)
            {
                // get old value from input monitor if present
                oldValue = ValueConverter.toBitVector(inputMonitor
                    .getCurrentValue());
            }
            else
            {
                // get old value from simulator
                oldValue = (BitVector) signalWrite.getValue(ValueType.VECTOR);
            }

            // merge new value into old value according to write mask
            final BitVector newValue = oldValue.assignMask(value, writeMask);

            // suppress redundant drives
            if (!newValue.equalsExact(oldValue))
            {
                if (Debug.enabled)
                {
                    Debug.out.println("DriveEvent.execute: signal = "
                        + signalName + ", value = " + value + ", writeMask = "
                        + writeMask + ", strongMask = " + strongMask
                        + ", oldValue = " + oldValue + ", newValue = "
                        + newValue);
                }

                signalWrite.putValue(newValue);
            }

            // update cached signal value
            currentValue = newValue;
        }
    }

    private final class DriveEventRecordFactory
        implements EventRecordFactory<DriveEventRecord>
    {
        public DriveEventRecord newEvent(long time)
        {
            return new DriveEventRecord(time);
        }
    }
}
