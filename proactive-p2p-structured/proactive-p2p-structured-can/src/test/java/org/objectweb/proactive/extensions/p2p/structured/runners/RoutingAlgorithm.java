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
package org.objectweb.proactive.extensions.p2p.structured.runners;

import org.objectweb.proactive.extensions.p2p.structured.messages.Message;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.EfficientBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.router.can.FloodingBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.router.can.OptimalBroadcastRequestRouter;

/**
 * Defines the different routing algorithms.
 * 
 * @author lpellegr
 */
public enum RoutingAlgorithm {

    FLOODING_BROADCAST, EFFICIENT_BROADCAST, OPTIMAL_BROADCAST;

    public static RoutingAlgorithm toUse() {
        return RoutingAlgorithm.valueOf(System.getProperty("junit.routing.algorithm"));
    }

    public static <E extends Element> Router<? extends Message<Coordinate<E>>, Coordinate<E>> createRouterToUse() {
        switch (RoutingAlgorithm.toUse()) {
            case EFFICIENT_BROADCAST:
                return new EfficientBroadcastRequestRouter<MulticastRequest<E>, E>();
            case FLOODING_BROADCAST:
                return new FloodingBroadcastRequestRouter<MulticastRequest<E>, E>();
            case OPTIMAL_BROADCAST:
                return new OptimalBroadcastRequestRouter<MulticastRequest<E>, E>();
        }

        throw new IllegalStateException();
    }

}
