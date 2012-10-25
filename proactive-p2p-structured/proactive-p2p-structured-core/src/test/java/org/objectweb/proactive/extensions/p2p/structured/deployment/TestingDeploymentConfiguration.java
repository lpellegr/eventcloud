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

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;

/**
 * Configure a {@link NetworkDeployer} for testing purpose. In that case some
 * properties like for example
 * {@link P2PStructuredProperties#TRACKER_STORAGE_PROBABILITY} is set to
 * {@code 1}.
 * 
 * @author lpellegr
 */
public final class TestingDeploymentConfiguration implements
        DeploymentConfiguration {

    private static final long serialVersionUID = 130L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        P2PStructuredProperties.TRACKER_STORAGE_PROBABILITY.setValue(1.0);
    }

}
