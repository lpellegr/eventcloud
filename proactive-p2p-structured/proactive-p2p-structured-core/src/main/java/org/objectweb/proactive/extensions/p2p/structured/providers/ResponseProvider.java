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
package org.objectweb.proactive.extensions.p2p.structured.providers;

import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * This class is an abstraction that defines how to build a response.
 * 
 * @author lpellegr
 */
public abstract class ResponseProvider<T extends Response<K>, K> extends
        SerializableProvider<T> {

    private static final long serialVersionUID = 150L;

    /**
     * Returns a new response whose its attributes have been initialized with a
     * call to {@link Response#setAttributes(Request, StructuredOverlay)}.
     * 
     * @param request
     *            the request that triggers the creation of the response.
     * @param overlay
     *            the overlay on which the response is created.
     * 
     * @return a response according to the request type.
     */
    public T get(Request<K> request, StructuredOverlay overlay) {
        T result = this.get();
        result.setAttributes(request, overlay);
        return result;
    }

}
