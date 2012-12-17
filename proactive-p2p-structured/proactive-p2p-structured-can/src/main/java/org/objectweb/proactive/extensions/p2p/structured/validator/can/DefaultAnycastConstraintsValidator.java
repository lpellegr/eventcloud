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
package org.objectweb.proactive.extensions.p2p.structured.validator.can;

import static com.google.common.base.Preconditions.checkNotNull;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;

/**
 * This class is the default validator for {@link AnycastRequestRouter}. This
 * validator assumes that all coordinate elements set to {@code null} match the
 * constraints.
 * 
 * @author lpellegr
 */
public class DefaultAnycastConstraintsValidator<E extends StringElement>
        extends AnycastConstraintsValidator<E> {

    private static final long serialVersionUID = 140L;

    /**
     * Creates a new {@code DefaultAnycastConstraintsValidator} with the
     * specified {@code key} to reach.
     * 
     * @param key
     *            the key to reach.
     */
    public DefaultAnycastConstraintsValidator(Coordinate<E> key) {
        super(checkNotNull(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final boolean validatesKeyConstraints(StructuredOverlay overlay) {
        return this.validatesKeyConstraints(((CanOverlay<E>) overlay).getZone());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validatesKeyConstraints(Zone<E> zone) {
        for (byte i = 0; i < super.key.getValue().size(); i++) {
            // if coordinate is null we skip the test
            if (super.key.getValue().getElement(i) != null) {
                // the specified overlay does not contains the key
                if (zone.contains(i, super.key.getValue().getElement(i)) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

}
