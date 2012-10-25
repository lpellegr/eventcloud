/**
 * Copyright (c) 2011-2012 INRIA.
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
package fr.inria.eventcloud.messages.response.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.ForwardResponse;

import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * A response that may be associated to a {@link ForwardRequest}. The response
 * contains a boolean value.
 * 
 * @author lpellegr
 */
public class BooleanForwardResponse extends ForwardResponse<SemanticElement> {

    private static final long serialVersionUID = 130L;

    private boolean result;

    public BooleanForwardResponse() {
        super();
    }

    /**
     * Returns the boolean value associated to the response.
     * 
     * @return the boolean value associated to the response.
     */
    public boolean getResult() {
        return this.result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

}
