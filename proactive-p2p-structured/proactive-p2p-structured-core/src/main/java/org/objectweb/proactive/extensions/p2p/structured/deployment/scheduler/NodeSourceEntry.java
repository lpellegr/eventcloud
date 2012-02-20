/**
 * Copyright (c) 2011 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.deployment.scheduler;

import org.objectweb.proactive.core.UniqueID;

/**
 * NodeSourceEntry is used as an entry in a {@link GcmVirtualNodeEntry}.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class NodeSourceEntry {

    private final String nodeSourceName;

    private final int nbNodes;

    protected UniqueID nodeRequestId;

    public NodeSourceEntry(int nbNodes) {
        this(null, nbNodes);
    }

    public NodeSourceEntry(String nodeSourceName, int nbNodes) {
        this.nodeSourceName = nodeSourceName;
        this.nbNodes = nbNodes;
    }

    public String getNodeSourceName() {
        return this.nodeSourceName;
    }

    public int getNbNodes() {
        return this.nbNodes;
    }

}
