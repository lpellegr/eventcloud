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

import fr.inria.eventcloud.load_balancing.LoadReport;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Gossip strategy based on Erdos-Renyi algorithm to generate random graphs. It
 * assumes that we have an approximation of the network size.
 * 
 * @author lpellegr
 */
public class ErdosRenyiStrategy implements GossipStrategy<LoadReport> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(SemanticCanOverlay overlay, LoadReport loadReport) {
        // TODO http://en.wikipedia.org/wiki/Erd%C5%91s%E2%80%93R%C3%A9nyi_model
    }

}
