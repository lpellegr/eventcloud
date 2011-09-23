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

import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.TestingDeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

import fr.inria.eventcloud.datastore.InMemoryJenaDatastore;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.providers.SemanticNonPersistentOverlayProvider;

/**
 * This class is used to specialize an {@link EventCloudDeployer} for unit
 * testing by using an {@link InMemoryJenaDatastore}.
 * 
 * @author lpellegr
 */
public class JunitEventCloudDeployer extends EventCloudDeployer {

    private static final long serialVersionUID = 1L;

    public JunitEventCloudDeployer() {
        super(new TestingDeploymentConfiguration());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Peer createPeer(NodeProvider nodeProvider) {
        return SemanticFactory.newSemanticPeer(new SemanticNonPersistentOverlayProvider());
    }

}
