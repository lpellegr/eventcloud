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
package fr.inria.eventcloud.deployment;

import org.objectweb.proactive.extensions.p2p.structured.deployment.CanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;

/**
 * {@link DeploymentDescriptor} to use with an {@link EventCloudDeployer}.
 * 
 * @author lpellegr
 */
public class EventCloudDeploymentDescriptor extends
        CanDeploymentDescriptor<SemanticElement> {

    private static final long serialVersionUID = 140L;

    /**
     * Creates a new {@link EventCloudDeploymentDescriptor} by using a
     * {@link SemanticInMemoryOverlayProvider}.
     */
    public EventCloudDeploymentDescriptor() {
        super(new SemanticInMemoryOverlayProvider());
    }

    /**
     * Creates a new {@link EventCloudDeploymentDescriptor} with the specified
     * {@code overlayProvider}.
     * 
     * @param overlayProvider
     *            the overlay provider to use.
     */
    public EventCloudDeploymentDescriptor(
            SerializableProvider<? extends SemanticCanOverlay> overlayProvider) {
        super(overlayProvider);
    }

}
