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

import java.util.List;

/**
 * A GcmVirtualNodeEntry is the basic entry to use with the
 * {@link SchedulerNodeProvider}. It contains the name of its corresponding
 * GCMVirtualNode and the list of {@link NodeSourceEntry} that compose this
 * GCMVirtualNode.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class GcmVirtualNodeEntry {

    private String virtualNodeName;

    private List<NodeSourceEntry> nodeSourceEntries;

    public GcmVirtualNodeEntry(String vnName, List<NodeSourceEntry> entries) {
        this.virtualNodeName = vnName;
        this.nodeSourceEntries = entries;
    }

    public String getVirtualNodeName() {
        return this.virtualNodeName;
    }

    public List<NodeSourceEntry> getNodeSourceEntries() {
        return this.nodeSourceEntries;
    }

}
