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

import org.junit.After;
import org.junit.Before;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * An {@link EventCloudDeployer} configured for testing.
 * 
 * @author lpellegr
 */
public class JunitByClassEventCloudDeployer extends
        JunitEventCloudInfrastructureDeployer {

    private final EventCloudDeploymentDescriptor descriptor;

    private final int nbTrackers;

    private final int nbPeers;

    private EventCloudId eventCloudId;

    private PublishApi publishProxy;

    private PutGetApi putgetProxy;

    private SubscribeApi subscribeProxy;

    public JunitByClassEventCloudDeployer(int nbTrackers, int nbPeers) {
        this(new EventCloudDeploymentDescriptor(), nbTrackers, nbPeers);
    }

    public JunitByClassEventCloudDeployer(
            EventCloudDeploymentDescriptor deploymentDescriptor,
            int nbTrackers, int nbPeers) {
        super();
        this.descriptor = deploymentDescriptor;
        this.nbTrackers = nbTrackers;
        this.nbPeers = nbPeers;
    }

    public SemanticPeer getRandomSemanticPeer() {
        return super.getRandomSemanticPeer(this.eventCloudId);
    }

    public SemanticTracker getRandomSemanticTracker() {
        return super.getRandomSemanticTracker(this.eventCloudId);
    }

    public PublishApi getPublishProxy() {
        return this.publishProxy;
    }

    public PutGetApi getPutGetProxy() {
        return this.putgetProxy;
    }

    public SubscribeApi getSubscribeProxy() {
        return this.subscribeProxy;
    }

    @Before
    public void setUp() {
        this.eventCloudId =
                super.newEventCloud(
                        this.descriptor, this.nbTrackers, this.nbPeers);

        try {
            this.publishProxy =
                    ProxyFactory.newPublishProxy(
                            super.getEventCloudsRegistryUrl(),
                            this.eventCloudId);

            this.putgetProxy =
                    ProxyFactory.newPutGetProxy(
                            this.getEventCloudsRegistryUrl(), this.eventCloudId);

            this.subscribeProxy =
                    ProxyFactory.newSubscribeProxy(
                            this.getEventCloudsRegistryUrl(), this.eventCloudId);
        } catch (EventCloudIdNotManaged e) {
            throw new IllegalStateException(e);
        }
    }

    @After
    public void tearDown() {
        super.undeploy();
    }

}
