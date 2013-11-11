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
package org.objectweb.proactive.extensions.p2p.structured.overlay;

/**
 * Maintains the different states and transitioning states that may be assigned
 * to a {@link Peer}.
 * 
 * @author lpellegr
 */
public enum Status {

    /**
     * A peer in this state is inactive. Only {@link Peer#create()} and
     * {@link Peer#join(Peer)} operations are allowed.
     */
    NOT_ACTIVATED,
    /**
     * A peer in this state is active. All peer features should be working and
     * allowed (e.g. routing requests, handling operations, etc.).
     */
    ACTIVATED,
    /**
     * A peer in this state is transitioning to {@link #ACTIVATED} by performing
     * a {@link Peer#create()} operation.
     */
    PERFORMING_CREATE,
    /**
     * A peer in this state is transitioning to {@link #ACTIVATED} by performing
     * a {@link Peer#join(Peer)} operation.
     */
    PERFORMING_JOIN,
    /**
     * A peer in this state is transitioning to {@link #ACTIVATED} by performing
     * a {@link Peer#leave()} operation.
     */
    PERFORMING_LEAVE,
    /**
     * A peer in this state is transitioning to {@link #ACTIVATED} by performing
     * a {@link Peer#reassign(Peer)} operation.
     */
    PERFORMING_REASSIGN

}
