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

import com.newisys.eventsim.Event;
import com.newisys.verilog.EdgeSet;
import com.newisys.verilog.util.BitVector;

/**
 * Represents an input signal (or multiple-bit input signal bus). It provides
 * methods that:
 * <ul>
 * <li>describe when the signal is sampled (relative to a clock signal)</li>
 * <li>sample the value of the signal</li>
 * <li>synchronize relative to the sample edge of the clock signal</li>
 * <li>synchronize relative to changes in the value of the signal</li>
 * </ul>
 * 
 * @author Trevor Robinson
 */
public interface InputSignal
    extends Signal
{
    /**
     * Returns the set of clock edges this signal is sampled at.
     *
     * @return the sample edge set
     */
    EdgeSet getInputEdges();

    /**
     * Returns the skew (in simulator ticks) relative to the clock edge at which
     * this signal is sampled. The skew can be any non-positive integer, though
     * the use of zero skew is not recommended for input signals. For in/out
     * signals, the skew must be negative. For clock signals, the skew is always
     * 0.
     *
     * @return the sample skew
     */
    int getInputSkew();

    /**
     * Returns the sample buffer depth for this signal, which is the number of
     * samples stored for back-references. The depth is always at least 1.
     *
     * @return the sample buffer depth
     * @see InputSignal#sampleDepth(int)
     */
    int getInputDepth();

    /**
     * Samples the value of this signal, after synchronizing to the sample edge
     * of the clock, as if by a call to syncSample().
     *
     * @return the value of this signal
     * @see InputSignal#syncSample()
     */
    BitVector sample();

    /**
     * Returns the value of this signal the given number of cycles in the past,
     * after synchronizing to the sample edge of the clock, as if by a call to
     * syncSample(). The given depth must be >= 0 and less than the sample
     * buffer depth of this signal, as returned by getInputDepth(). Calling this
     * method with depth 0 is equivalent to calling sample().
     *
     * @param depth the number of cycles in the past
     * @return the value of this signal
     * @see InputSignal#syncSample()
     * @see InputSignal#getInputDepth()
     */
    BitVector sampleDepth(int depth);

    /**
     * Samples the value of this signal immediately, without synchronizing to
     * the sample edge of the clock.
     *
     * @return the value of this signal
     */
    BitVector sampleAsync();

    /**
     * Returns the value of this signal the given number of cycles in the past,
     * without synchronizing to the sample edge of the clock. The given depth
     * must be >= 0 and less than the sample buffer depth of this signal, as
     * returned by getInputDepth(). Calling this method with depth 0 returns the
     * value of this signal at the last clock edge.
     *
     * @param depth the number of cycles in the past
     * @return the value of this signal
     * @see InputSignal#getInputDepth()
     */
    BitVector sampleDepthAsync(int depth);

    /**
     * Suspends this thread until the clock for this signal is at the sample
     * edge. If the clock is already at the sample edge, this method returns
     * immediately.
     */
    void syncSample();

    /**
     * Suspends this thread until the clock for this signal reaches the sample
     * edge the given number of times. If cycles is 1, this method waits for the
     * next sample edge. If cycles is 0 and the clock is already at the sample
     * edge, this method returns immediately. In other words, syncSampleDelay(0)
     * is equivalent to syncSample().
     *
     * @param cycles the number of times the sample edge must be observed
     */
    void syncSampleDelay(int cycles);

    /**
     * Suspends this thread until a) (the low bit of) this signal reaches an
     * edge in the given set and then b) the clock for this signal reaches its
     * next sample edge. At the time of the call, if the last transition of the
     * signal in the current cycle matches the given edge set, condition a) is
     * considered satisfied and this method simply waits for condition b). This
     * method is intended for use with single bit signals; for multiple bit
     * signals, use syncEdge(EdgeSet, int).
     *
     * @param edges the set of edges to wait for
     * @see #syncEdge(EdgeSet, int)
     */
    void syncEdge(EdgeSet edges);

    /**
     * Suspends this thread until a) the given bit of this signal reaches an
     * edge in the given set and then b) the clock for this signal reaches its
     * next sample edge. At the time of the call, if the last transition of the
     * signal in the current cycle matches the given edge set, condition a) is
     * considered satisfied and this method simply waits for condition b).
     *
     * @param edges the set of edges to wait for
     * @param bit the index of the bit to wait for the edge on
     */
    void syncEdge(EdgeSet edges, int bit);

    /**
     * Suspends this thread until (the low bit of) this signal reaches an edge
     * in the given set. This method does not synchronize to the sample edge of
     * the clock. This method is intended for use with single bit signals; for
     * multiple bit signals, use syncEdgeAsync(EdgeSet, int).
     *
     * @param edges the set of edges to wait for
     * @see #syncEdgeAsync(EdgeSet, int)
     */
    void syncEdgeAsync(EdgeSet edges);

    /**
     * Suspends this thread until the given bit of this signal reaches an edge
     * in the given set. This method does not synchronize to the sample edge of
     * the clock.
     *
     * @param edges the set of edges to wait for
     * @param bit the index of the bit to wait for the edge on
     */
    void syncEdgeAsync(EdgeSet edges, int bit);

    /**
     * Returns an event that will be notified each time the given bit of this
     * signal reaches an edge in the given set. Unless the async flag is set,
     * the event is not notified until the next sample edge of the clock.
     *
     * @param edges the set of edges to wait for
     * @param bit the index of the bit to wait for edges on
     * @param async indicates not to wait for the next sample edge
     * @return an event notified each time the requested edge(s) occurs
     */
    Event getEdgeEvent(EdgeSet edges, int bit, boolean async);

    /**
     * Suspends this thread until a) (any bit of) this signal changes value and
     * then b) the clock for this signal reaches its next sample edge. At the
     * time of the call, if the signal has changed value in the current cycle,
     * condition a) is considered satisfied and this method simply waits for
     * condition b).
     */
    void syncChange();

    /**
     * Suspends this thread until a) one of the signal bits corresponding to the
     * given mask changes value and then b) the clock for this signal reaches
     * its next sample edge. At the time of the call, if the signal has changed
     * value in the current cycle, condition a) is considered satisfied and this
     * method simply waits for condition b).
     *
     * @param mask a bit vector containing a ONE for each signal bit to watch,
     *            or null to watch the entire signal
     */
    void syncChange(BitVector mask);

    /**
     * Suspends this thread until (any bit of) this signal changes value. This
     * method does not synchronize to the sample edge of the clock.
     */
    void syncChangeAsync();

    /**
     * Suspends this thread until one of the signal bits corresponding to the
     * given mask changes value. This method does not synchronize to the sample
     * edge of the clock.
     *
     * @param mask a bit vector containing a ONE for each signal bit to watch,
     *            or null to watch the entire signal
     */
    void syncChangeAsync(BitVector mask);

    /**
     * Returns an event that will be notified each time any of the signal bits
     * corresponding to the given mask changes value. Unless the async flag is
     * set, the event is not notified until the next sample edge of the clock.
     *
     * @param mask a bit vector containing a ONE for each signal bit to watch,
     *            or null to watch the entire signal
     * @param async indicates not to wait for the next sample edge
     * @return an event notified each time the requested change occurs
     */
    Event getChangeEvent(BitVector mask, boolean async);
}
