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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.validator.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.LookupResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * {@link ConstraintsValidator} for {@link LookupRequest} and
 * {@link LookupResponse}.
 * 
 * @author lpellegr
 */
public class UnicastConstraintsValidator extends
        ConstraintsValidator<StringCoordinate> {

    private static final long serialVersionUID = 1L;

    public UnicastConstraintsValidator(StringCoordinate key) {
        super(key);
    }

    public boolean validatesKeyConstraints(StructuredOverlay overlay) {
        return ((CanOverlay) overlay).getZone().contains(super.key.getValue());
    }

}
