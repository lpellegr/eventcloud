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

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

import com.google.common.base.Preconditions;

/**
 * Describes what are the parameters to use with a {@link NetworkDeployer}.
 * 
 * @author lpellegr
 */
public class DeploymentDescriptor implements Serializable {

    private static final long serialVersionUID = 140L;

    private final SerializableProvider<? extends StructuredOverlay> overlayProvider;

    private DeploymentConfiguration deploymentConfiguration;

    private InjectionConstraintsProvider injectionConstraintsProvider;

    private NodeProvider nodeProvider;

    /**
     * Creates a new deployment descriptor from the specified
     * {@code overlayProvider}.
     * 
     * @param overlayProvider
     *            the provider to use in order to create an overlay for each
     *            peer which is built and deployed.
     */
    public DeploymentDescriptor(
            SerializableProvider<? extends StructuredOverlay> overlayProvider) {
        this.overlayProvider = overlayProvider;
    }

    /**
     * Sets the deployment configuration to use during the deployment.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment.
     */
    public DeploymentDescriptor setDeploymentConfiguration(DeploymentConfiguration deploymentConfiguration) {
        Preconditions.checkState(
                this.deploymentConfiguration == null,
                "Deployment configuration was already set");

        this.deploymentConfiguration = deploymentConfiguration;

        return this;
    }

    /**
     * Sets the injection constraints provider to use during the deployment.
     * 
     * @param injectionConstraintsProvider
     *            the injection constraints provider to use during the
     *            deployment.
     */
    public DeploymentDescriptor setInjectionConstraintsProvider(InjectionConstraintsProvider injectionConstraintsProvider) {
        Preconditions.checkState(
                this.injectionConstraintsProvider == null,
                "Injection constraints provider was already set");

        this.injectionConstraintsProvider = injectionConstraintsProvider;

        return this;
    }

    /**
     * Sets the node provider to use during the deployment.
     * 
     * @param nodeProvider
     *            the node provider to use during the deployment.
     */
    public DeploymentDescriptor setNodeProvider(NodeProvider nodeProvider) {
        Preconditions.checkState(
                this.nodeProvider == null, "Node provider was already set");

        this.nodeProvider = nodeProvider;

        return this;
    }

    /**
     * Returns the deployment configuration used during the deployment.
     * 
     * @return the deployment configuration used during the deployment.
     */
    public DeploymentConfiguration getDeploymentConfiguration() {
        return this.deploymentConfiguration;
    }

    /**
     * Returns the injection constraints provider used during the deployment.
     * 
     * @return the injection constraints provider used during the deployment.
     */
    public InjectionConstraintsProvider getInjectionConstraintsProvider() {
        return this.injectionConstraintsProvider;
    }

    /**
     * Returns the instance of the {@link NodeProvider} that is used to deploy
     * the components, or {@code null} if no {@link NodeProvider} is provided.
     * 
     * @return returns the instance of the {@link NodeProvider} that is used to
     *         deploy the components, or {@code null} if no {@link NodeProvider}
     *         is used.
     */
    public NodeProvider getNodeProvider() {
        return this.nodeProvider;
    }

    /**
     * Returns the overlay provider used during the deployment.
     * 
     * @return the overlay provider used during the deployment.
     */
    public SerializableProvider<? extends StructuredOverlay> getOverlayProvider() {
        return this.overlayProvider;
    }

}
