/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.overlay.datastore;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * A DataHandler defines methods that are used by a peer managing data. This
 * allows to automate/ease the data transfer during the
 * {@link StructuredOverlay#join(Peer)} and {@link StructuredOverlay#leave()}
 * operations.
 * 
 * @author lpellegr
 */
public interface PeerDataHandler {

    /**
     * Affects the specified {@code dataReceived} to the datastore.
     * 
     * @param dataReceived
     *            the data which have been received (e.g. when the peer has join
     *            a network from a landmark node).
     */
    public abstract void affectDataReceived(Object dataReceived);

    /**
     * Returns a copy of all the data from the datastore.
     * 
     * @return all the data managed by the peer.
     */
    public abstract Object retrieveAllData();

    /**
     * Returns a copy of all the data which are contained in the specified
     * {@code interval}.
     * 
     * @param interval
     *            the interval to use in order to restrict the scope of the
     *            retrieve operation (e.g. with CAN, the interval condition is a
     *            zone).
     * 
     * @return the data which are contained in the specified {@code interval}.
     */
    public abstract Object retrieveDataIn(Object interval);

    /**
     * Removes and returns all the data which are in the given {@code interval}.
     * 
     * @param interval
     *            the interval to use in order to restrict the scope of the
     *            remove operation (e.g. with CAN, the interval condition is a
     *            zone).
     * 
     * @return all the data which have been removed from the given
     *         {@code interval}.
     */
    public abstract Object removeDataIn(Object interval);

}
