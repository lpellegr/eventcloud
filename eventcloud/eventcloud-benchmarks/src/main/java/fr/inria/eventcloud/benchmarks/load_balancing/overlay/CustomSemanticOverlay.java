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

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.JoinIntroduceOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

import fr.inria.eventcloud.benchmarks.load_balancing.BenchmarkStatsCollector;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.SemanticData;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Custom overlay to maintain some measurements.
 * 
 * @author lpellegr
 */
public class CustomSemanticOverlay extends SemanticCanOverlay {

    private final BenchmarkStatsCollector collector;

    public CustomSemanticOverlay(BenchmarkStatsCollector collector,
            TransactionalTdbDatastore subscriptionsDatastore,
            TransactionalTdbDatastore miscDatastore,
            TransactionalTdbDatastore colanderDatastore) {
        super(subscriptionsDatastore, miscDatastore, colanderDatastore);
        this.collector = collector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void join(Peer landmarkPeer) {
        super.join(landmarkPeer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assignDataReceived(Serializable dataReceived) {
        super.assignDataReceived(dataReceived);

        this.collector.report(
                super.id, ((SemanticData) dataReceived).getMiscData().size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EmptyResponseOperation handleJoinIntroduceOperation(JoinIntroduceOperation<SemanticCoordinate> msg) {
        EmptyResponseOperation response =
                super.handleJoinIntroduceOperation(msg);

        if (super.getLoadBalancingManager() != null) {
            this.collector.report(super.id, (int) super.getMiscDatastore()
                    .getStatsRecorder()
                    .getNbQuadruples());
        }

        return response;
    };

}
