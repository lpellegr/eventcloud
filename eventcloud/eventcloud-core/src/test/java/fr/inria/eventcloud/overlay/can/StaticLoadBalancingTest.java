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
package fr.inria.eventcloud.overlay.can;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.stats.CentroidStatsRecorder;
import fr.inria.eventcloud.datastore.stats.MeanStatsRecorder;
import fr.inria.eventcloud.datastore.stats.StatsRecorder;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.operations.can.GetStatsRecordeResponseOperation;
import fr.inria.eventcloud.operations.can.GetStatsRecorderOperation;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;

/**
 * Tests for static load-balancing.
 * 
 * @author lpellegr
 */
public class StaticLoadBalancingTest {

    private static final Logger log =
            LoggerFactory.getLogger(StaticLoadBalancingTest.class);

    @Rule
    public TestRule globalTimeout = new Timeout(300000); // 5 min

    /*
     * Two peers, load-balancing and stats recording disabled. Short RDF terms length.
     */
    @org.junit.Test
    public void testStaticLoadBalancing1() {
        new StaticLoadBalancingTestBuilder(1000, 10).build().execute();
    }

    /*
     * Two peers, load-balancing disabled but stats recording enabled. MeanStatsRecorder. Short RDF terms length.
     */
    @org.junit.Test
    public void testStaticLoadBalancing2() {
        new StaticLoadBalancingTestBuilder(1000, 10).enableStatsRecording(
                MeanStatsRecorder.class).build().execute();
    }

    /*
     * Two peers, load-balancing and stats recording disabled. Normal RDF term length.
     */
    @org.junit.Test
    public void testStaticLoadBalancing3() {
        new StaticLoadBalancingTestBuilder(1000, 100).build().execute();
    }

    /*
     * Two peers, load-balancing disabled but stats recording enabled. MeanStatsRecorder. Normal RDF term length.
     */
    @org.junit.Test
    public void testStaticLoadBalancing4() {
        new StaticLoadBalancingTestBuilder(1000, 100).enableStatsRecording(
                MeanStatsRecorder.class).build().execute();
    }

    /*
     * Two peers, load-balancing enabled. MeanStatsRecorder. Short RDF terms length.
     */
    @org.junit.Test
    public void testStaticLoadBalancing5() {
        new StaticLoadBalancingTestBuilder(1000, 10).enableLoadBalancing(
                MeanStatsRecorder.class)
                .setNbPeersToInject(1)
                .build()
                .execute();
    }

    /*
     * Two peers, load-balancing enabled. MeanStatsRecorder. Short RDF terms length. Simulate compound events.
     */
    @org.junit.Test
    public void testStaticLoadBalancing6() {
        new StaticLoadBalancingTestBuilder(1000, 10).enableLoadBalancing(
                MeanStatsRecorder.class)
                .simulateCompoundEvents(10)
                .setNbPeersToInject(1)
                .build()
                .execute();
    }

    /*
     * Two peers, load-balancing enabled. CentroidStatsRecorder -> 2/3 short and 1/3 normal RDF terms length.
     */
    @org.junit.Test
    public void testStaticLoadBalancing7() {
        new StaticLoadBalancingTestBuilder(3000, 10).enableLoadBalancing(
                CentroidStatsRecorder.class)
                .setNbPeersToInject(1)
                .build()
                .execute();
    }

    /*
     * Two peers, load-balancing enabled. MeanStatsRecorder. Inject 10 peers and performs 100 lookups.
     */
    @org.junit.Test
    public void testStaticLoadBalancing8() {
        new StaticLoadBalancingTestBuilder(1000, 10).enableLoadBalancing(
                MeanStatsRecorder.class)
                .setNbPeersToInject(10)
                .setNbLookupAfterJoinOperations(100)
                .build()
                .execute();
    }

    private static class StaticLoadBalancingTestBuilder {

        private boolean enableLoadBalancing = false;

        private boolean enableStatsRecording = false;

        private int nbLookupsAfterJoinOperations = -1;

        private int nbPeersToInject = 0;

        private int nbQuadsPerCompoundEvent = -1;

        private int nbQuadsToInsert;

        private int rdfTermSize;

        private Class<? extends StatsRecorder> statsRecorderClass;

