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
package fr.inria.eventcloud.load_balancing.gossip;

import fr.inria.eventcloud.load_balancing.LoadReport;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Gossip strategy based on random walks. Forwards the load report to peers
 * selected at random.
 * 
 * @author lpellegr
 */
public class RandomWalkStrategy implements GossipStrategy<LoadReport> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(SemanticCanOverlay overlay, LoadReport loadReport) {
        // TODO
    }

}
