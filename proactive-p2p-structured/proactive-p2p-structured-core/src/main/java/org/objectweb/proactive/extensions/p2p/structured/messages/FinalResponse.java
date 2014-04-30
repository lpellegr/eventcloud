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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;

/**
 * Final response that is received by the requester (i.e. a {@link Proxy} that
 * has submitted a {@link Request}).
 * 
 * @author lpellegr
 */
public class FinalResponse implements Serializable {

    private static final long serialVersionUID = 160L;

    private final long creationTime;

    private final MessageId id;

    private final Serializable result;

    public FinalResponse(MessageId id, Serializable result) {
        this.creationTime = System.currentTimeMillis();
        this.id = id;
        this.result = result;
    }

    public long getElapsedTimeSinceCreation() {
        return System.currentTimeMillis() - this.creationTime;
    }

    public MessageId getId() {
        return this.id;
    }

    public Serializable getResult() {
        return this.result;
    }

}
