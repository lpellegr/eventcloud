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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.deployment;

import org.objectweb.proactive.extensions.p2p.structured.deployment.TestingDeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;

/**
 * This class is used to specialize an {@link EventCloudDeployer} for unit
 * testing by using by default an {@link InMemoryJenaDatastore}.
 * 
 * @author lpellegr
 */
public class JunitEventCloudDeployer extends EventCloudDeployer {

    private static final long serialVersionUID = 1L;

    private final DatastoreType datastoreType;

    public JunitEventCloudDeployer() {
        this(DatastoreType.IN_MEMORY);
    }

    public JunitEventCloudDeployer(DatastoreType type) {
        super(new TestingDeploymentConfiguration());
        this.datastoreType = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Peer createPeer() {
        SerializableProvider<SemanticCanOverlay> provider;

        if (this.datastoreType == DatastoreType.IN_MEMORY) {
            provider = new SemanticInMemoryOverlayProvider();
        } else {
            provider = new SemanticPersistentOverlayProvider();
        }

        return SemanticFactory.newSemanticPeer(provider);
    }

}
