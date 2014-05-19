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
import com.newisys.verilog.util.BitRange;

/**
 * Represents an output signal (or multiple-bit output signal bus). It provides
 * methods that:
 * <ul>
 * <li>describe when the signal is driven (relative to a clock signal)</li>
 * <li>drive the value of the signal</li>
 * <li>synchronize relative to the drive edge of the clock signal</li>
 * </ul>
 * <p>
 * When driving a signal, the drive value maybe any of the following types:
 * <ul>
 * <li>BitVector</li>
 * <li>Bit</li>
 * <li>Integer</li>
 * <li>Double</li>
 * <li>String</li>
 * </ul>
 * <p>
 * Drives have several options that control how and when the signal is driven:
 * <p>
 * <b>Synchronous vs. asynchronous drives </b>
 * <p>
 * Synchronous drives occur relative to a specified edge of an associated clock
 * signal (plus the associated output skew). There are methods to perform
 * synchronous drives on the current/next clock edge, as well as an arbitrary
 * number of edges into the future. Asynchronous drives do not synchronize
 * relative to the clock, and drive their value at the current time (plus the
 * output skew).
 * <p>
 * <b>Blocking vs. non-blocking drives </b>
 * <p>
 * Blocking drives suspend the simulation thread (i.e. block the caller) until
 * the appropriate drive edge is reached. Non-blocking drives schedule the drive
 * for the appropriate edge and return immediately.
 * <p>
 * <b>Strong vs. soft drives </b>
 * <p>
 * By default, all drives are strong drives, which means that driving a signal
 * with multiple different values in the same simulation cycle results in a
 * simulation error (specifically, a DVRuntimeException). Soft drives allow such
 * drives to occur without causing a simulation error. If multiple soft drives
 * conflict (and there is no strong drive), the signal is driven to X. (For
 * multiple-bit signals, only the conflicting bits are driven to X.) If one or
 * more soft drives conflict with a strong drive, the value of the strong drive
 * is used.
 * <p>
 * <b>Range drives </b>
 * <p>
 * By default, when a multiple-bit signal bus is driven, all bits of the bus are
 * driven together. Range drives allow a specified range of bits in the bus to
 * be driven independently. Bits that are not driven in a given cycle retain
 * their current value. For the purposes of detecting drive conflicts (strong or
 * soft), each bit in the bus is considered independently (i.e. multiple drives
 * of different bits is not a conflict, whereas multiple drives of different
 * values to the same bit or bits is a conflict).
 * 
 * @author Trevor Robinson
 */
