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
package org.objectweb.proactive.extensions.p2p.structured.deployment;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.StringCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

/**
 * {@link CanDeploymentDescriptor} semiskilled for {@link StringCoordinate}s.
 * 
 * @author lpellegr
 */
public class StringCanDeploymentDescriptor extends
        CanDeploymentDescriptor<StringCoordinate> {

    private static final long serialVersionUID = 160L;

    /**
     * Creates a new {@link CanDeploymentDescriptor} by using a
     * {@link CanOverlay} provider.
     */
    public StringCanDeploymentDescriptor() {
        super(new SerializableProvider<StringCanOverlay>() {
            private static final long serialVersionUID = 160L;

            @Override
            public StringCanOverlay get() {
                return new StringCanOverlay();
            }
        });
    }

}
