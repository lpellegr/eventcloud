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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;

/**
 * Message Identifier.
 * 
 * @author lpellegr
 */
public final class MessageId implements Serializable {

    private static final long serialVersionUID = 151L;

    private final UniqueID producerId;

    private final long producerSequenceId;

    /**
     * Cached hashCode value (class is immutable)
     */
    private transient int hashCode;

    /**
     * Cached toString value (class is immutable)
     */
    private transient String toString;

    public MessageId(UniqueID proxyId, long sequenceId) {
        this.producerId = proxyId;
        this.producerSequenceId = sequenceId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof MessageId) {
            MessageId other = (MessageId) obj;

            return this.producerSequenceId == other.producerSequenceId
                    && this.producerId.equals(other.producerId);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode =
                    this.producerId.hashCode() ^ (int) this.producerSequenceId;
        }

        return this.hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (this.toString == null) {
            this.toString =
                    this.producerId.toString() + ':' + this.producerSequenceId;
        }

        return this.toString;
    }

}
