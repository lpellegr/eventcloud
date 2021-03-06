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
package fr.inria.eventcloud.jmx;

import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;

import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * JMX MBean interface for defining information that can be retrieved for a
 * {@link SemanticPeer}.
 * 
 * @author lpellegr
 */
public interface SemanticPeerMBean {

    boolean isDynamicLoadBalancingEnabled();

    boolean isStaticLoadBalancingEnabled();

    OverlayId getId();

    long getNumberOfMiscDataManaged();

    // double getLocalLoad();
    //
    // double getAverageOverlayLoad();
    //
    // LoadState getLoadState();

    long getLastMaintenanceTimestamp();

}
