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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.stats.CentroidStatsRecorder;
import fr.inria.eventcloud.datastore.stats.StatsRecorder;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.operations.can.GetStatsRecordeResponseOperation;
import fr.inria.eventcloud.operations.can.GetStatsRecorderOperation;
import fr.inria.eventcloud.operations.can.Operations;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;
import fr.inria.eventcloud.utils.Callback;

public class StaticLoadBalancingTestBuilder {

    private static final Logger log =
            LoggerFactory.getLogger(StaticLoadBalancingTestBuilder.class);

    private boolean enableLoadBalancing = false;

    private boolean enableStatsRecording = false;

    private boolean insertSkewedData = false;

    private int nbLookupsAfterJoinOperations = -1;

    private int nbPeersToInject = 0;

    private int nbQuadsPerCompoundEvent = -1;

    private final int nbQuadsToInsert;

    private final int rdfTermSize;

    private Class<? extends StatsRecorder> statsRecorderClass;

    private String trigResource;

    public StaticLoadBalancingTestBuilder(String trigResource) {
        this.trigResource = trigResource;
        this.nbQuadsToInsert = 1000;
        this.rdfTermSize = 10;
    }

    public StaticLoadBalancingTestBuilder(int nbQuadsToInsert, int rdfTermSize) {
        Preconditions.checkArgument(
                nbQuadsToInsert > 0, "Invalid nbQuadsToInsert length: "
                        + nbQuadsToInsert);
        Preconditions.checkArgument(
                rdfTermSize > 0, "Invalid rdfTermSize length: " + rdfTermSize);

        this.nbQuadsToInsert = nbQuadsToInsert;
        this.rdfTermSize = rdfTermSize;
    }

