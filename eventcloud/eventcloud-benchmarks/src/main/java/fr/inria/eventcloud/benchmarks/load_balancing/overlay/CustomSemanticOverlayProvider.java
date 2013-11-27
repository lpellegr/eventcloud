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
package fr.inria.eventcloud.benchmarks.load_balancing.overlay;

import fr.inria.eventcloud.benchmarks.load_balancing.BenchmarkStatsCollector;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.load_balancing.configuration.LoadBalancingConfiguration;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.providers.SemanticOverlayProvider;

public class CustomSemanticOverlayProvider extends SemanticOverlayProvider {

    private static final long serialVersionUID = 160L;

    private final String statsCollectorURL;

    public CustomSemanticOverlayProvider(
            LoadBalancingConfiguration loadBalancingConfiguration,
            String statsCollectorURL, int nbQuadruplesExpected, boolean inMemory) {
        super(loadBalancingConfiguration, inMemory);

        this.statsCollectorURL = statsCollectorURL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticCanOverlay get() {
        TransactionalTdbDatastore[] datastores = this.createDatastores();

        CustomSemanticOverlay result =
                new CustomSemanticOverlay(
                        BenchmarkStatsCollector.lookup(this.statsCollectorURL),
                        datastores[0], datastores[1], datastores[2]);

        return result;
    }

}