public interface OutputSignal
    extends Signal
{
    /**
     * Returns the set of clock edges this signal is driven at.
     *
     * @return the drive edge set
     */
    EdgeSet getOutputEdges();

    /**
     * Returns the skew (in simulator ticks) relative to the clock edge at which
     * this signal is driven. The skew can be any non-negative integer, though
     * the use of zero skew is not recommended for output signals. For in/out
     * signals, the skew must be postive.
     *
     * @return the drive skew
     */
    int getOutputSkew();

    // non-range, strong drives

    /**
     * Performs a strong drive of this signal, after synchronizing to the drive
     * edge of the clock, as if by a call to syncDrive().
     *
     * @param value the value to drive
     * @see OutputSignal#syncDrive()
     */
    void drive(Object value);

    /**
     * Performs a non-blocking strong drive of this signal. If the clock for
     * this signal is already at the drive edge, this method drives the value
     * relative the current edge (i.e. at current time + skew). If the clock is
     * not at the drive edge, the drive is queued until that edge (i.e. at next
     * edge time + skew).
     *
     * @param value the value to drive
     */
    void driveNB(Object value);

    /**
     * Suspends this thread until the clock for this signal reaches the drive
     * edge the given number of times, then performs a strong drive of the
     * signal. If cycles is 1, this method waits for the next drive edge. If
     * cycles is 0 and the clock is already at the drive edge, this method
     * drives the value relative the current edge (i.e. at current time + skew).
     * In other words, driveDelay(0, value) is equivalent to drive(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param value the value to drive
     */
    void driveDelay(int cycles, Object value);

    /**
     * Performs a non-blocking strong drive of this signal the given number of
     * cycles into the future. If cycles is 1, this method drives the value at
     * the next drive edge. If cycles is 0 and the clock is already at the drive
     * edge, this method drives the value relative the current edge (i.e. at
     * current time + skew). In other words, driveDelayNB(0, value) is
     * equivalent to driveNB(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param value the value to drive
     */
    void driveDelayNB(int cycles, Object value);

    /**
     * Performs an immediate strong drive of this signal, without synchronizing
     * to the drive edge of the clock.
     *
     * @param value the value to drive
     */
    void driveAsync(Object value);

    // non-range, soft drives

    /**
     * Performs a soft drive of this signal, after synchronizing to the drive
     * edge of the clock, as if by a call to syncDrive().
     *
     * @param value the value to drive
     * @see OutputSignal#syncDrive()
     */
    void driveSoft(Object value);

    /**
     * Performs a non-blocking soft drive of this signal. If the clock for this
     * signal is already at the drive edge, this method drives the value
     * relative the current edge (i.e. at current time + skew). If the clock is
     * not at the drive edge, the drive is queued until that edge (i.e. at next
     * edge time + skew).
     *
     * @param value the value to drive
     */
    void driveSoftNB(Object value);

    /**
     * Suspends this thread until the clock for this signal reaches the drive
     * edge the given number of times, then performs a soft drive of the signal.
     * If cycles is 1, this method waits for the next drive edge. If cycles is 0
     * and the clock is already at the drive edge, this method drives the value
     * relative the current edge (i.e. at current time + skew). In other words,
     * driveDelaySoft(0, value) is equivalent to driveSoft(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param value the value to drive
     */
    void driveDelaySoft(int cycles, Object value);

    /**
     * Performs a non-blocking soft drive of this signal the given number of
     * cycles into the future. If cycles is 1, this method drives the value at
     * the next drive edge. If cycles is 0 and the clock is already at the drive
     * edge, this method drives the value relative the current edge (i.e. at
     * current time + skew). In other words, driveDelaySoftNB(0, value) is
     * equivalent to driveSoftNB(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param value the value to drive
     */
    void driveDelaySoftNB(int cycles, Object value);

    /**
     * Performs an immediate soft drive of this signal, without synchronizing to
     * the drive edge of the clock.
     *
     * @param value the value to drive
     */
    void driveAsyncSoft(Object value);

    // range, non-soft drives

    /**
     * Performs a strong range drive of this signal, after synchronizing to the
     * drive edge of the clock, as if by a call to syncDrive().
     *
     * @param highBit the index of the highest bit to drive
     * @param lowBit the index of the lowest bit to drive
     * @param value the value to drive
     * @see OutputSignal#syncDrive()
     */
    void driveRange(int highBit, int lowBit, Object value);

    /**
     * Performs a strong range drive of this signal, after synchronizing to the
     * drive edge of the clock, as if by a call to syncDrive().
     *
     * @param range the range of bits to drive
     * @param value the value to drive
     * @see OutputSignal#syncDrive()
     */
    void driveRange(BitRange range, Object value);

    /**
     * Performs a non-blocking strong range drive of this signal. If the clock
     * for this signal is already at the drive edge, this method drives the
     * value relative the current edge (i.e. at current time + skew). If the
     * clock is not at the drive edge, the drive is queued until that edge (i.e.
     * at next edge time + skew).
     *
     * @param highBit the index of the highest bit to drive
     * @param lowBit the index of the lowest bit to drive
     * @param value the value to drive
     */
    void driveRangeNB(int highBit, int lowBit, Object value);

    /**
     * Performs a non-blocking strong range drive of this signal. If the clock
     * for this signal is already at the drive edge, this method drives the
     * value relative the current edge (i.e. at current time + skew). If the
     * clock is not at the drive edge, the drive is queued until that edge (i.e.
     * at next edge time + skew).
     *
     * @param range the range of bits to drive
     * @param value the value to drive
     */
    void driveRangeNB(BitRange range, Object value);

    /**
     * Suspends this thread until the clock for this signal reaches the drive
     * edge the given number of times, then performs a strong range drive of the
     * signal. If cycles is 1, this method waits for the next drive edge. If
     * cycles is 0 and the clock is already at the drive edge, this method
     * drives the value relative the current edge (i.e. at current time + skew).
     * In other words, driveRangeDelay(0, value) is equivalent to
     * driveRange(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param highBit the index of the highest bit to drive
     * @param lowBit the index of the lowest bit to drive
     * @param value the value to drive
     */
    void driveRangeDelay(int cycles, int highBit, int lowBit, Object value);

    /**
     * Suspends this thread until the clock for this signal reaches the drive
     * edge the given number of times, then performs a strong range drive of the
     * signal. If cycles is 1, this method waits for the next drive edge. If
     * cycles is 0 and the clock is already at the drive edge, this method
     * drives the value relative the current edge (i.e. at current time + skew).
     * In other words, driveRangeDelay(0, value) is equivalent to
     * driveRange(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param range the range of bits to drive
     * @param value the value to drive
     */
    void driveRangeDelay(int cycles, BitRange range, Object value);

    /**
     * Performs a non-blocking strong range drive of this signal the given
     * number of cycles into the future. If cycles is 1, this method drives the
     * value at the next drive edge. If cycles is 0 and the clock is already at
     * the drive edge, this method drives the value relative the current edge
     * (i.e. at current time + skew). In other words, driveRangeDelayNB(0,
     * value) is equivalent to driveRangeNB(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param highBit the index of the highest bit to drive
     * @param lowBit the index of the lowest bit to drive
     * @param value the value to drive
     */
    void driveRangeDelayNB(int cycles, int highBit, int lowBit, Object value);

    /**
     * Performs a non-blocking strong range drive of this signal the given
     * number of cycles into the future. If cycles is 1, this method drives the
     * value at the next drive edge. If cycles is 0 and the clock is already at
     * the drive edge, this method drives the value relative the current edge
     * (i.e. at current time + skew). In other words, driveRangeDelayNB(0,
     * value) is equivalent to driveRangeNB(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param range the range of bits to drive
     * @param value the value to drive
     */
    void driveRangeDelayNB(int cycles, BitRange range, Object value);

    /**
     * Performs an immediate strong range drive of this signal, without
     * synchronizing to the drive edge of the clock.
     *
     * @param highBit the index of the highest bit to drive
     * @param lowBit the index of the lowest bit to drive
     * @param value the value to drive
     */
    void driveRangeAsync(int highBit, int lowBit, Object value);

    /**
     * Performs an immediate strong range drive of this signal, without
     * synchronizing to the drive edge of the clock.
     *
     * @param range the range of bits to drive
     * @param value the value to drive
     */
    void driveRangeAsync(BitRange range, Object value);

    // range, soft drives

    /**
     * Performs a soft range drive of this signal, after synchronizing to the
     * drive edge of the clock, as if by a call to syncDrive().
     *
     * @param highBit the index of the highest bit to drive
     * @param lowBit the index of the lowest bit to drive
     * @param value the value to drive
     * @see OutputSignal#syncDrive()
     */
    void driveRangeSoft(int highBit, int lowBit, Object value);

    /**
     * Performs a soft range drive of this signal, after synchronizing to the
     * drive edge of the clock, as if by a call to syncDrive().
     *
     * @param range the range of bits to drive
     * @param value the value to drive
     * @see OutputSignal#syncDrive()
     */
    void driveRangeSoft(BitRange range, Object value);

    /**
     * Performs a non-blocking soft range drive of this signal. If the clock for
     * this signal is already at the drive edge, this method drives the value
     * relative the current edge (i.e. at current time + skew). If the clock is
     * not at the drive edge, the drive is queued until that edge (i.e. at next
     * edge time + skew).
     *
     * @param highBit the index of the highest bit to drive
     * @param lowBit the index of the lowest bit to drive
     * @param value the value to drive
     */
    void driveRangeSoftNB(int highBit, int lowBit, Object value);

    /**
     * Performs a non-blocking soft range drive of this signal. If the clock for
     * this signal is already at the drive edge, this method drives the value
     * relative the current edge (i.e. at current time + skew). If the clock is
     * not at the drive edge, the drive is queued until that edge (i.e. at next
     * edge time + skew).
     *
     * @param range the range of bits to drive
     * @param value the value to drive
     */
    void driveRangeSoftNB(BitRange range, Object value);

    /**
     * Suspends this thread until the clock for this signal reaches the drive
     * edge the given number of times, then performs a soft range drive of the
     * signal. If cycles is 1, this method waits for the next drive edge. If
     * cycles is 0 and the clock is already at the drive edge, this method
     * drives the value relative the current edge (i.e. at current time + skew).
     * In other words, driveRangeDelaySoft(0, value) is equivalent to
     * driveRangeSoft(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param highBit the index of the highest bit to drive
     * @param lowBit the index of the lowest bit to drive
     * @param value the value to drive
     */
    void driveRangeDelaySoft(int cycles, int highBit, int lowBit, Object value);

    /**
     * Suspends this thread until the clock for this signal reaches the drive
     * edge the given number of times, then performs a soft range drive of the
     * signal. If cycles is 1, this method waits for the next drive edge. If
     * cycles is 0 and the clock is already at the drive edge, this method
     * drives the value relative the current edge (i.e. at current time + skew).
     * In other words, driveRangeDelaySoft(0, value) is equivalent to
     * driveRangeSoft(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param range the range of bits to drive
     * @param value the value to drive
     */
    void driveRangeDelaySoft(int cycles, BitRange range, Object value);

    /**
     * Performs a non-blocking soft range drive of this signal the given number
     * of cycles into the future. If cycles is 1, this method drives the value
     * at the next drive edge. If cycles is 0 and the clock is already at the
     * drive edge, this method drives the value relative the current edge (i.e.
     * at current time + skew). In other words, driveRangeDelaySoftNB(0, value)
     * is equivalent to driveRangeSoftNB(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param highBit the index of the highest bit to drive
     * @param lowBit the index of the lowest bit to drive
     * @param value the value to drive
     */
    void driveRangeDelaySoftNB(int cycles, int highBit, int lowBit, Object value);

    /**
     * Performs a non-blocking soft range drive of this signal the given number
     * of cycles into the future. If cycles is 1, this method drives the value
     * at the next drive edge. If cycles is 0 and the clock is already at the
     * drive edge, this method drives the value relative the current edge (i.e.
     * at current time + skew). In other words, driveRangeDelaySoftNB(0, value)
     * is equivalent to driveRangeSoftNB(value).
     *
     * @param cycles the number of times the drive edge must be observed
     * @param range the range of bits to drive
     * @param value the value to drive
     */
    void driveRangeDelaySoftNB(int cycles, BitRange range, Object value);

    /**
     * Performs an immediate soft range drive of this signal, without
     * synchronizing to the drive edge of the clock.
     *
     * @param highBit the index of the highest bit to drive
     * @param lowBit the index of the lowest bit to drive
     * @param value the value to drive
     */
    void driveRangeAsyncSoft(int highBit, int lowBit, Object value);

    /**
     * Performs an immediate soft range drive of this signal, without
     * synchronizing to the drive edge of the clock.
     *
     * @param range the range of bits to drive
     * @param value the value to drive
     */
    void driveRangeAsyncSoft(BitRange range, Object value);

    /**
     * Suspends this thread until the clock for this signal is at the drive
     * edge. If the clock is already at the drive edge, this method returns
     * immediately.
     */
    void syncDrive();

    /**
     * Suspends this thread until the clock for this signal reaches the drive
     * edge the given number of times. If cycles is 1, this method waits for the
     * next drive edge. If cycles is 0 and the clock is already at the drive
     * edge, this method returns immediately. In other words, syncDriveDelay(0)
     * is equivalent to syncDrive().
     *
     * @param cycles the number of times the drive edge must be observed
     */
    void syncDriveDelay(int cycles);
}
