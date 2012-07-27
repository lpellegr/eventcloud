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
package org.objectweb.proactive.extensions.p2p.structured.deployment;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

/**
 * This class defines a deployment descriptor that is supposed to be used with a
 * {@link CanNetworkDeployer}.
 * 
 * @author lpellegr
 */
public class CanDeploymentDescriptor extends DeploymentDescriptor {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@link CanDeploymentDescriptor} by using a
     * {@link CanOverlay} provider.
     */
    public CanDeploymentDescriptor() {
        super(new SerializableProvider<CanOverlay>() {
            private static final long serialVersionUID = 1L;

            @Override
            public CanOverlay get() {
                return new CanOverlay();
            }
        });
    }

    /**
     * Creates a new {@link CanDeploymentDescriptor} with the specified
     * {@code overlayProvider}.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     */
    public CanDeploymentDescriptor(
            SerializableProvider<? extends CanOverlay> overlayProvider) {
        super(overlayProvider);
    }

}
