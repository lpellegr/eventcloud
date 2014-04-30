/**
 * Copyright (c) 2011-2014 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;

/**
 * DataHandler defines the methods that are used by a peer managing data. This
 * allows to automate/ease the data transfer during the
 * {@link StructuredOverlay#join(Peer)} and {@link StructuredOverlay#leave()}
 * operations.
 * 
 * @author lpellegr
 */
public interface DataHandler {

    /**
     * Defines how to assign the data received from a neighbor (during a join
     * operation) to the desired data structure. This method is automatically
     * executed during a join operation to assign the data retrieved from the
     * landmark peer to the peer which is joining.
     * 
     * @param dataReceived
     *            the data that are received when a peer joins a network from a
     *            landmark node.
     */
    void assignDataReceived(Serializable dataReceived);

    /**
     * Returns a copy of all the data managed by the current peer. This is
     * automatically executed when a peer leaves the network to transfer data.
     * 
     * @return all the data managed by the current peer.
     */
    Serializable retrieveAllData();

    /**
     * Returns a copy of all the data which are contained in the specified
     * {@code interval}. This method is automatically executed during the
     * introduce phase of a join operation to retrieve the data that have to be
     * transfered from the landmark peer to the new peer.
     * 
     * @param interval
     *            the interval to use in order to restrict the scope of the
     *            retrieve operation (e.g. with CAN the interval condition is a
     *            zone).
     * 
     * @return the data which are contained in the specified {@code interval}.
     */
    Serializable retrieveDataIn(Object interval);

    /**
     * Removes and returns all the data which are in the given {@code interval}.
     * This method is automatically executed during the welcome phase of a join
     * operation to remove from the landmark peer the data that have been
     * transfered to the new peer.
     * 
     * @param interval
     *            the interval to use in order to restrict the scope of the
     *            remove operation (e.g. with CAN the interval condition is a
     *            zone).
     * 
     * @return all the data which have been removed from the given
     *         {@code interval}.
     */
    Serializable removeDataIn(Object interval);

}
