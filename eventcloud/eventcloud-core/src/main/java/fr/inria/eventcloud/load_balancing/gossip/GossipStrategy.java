/**
 * Copyright (c) 2011-2013 INRIA.
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
package fr.inria.eventcloud.load_balancing.gossip;

import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Defines the methods required for implementing a gossip strategy. It is
 * assumes that all gossip strategies rely on a push model and not a pull one
 * since the last incurs bad performances (e.g. higher bandwidth consumption due
 * to the round-trip).
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            rumor's type to propagate.
 */
public interface GossipStrategy<T> {

    /**
     * Pushes the specified rumour to the desired peers.
     * 
     * @param overlay
     *            the overlay from where the rumour is triggered.
     * @param rumour
     *            the rumour to propagate.
     */
    void push(SemanticCanOverlay overlay, T rumour);

}