    public StaticLoadBalancingTestBuilder insertSkewedData(boolean value) {
        this.insertSkewedData = value;
        return this;
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

            private static final String CENTROID_SHORT_RDF_TERM_PREFIX =
                    "http://aaa";

            private static final String CENTROID_LONG_RDF_TERM_PREFIX =
                    "http://zzz";

            @Override
            @SuppressWarnings("resource")
            protected void _execute() throws EventCloudIdNotManaged,
                    NetworkAlreadyJoinedException, FileNotFoundException,
                    PeerNotActivatedException {

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

                final PutGetApi putgetProxy =
                        ProxyFactory.newPutGetProxy(
                                this.deployer.getEventCloudsRegistryUrl(),
                                this.eventCloudId);

                final Stopwatch stopwatch = new Stopwatch();

                Node graph = null;

                if (StaticLoadBalancingTestBuilder.this.trigResource == null) {
                    if (this.simulateCompoundEvents()) {
                        graph =
                                NodeGenerator.randomUri(StaticLoadBalancingTestBuilder.this.rdfTermSize);
                    }

                    int tmpNbQuadsToInsert =
                            StaticLoadBalancingTestBuilder.this.nbQuadsToInsert;
                    if (this.isCentroidStatsRecorderUsed()
                            && StaticLoadBalancingTestBuilder.this.nbPeersToInject > 0) {
                        tmpNbQuadsToInsert =
                                StaticLoadBalancingTestBuilder.this.nbQuadsToInsert / 3 * 2;
                    }

                    for (int i = 0; i < tmpNbQuadsToInsert; i++) {
                        Quadruple quad = null;

                        if (this.simulateCompoundEvents()
                                && i
                                        % StaticLoadBalancingTestBuilder.this.nbQuadsPerCompoundEvent == 0) {
                            if (this.isCentroidStatsRecorderUsed()
                                    && StaticLoadBalancingTestBuilder.this.nbPeersToInject > 1) {
                                graph =
                                        NodeGenerator.randomUri(
                                                CENTROID_SHORT_RDF_TERM_PREFIX,
                                                StaticLoadBalancingTestBuilder.this.rdfTermSize);
                            } else {
                                graph =
                                        NodeGenerator.randomUri(StaticLoadBalancingTestBuilder.this.rdfTermSize);
                            }
                        }

                        quad =
                                this.buildQuadruple(
                                        graph,
                                        StaticLoadBalancingTestBuilder.this.rdfTermSize);

                        stopwatch.start();
                        putgetProxy.add(quad);
                        stopwatch.stop();
                    }
                } else {
                    InputStream is = null;

                    try {
                        is =
                                new FileInputStream(
                                        StaticLoadBalancingTestBuilder.this.trigResource);
                    } catch (FileNotFoundException e) {
                        is =
                                StaticLoadBalancingTest.class.getResourceAsStream(StaticLoadBalancingTestBuilder.this.trigResource);

                        if (is == null) {
                            throw e;
                        }
                    }

                    RdfParser.parse(
                            new BufferedInputStream(is),
                            SerializationFormat.TriG,
                            new Callback<Quadruple>() {
                                @Override
                                public void execute(Quadruple quad) {
                                    stopwatch.start();
                                    putgetProxy.add(quad);
                                    stopwatch.stop();
                                }
                            });
                }

                if (StaticLoadBalancingTestBuilder.this.insertSkewedData
                        && this.isCentroidStatsRecorderUsed()
                        && StaticLoadBalancingTestBuilder.this.nbPeersToInject > 0) {
                    // add 1/3 of the data which are 10 times longer
                    int longRdfTermSize =
                            StaticLoadBalancingTestBuilder.this.rdfTermSize * 10;

                    if (this.simulateCompoundEvents()) {
                        graph =
                                NodeGenerator.randomUri(
                                        CENTROID_LONG_RDF_TERM_PREFIX,
                                        longRdfTermSize);
                    }

                    for (int i = 0; i < StaticLoadBalancingTestBuilder.this.nbQuadsToInsert / 3; i++) {
                        Quadruple quad = null;

                        if (this.simulateCompoundEvents()
                                && i
                                        % StaticLoadBalancingTestBuilder.this.nbQuadsPerCompoundEvent == 0) {
                            graph =
                                    NodeGenerator.randomUri(CENTROID_LONG_RDF_TERM_PREFIX
                                            + longRdfTermSize);
                        }

                        quad = this.buildQuadruple(graph, longRdfTermSize);

                        stopwatch.start();
                        putgetProxy.add(quad);
                        stopwatch.stop();
                    }
                }

                log.info(
                        "It took {} to insert {} quadruples",
                        stopwatch.toString(),
                        StaticLoadBalancingTestBuilder.this.nbQuadsToInsert);

                this.executionTime = stopwatch.elapsedMillis();

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
                                this.eventCloudId).getPeers()) {
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

                        newPeer.join(electedPeer);

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
                } else {
                    log.info("Peer dump:\n" + firstPeer.dump());
                }
            }

            private Quadruple buildQuadruple(Node graph, int rdfTermSize) {
                if (this.simulateCompoundEvents()) {
                    if (this.isCentroidStatsRecorderUsed()
                            && StaticLoadBalancingTestBuilder.this.nbPeersToInject > 1) {
                        if (rdfTermSize > StaticLoadBalancingTestBuilder.this.rdfTermSize) {
                            return QuadrupleGenerator.randomWithoutLiteral(
                                    graph, CENTROID_LONG_RDF_TERM_PREFIX,
                                    rdfTermSize);
                        } else {
                            return QuadrupleGenerator.randomWithoutLiteral(
                                    graph, CENTROID_SHORT_RDF_TERM_PREFIX,
                                    rdfTermSize);
                        }
                    } else {
                        if (graph == null) {
                            return QuadrupleGenerator.randomWithoutLiteral(rdfTermSize);
                        } else {
                            return QuadrupleGenerator.randomWithoutLiteral(
                                    graph, rdfTermSize);
                        }
                    }
                } else {
                    if (this.isCentroidStatsRecorderUsed()
                            && StaticLoadBalancingTestBuilder.this.nbPeersToInject > 1) {
                        if (rdfTermSize > StaticLoadBalancingTestBuilder.this.rdfTermSize) {
                            return QuadrupleGenerator.randomWithoutLiteral(
                                    CENTROID_LONG_RDF_TERM_PREFIX, rdfTermSize);
                        } else {
                            return QuadrupleGenerator.randomWithoutLiteral(
                                    CENTROID_SHORT_RDF_TERM_PREFIX, rdfTermSize);
                        }
                    } else {
                        if (graph == null) {
                            return QuadrupleGenerator.randomWithoutLiteral(rdfTermSize);
                        } else {
                            return QuadrupleGenerator.randomWithoutLiteral(
                                    graph, rdfTermSize);
                        }
                    }
                }
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

    public static abstract class Test {

        protected JunitEventCloudInfrastructureDeployer deployer;

        protected EventCloudId eventCloudId;

        protected long executionTime;

        public Test() {
            this.deployer = new JunitEventCloudInfrastructureDeployer();
        }

        public void tearDown() {
            this.deployer.undeploy();
        }

        protected abstract void _execute() throws EventCloudIdNotManaged,
                NetworkAlreadyJoinedException, FileNotFoundException,
                PeerNotActivatedException;

        public void execute() {
            try {
                this._execute();

                if (EventCloudProperties.RECORD_STATS_MISC_DATASTORE.getValue()) {
                    Pair<String, Double> stats = this.computeStats();

                    logDistribution(stats.getFirst());
                    logStandardDeviation(stats.getSecond());
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                this.tearDown();
            }
        }

        private Pair<String, Double> computeStats() {
            SummaryStatistics stats = new SummaryStatistics();
            StringBuilder distribution = new StringBuilder();

            List<Peer> peers =
                    this.deployer.getRandomSemanticTracker(this.eventCloudId)
                            .getPeers();

            for (int i = 0; i < peers.size(); i++) {
                StatsRecorder statsRecorder =
                        Operations.getStatsRecorder(peers.get(i));
                stats.addValue(statsRecorder.getNbQuads());

                distribution.append(statsRecorder.getNbQuads());
                if (i < peers.size() - 1) {
                    distribution.append(' ');
                }
            }

            return Pair.create(
                    distribution.toString(), stats.getStandardDeviation());
        }

        private static void logStandardDeviation(double stdVar) {
            log.info("Standard deviation is {}", Precision.round(stdVar, 3));
        }

        private static void logDistribution(String distribution) {
            log.info("Distribution is [{}]", distribution);
        }

        public long getExecutionTime() {
            return this.executionTime;
        }

    }

}
