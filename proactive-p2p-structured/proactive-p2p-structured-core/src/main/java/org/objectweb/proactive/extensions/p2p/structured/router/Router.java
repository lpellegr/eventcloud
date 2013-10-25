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
package org.objectweb.proactive.extensions.p2p.structured.router;

import org.objectweb.proactive.extensions.p2p.structured.messages.Message;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * A router defines how to route a {@link Message}.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the message type to route.
 * @param <K>
 *            the key type used to check at each routing step whether the
 *            constraints are validated or not.
 */
public abstract class Router<T extends Message<K>, K> {

    /**
     * Constructs a new Router.
     */
    public Router() {
    }

    /**
     * This method is used by a router to make decision when a message is
     * received on a peer. The decision consists in choosing which method
     * between {@link #route(StructuredOverlay, Message)} or
     * {@link #handle(StructuredOverlay, Message)} must be called. A correct
     * implementation of {@link #makeDecision(StructuredOverlay, Message)} is
     * supposed to call {@link #route(StructuredOverlay, Message)} and
     * {@link #handle(StructuredOverlay, Message)} methods according to the
     * conditions which are verified.
     * 
     * @param overlay
     *            the overlay used to make the decision.
     * @param msg
     *            the message to forward or to handle.
     */
    public abstract void makeDecision(StructuredOverlay overlay, T msg);

    /**
     * Handles the specified {@code msg} on the given {@code overlay} (i.e. the
     * message has reached one of the peers it should reach).
     * 
     * @param overlay
     *            the {@link StructuredOverlay} used to handle the message.
     * @param msg
     *            the {@link Message} to handle.
     */
    protected abstract void handle(StructuredOverlay overlay, T msg);

    /**
     * Makes decision to route the message to an another peer.
     * 
     * @param overlay
     *            the {@link StructuredOverlay} to use in order to make the
     *            decision.
     * @param msg
     *            the {@link Message} which have to be routed.
     */
    protected abstract void route(StructuredOverlay overlay, T msg);

    /**
     * This method is supposed to be called when the message is on a peer which
     * validates the constraints.
     * 
     * @param overlay
     *            the overlay which handles the message.
     * 
     * @param msg
     *            the message which has reached the destination.
     */
    protected void onDestinationReached(StructuredOverlay overlay, T msg) {
        // to be overridden
    }

}
