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
package fr.inria.eventcloud.deployment;

import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitHelper;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.api.EventCloudId;

/**
 * This class is used to specialize an {@link EventCloudDeployer} for unit
 * testing.
 * 
 * @author lpellegr
 */
public class JunitEventCloudDeployer extends EventCloudDeployer {

    private static final long serialVersionUID = 160L;

    /**
     * Creates a new {@link JunitEventCloudDeployer} by using the specified
     * {@code DeploymentDescriptor}.
     * 
     * @param deploymentDescriptor
     *            the deployment descriptor to use.
     */
    public JunitEventCloudDeployer(
            EventCloudDeploymentDescriptor deploymentDescriptor) {
        this(new EventCloudDescription(new EventCloudId()),
                deploymentDescriptor);
    }

    /**
     * Creates a new {@link JunitEventCloudDeployer} by using the specified
     * {@code DeploymentDescriptor}.
     * 
     * @param eventCloudDescription
     *            the EventCloud description to use.
     * @param deploymentDescriptor
     *            the deployment descriptor to use.
     */
    public JunitEventCloudDeployer(EventCloudDescription eventCloudDescription,
            EventCloudDeploymentDescriptor deploymentDescriptor) {
        super(
                eventCloudDescription,
                JunitHelper.setTestingDeploymentConfiguration(deploymentDescriptor));
    }

}
