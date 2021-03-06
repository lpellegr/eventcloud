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
package fr.inria.eventcloud.datastore;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.p2p.structured.utils.Files;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.deployment.JunitEventCloudDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.providers.SemanticOverlayProvider;

/**
 * This class defines a test that checks if an EventCloud which is created two
 * times will use the same repository the second time when the property
 * EventCloudProperties.REPOSITORIES_RESTORE is set to true.
 * 
 * @author lpellegr
 */
public class DatastoreRestorationTest {

    @Before
    public void setUp() throws IOException {
        EventCloudProperties.REPOSITORIES_RESTORE.setValue(true);
        EventCloudProperties.REPOSITORIES_PATH.setValue(System.getProperty("java.io.tmpdir")
                + File.separator + "eventcloud-restoration-test");
        
        Files.deleteDirectory(EventCloudProperties.REPOSITORIES_PATH.getValue());
    }

    @Test
    public void testRestoration() throws ProActiveException,
            EventCloudIdNotManaged {
        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();
        String registryUrl = registry.register("registry");

        EventCloudDeployer deployer = this.createEventCloudDeployer();
        deployer.deploy(1, 1);
        registry.register(deployer);

        PutGetApi proxy =
                ProxyFactory.newPutGetProxy(
                        registryUrl, deployer.getEventCloudDescription()
                                .getId());

        proxy.add(QuadrupleGenerator.random());

        registry.undeploy(deployer.getEventCloudDescription().getId());

        // creates a new EventCloud with the same stream url
        deployer = this.createEventCloudDeployer();
        deployer.deploy(1, 1);
        registry.register(deployer);

        proxy =
                ProxyFactory.newPutGetProxy(
                        registryUrl, deployer.getEventCloudDescription()
                                .getId());

        Assert.assertEquals(1, proxy.find(QuadruplePattern.ANY).size());

        registry.undeploy(deployer.getEventCloudDescription().getId());
    }

    @After
    public void tearDown() {
        EventCloudProperties.REPOSITORIES_RESTORE.setValue(false);
        EventCloudProperties.REPOSITORIES_PATH.setValue(EventCloudProperties.REPOSITORIES_PATH.getDefaultValue());
    }

    private JunitEventCloudDeployer createEventCloudDeployer() {
        return new JunitEventCloudDeployer(
                new EventCloudDescription("http://example.org/stream"),
                new EventCloudDeploymentDescriptor(new SemanticOverlayProvider(
                        false)));
    }

}