        public StaticLoadBalancingTestBuilder(int nbQuadsToInsert,
                int rdfTermSize) {
            Preconditions.checkArgument(
                    nbQuadsToInsert > 0, "Invalid nbQuadsToInsert length: "
                            + nbQuadsToInsert);
            Preconditions.checkArgument(
                    rdfTermSize > 0, "Invalid rdfTermSize length: "
                            + rdfTermSize);

            this.nbQuadsToInsert = nbQuadsToInsert;
            this.rdfTermSize = rdfTermSize;
        }

        public StaticLoadBalancingTestBuilder setNbLookupAfterJoinOperations(int n) {
            this.nbLookupsAfterJoinOperations = n;
            return this;
        }

        public StaticLoadBalancingTestBuilder setNbPeersToInject(int n) {
            this.nbPeersToInject = n;
            return this;
        }

        public StaticLoadBalancingTestBuilder enableStatsRecording(Class<? extends StatsRecorder> statsRecorderClass) {
            this.enableStatsRecording = true;
            this.statsRecorderClass = statsRecorderClass;
            return this;
        }

        public StaticLoadBalancingTestBuilder enableLoadBalancing(Class<? extends StatsRecorder> statsRecorderClass) {
            this.enableLoadBalancing = true;
            this.enableStatsRecording = true;
            this.statsRecorderClass = statsRecorderClass;
            return this;
        }

        public StaticLoadBalancingTestBuilder simulateCompoundEvents(int nbQuadsPerCompoundEvent) {
            this.nbQuadsPerCompoundEvent = nbQuadsPerCompoundEvent;
            return this;
        }

