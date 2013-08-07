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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.StringZone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

/**
 * A {@link CanOverlay} semiskilled for {@link StringElement}s.
 * 
 * @author lpellegr
 */
public class StringCanOverlay extends CanOverlay<StringElement> {

    public StringCanOverlay() {
        super();
    }

    /**
     * Constructs a new overlay with the specified
     * {@code requestResponseManager} and {@code datastore}.
     * 
     * @param requestResponseManager
     *            the {@link RequestResponseManager} to use.
     */
    public StringCanOverlay(RequestResponseManager requestResponseManager) {
        super(requestResponseManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Zone<StringElement> newZone() {
        return new StringZone();
    }

    @Override
    public void assignDataReceived(Serializable dataReceived) {

    }

    @Override
    public Serializable retrieveAllData() {
        return null;
    }

    @Override
    public Serializable retrieveDataIn(Object interval) {
        return null;
    }

    @Override
    public Serializable removeDataIn(Object interval) {
        return null;
    }

}
