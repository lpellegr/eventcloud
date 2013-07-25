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
package org.objectweb.proactive.extensions.p2p.structured.operations.mutual_exclusion;

import org.objectweb.proactive.extensions.p2p.structured.mutual_exclusion.RicartAgrawalaManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * Reply operation associated to the Ricart Agrawala algorithm.
 * 
 * @author lpellegr
 */
public class RicartAgrawalaReply extends MutualExclusionOperation {

    private static final long serialVersionUID = 151L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(StructuredOverlay overlay) {
        ((RicartAgrawalaManager) overlay.getMutualExclusionManager()).receiveReply();
    }

}