        public Test build() {
            return new Test() {

                @Override
                protected void _execute() {
                    if (StaticLoadBalancingTestBuilder.this.enableStatsRecording) {
                        EventCloudProperties.RECORD_STATS_MISC_DATASTORE.setValue(true);
                    }

                    if (StaticLoadBalancingTestBuilder.this.enableLoadBalancing) {
                        EventCloudProperties.STATIC_LOAD_BALANCING.setValue(true);
                    }

                    if (StaticLoadBalancingTestBuilder.this.statsRecorderClass != null) {
                        EventCloudProperties.STATS_RECORDER_CLASS.setValue(StaticLoadBalancingTestBuilder.this.statsRecorderClass);
                    }

                    this.eventCloudId = this.deployer.newEventCloud(1, 1);

                    SemanticPeer firstPeer =
                            this.deployer.getRandomSemanticPeer(this.eventCloudId);

                    PutGetApi putgetProxy = null;
                    try {
                        putgetProxy =
                                ProxyFactory.newPutGetProxy(
                                        this.deployer.getEventCloudsRegistryUrl(),
                                        this.eventCloudId);
                    } catch (EventCloudIdNotManaged e) {
                        throw new IllegalStateException(e);
                    }

                    Stopwatch stopwatch = new Stopwatch();
                    stopwatch.start();

                    Node graph = null;

                    if (this.simulateCompoundEvents()) {
                        graph =
                                NodeGenerator.randomUri(StaticLoadBalancingTestBuilder.this.rdfTermSize);
                    }

                    int tmpNbQuadsToInsert =
                            StaticLoadBalancingTestBuilder.this.nbQuadsToInsert;
                    if (this.isCentroidStatsRecorderUsed()) {
                        tmpNbQuadsToInsert =
                                StaticLoadBalancingTestBuilder.this.nbQuadsToInsert / 3 * 2;
                    }

                    for (int i = 0; i < tmpNbQuadsToInsert; i++) {
                        Quadruple quad = null;

                        if (this.simulateCompoundEvents()
                                && i
                                        % StaticLoadBalancingTestBuilder.this.nbQuadsPerCompoundEvent == 0) {
                            graph =
                                    NodeGenerator.randomUri(StaticLoadBalancingTestBuilder.this.rdfTermSize);
                        }

                        quad =
                                this.buildQuadruple(
                                        graph,
                                        StaticLoadBalancingTestBuilder.this.rdfTermSize);

                        putgetProxy.add(quad);
                    }

                    if (this.isCentroidStatsRecorderUsed()) {
                        // add 1/3 of the data which are 10 times longer
                        int longRdfTermSize =
                                StaticLoadBalancingTestBuilder.this.rdfTermSize * 10;

                        if (this.simulateCompoundEvents()) {
                            graph = NodeGenerator.randomUri(longRdfTermSize);
                        }

                        for (int i = 0; i < StaticLoadBalancingTestBuilder.this.nbQuadsToInsert / 3; i++) {
                            Quadruple quad = null;

                            if (this.simulateCompoundEvents()
                                    && i
                                            % StaticLoadBalancingTestBuilder.this.nbQuadsPerCompoundEvent == 0) {
                                graph =
                                        NodeGenerator.randomUri(longRdfTermSize);
                            }

                            quad = this.buildQuadruple(graph, longRdfTermSize);

                            putgetProxy.add(quad);
                        }
                    }

                    stopwatch.stop();

                    log.info(
                            "It took {} to insert {} quadruples",
                            stopwatch.toString(),
                            StaticLoadBalancingTestBuilder.this.nbQuadsToInsert);

                    if (StaticLoadBalancingTestBuilder.this.nbPeersToInject > 0) {
                        log.info("Before join, first peer dump:\n"
                                + firstPeer.dump());

                        for (int i = 0; i < StaticLoadBalancingTestBuilder.this.nbPeersToInject; i++) {
                            long maxNumQuads = -1;
                            Peer electedPeer = null;

                            // we select the peer which has the higher number of
                            // quadruples in the misc datastore in order to
                            // perform the next split
                            for (Peer p : this.deployer.getRandomSemanticTracker(
                                    this.eventCloudId)
                                    .getPeers()) {
                                GetStatsRecordeResponseOperation response =
                                        (GetStatsRecordeResponseOperation) PAFuture.getFutureValue(p.receive(new GetStatsRecorderOperation()));

                                if (response.getStatsRecorder().getNbQuads() > maxNumQuads) {
                                    maxNumQuads =
                                            response.getStatsRecorder()
                                                    .getNbQuads();
                                    electedPeer = p;
                                }
                            }

                            Peer newPeer =
                                    SemanticFactory.newSemanticPeer(new SemanticInMemoryOverlayProvider());

                            try {
                                newPeer.join(electedPeer);
                            } catch (NetworkAlreadyJoinedException e) {
                                throw new IllegalStateException(e);
                            }

                            this.deployer.getRandomSemanticTracker(
                                    this.eventCloudId).storePeer(newPeer);
                            log.info("Join operation " + (i + 1));
                        }

                        log.info("After injections, other peers dump:\n");
                        for (Peer p : this.deployer.getRandomSemanticTracker(
                                this.eventCloudId).getPeers()) {
                            log.info(p.dump());
                        }

                        if (StaticLoadBalancingTestBuilder.this.nbLookupsAfterJoinOperations > 0) {
                            for (int i = 0; i < StaticLoadBalancingTestBuilder.this.nbLookupsAfterJoinOperations; i++) {
                                Assert.assertEquals(
                                        StaticLoadBalancingTestBuilder.this.nbQuadsToInsert,
                                        putgetProxy.find(QuadruplePattern.ANY)
                                                .size());
                            }
                        }
                    }
                }

                private Quadruple buildQuadruple(Node graph, int rdfTermSize) {
                    Quadruple quad;
                    if (this.simulateCompoundEvents()) {
                        quad = QuadrupleGenerator.random(graph, rdfTermSize);
                    } else {
                        quad = QuadrupleGenerator.random(rdfTermSize);
                    }
                    return quad;
                }

                private boolean isCentroidStatsRecorderUsed() {
                    return (StaticLoadBalancingTestBuilder.this.statsRecorderClass != null)
                            && (StaticLoadBalancingTestBuilder.this.statsRecorderClass.isAssignableFrom(CentroidStatsRecorder.class));
                }

                private boolean simulateCompoundEvents() {
                    return StaticLoadBalancingTestBuilder.this.nbQuadsPerCompoundEvent != -1;
                }

            };

        }
    }

    private static abstract class Test {

        protected JunitEventCloudInfrastructureDeployer deployer;

        protected EventCloudId eventCloudId;

        public Test() {
            this.deployer = new JunitEventCloudInfrastructureDeployer();
        }

        public void tearDown() {
            this.deployer.undeploy();
        }

        protected abstract void _execute();

        public void execute() {
            this._execute();
            this.tearDown();
        }

    }

}
