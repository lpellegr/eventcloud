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
package org.objectweb.proactive.extensions.p2p.structured.validator;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

/**
 * Used by the {@link Router}s to known whether a {@link StructuredOverlay}
 * which handles the message validates the constraints associated to the key
 * contained by the message.
 * 
 * @author lpellegr
 * 
 * @param <K>
 *            the type of the key used to check whether the constraints are
 *            validated or not.
 */
public abstract class ConstraintsValidator<K> implements Serializable {

    private static final long serialVersionUID = 160L;

    /**
     * The key used in order to route the query over the network.
     */
    protected final SerializedValue<K> key;

    public ConstraintsValidator(K key) {
        this.key = SerializedValue.create(key);
    }

    public abstract boolean validatesKeyConstraints(StructuredOverlay overlay);

    /**
     * Returns the key to reach (i.e. the peer containing this key is the
     * receiver of this message).
     * 
     * @return the key to reach (i.e. the peer containing this key is the
     *         receiver of this message).
     */
    public K getKey() {
        return this.key.getValue();
    }

}
