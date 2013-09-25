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
package fr.inria.eventcloud.deployment;

import org.objectweb.proactive.extensions.p2p.structured.deployment.CanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.local.LocalNodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

import com.google.common.base.Preconditions;

import fr.inria.eventcloud.factories.ComponentPoolManagerFactory;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.providers.SemanticOverlayProvider;

/**
 * {@link DeploymentDescriptor} to use with an {@link EventCloudDeployer}.
 * 
 * @author lpellegr
 */
public class EventCloudDeploymentDescriptor extends
        CanDeploymentDescriptor<SemanticElement> {

    private static final long serialVersionUID = 160L;

    private ComponentPoolManager componentPoolManager;

    /**
     * Creates a new {@link EventCloudDeploymentDescriptor} by using an
     * in-memory {@link SemanticOverlayProvider}.
     */
    public EventCloudDeploymentDescriptor() {
        super(new SemanticOverlayProvider(true));
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

    /**
     * Sets the component pool manager to use during the deployment.
     * 
     * @param componentPoolManager
     *            the component pool manager to use during the deployment.
     */
    public DeploymentDescriptor setComponentPoolManager(ComponentPoolManager componentPoolManager) {
        Preconditions.checkState(
                this.componentPoolManager == null,
                "Node provider was already set or deployment has already started");

        this.componentPoolManager = componentPoolManager;

        if (!this.componentPoolManager.isStarted()) {
            this.componentPoolManager.start();
        }

        return this;
    }

    /**
     * Returns the instance of the {@link ComponentPoolManager} that is used to
     * deploy the components.
     * 
     * @return returns the instance of the {@link ComponentPoolManager} that is
     *         used to deploy the components.
     */
    public ComponentPoolManager getComponentPoolManager() {
        if (this.componentPoolManager == null) {
            NodeProvider nodeProvider = new LocalNodeProvider();
            nodeProvider.start();
            this.componentPoolManager =
                    ComponentPoolManagerFactory.newComponentPoolManager(nodeProvider);
            this.componentPoolManager.start();
        }

        return this.componentPoolManager;
    }

}
