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
 * Defines the methods required for implementing a gossip strategy. We assume
 * that all gossip strategies rely on a push model and not a pull one since the
 * last incurs bad performances (e.g. higher bandwidth consumption due to the
 * round-trip). In anycase, we are aware that gossiping is slow and unsuitable
 * for real-time systems where speed is the goal.
 * 
 * @author lpellegr
 * 
 * @param T
 *            the type of the rumor to propagate to neighbors.
 */
public interface GossipStrategy<T> {

    /**
     * Pushes the specified rumour to the desired neighbors.
     * 
     * @param overlay
     *            the overlay from where the rumour should be triggered.
     * @param rumour
     *            the rumour to propagate.
     */
    void push(SemanticCanOverlay overlay, T rumour);

}
