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
package fr.inria.eventcloud.tracker;

import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * A specific extension of the {@link Tracker} interface for a semantic content
 * addressable network.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public interface SemanticTracker extends Tracker {

    /**
     * Returns a random and valid semantic peer from the stored peers list.
     * 
     * @return a random and valid semantic peer.
     */
    public SemanticPeer getRandomSemanticPeer();

}
