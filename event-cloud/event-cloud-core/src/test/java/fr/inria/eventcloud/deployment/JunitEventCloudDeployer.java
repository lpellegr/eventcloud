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

import org.objectweb.proactive.extensions.p2p.structured.deployment.TestingDeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;

/**
 * This class is used to specialize an {@link EventCloudDeployer} for unit
 * testing by using by default a {@link SemanticInMemoryOverlayProvider}.
 * 
 * @author lpellegr
 */
public class JunitEventCloudDeployer extends EventCloudDeployer {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@link JunitEventCloudDeployer} by using a
     * {@link SemanticInMemoryOverlayProvider} for creating peers.
     */
    public JunitEventCloudDeployer() {
        this(DatastoreType.IN_MEMORY);
    }

    /**
     * Creates a new {@link JunitEventCloudDeployer} by using a
     * {@link SemanticInMemoryOverlayProvider} for creating peers and the
     * specified {@code injectionConstraintsProvider}.
     * 
     * @param injectionConstraintsProvider
     *            a provider that knows how to create the constraints to use
     *            during the creation of the network.
     */
    public JunitEventCloudDeployer(
            InjectionConstraintsProvider injectionConstraintsProvider) {
        this(DatastoreType.IN_MEMORY);
    }

    /**
     * Creates a new {@link JunitEventCloudDeployer} by using the specified
     * datastore type (i.e. {@link SemanticInMemoryOverlayProvider} or
     * {@link SemanticPersistentOverlayProvider}) and the specified
     * {@code injectionConstraintsProvider} when peers are created.
     * 
     * @param type
     *            the datastore type that is used to determine which kind of
     *            overlay provider to use.
     * @param injectionConstraintsProvider
     *            a provider that knows how to create the constraints to use
     *            during the creation of the network.
     */
    public JunitEventCloudDeployer(DatastoreType type,
            InjectionConstraintsProvider injectionConstraintsProvider) {
        super(new TestingDeploymentConfiguration(),
                createProviderAccordingToDatastoreType(type),
                injectionConstraintsProvider);
    }

    /**
     * Creates a new {@link JunitEventCloudDeployer} by using the specified
     * datastore type (i.e. {@link SemanticInMemoryOverlayProvider} or
     * {@link SemanticPersistentOverlayProvider}) when peers are created.
     * 
     * @param type
     *            the datastore type that is used to determine which kind of
     *            overlay provider to use.
     */
    public JunitEventCloudDeployer(DatastoreType type) {
        super(new TestingDeploymentConfiguration(),
                createProviderAccordingToDatastoreType(type));
    }

    private static SerializableProvider<SemanticCanOverlay> createProviderAccordingToDatastoreType(DatastoreType type) {
        if (type == DatastoreType.IN_MEMORY) {
            return new SemanticInMemoryOverlayProvider();
        } else {
            return new SemanticPersistentOverlayProvider();
        }
    }

}
