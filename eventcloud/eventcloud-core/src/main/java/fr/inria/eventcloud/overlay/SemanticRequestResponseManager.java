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
package fr.inria.eventcloud.overlay;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.MessageId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanRequestResponseManager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastoreBuilder;
import fr.inria.eventcloud.reasoner.SparqlColander;

/**
 * {@link SemanticRequestResponseManager} is an implementation of
 * {@link CanRequestResponseManager} for managing SPARQL queries over a CAN
 * network.
 * 
 * @author lpellegr
 */
public class SemanticRequestResponseManager extends CanRequestResponseManager {

    private static final long serialVersionUID = 160L;

    private SparqlColander colander;

    private final ConcurrentHashMap<MessageId, Future<? extends Object>> pendingResults;

    // this thread pool is used to execute an atomic query on the underlying
    // semantic datastore while the request continue to be forwarded to
    // others peers. The thread that is used is joined once a response is routed
    // back through this peer to retrieve the results
    public ExecutorService threadPool;

    public SemanticRequestResponseManager(
            TransactionalTdbDatastore colanderDatastore) {
        super();

        this.colander = new SparqlColander(colanderDatastore);

        this.pendingResults =
                new ConcurrentHashMap<MessageId, Future<? extends Object>>(
                        16, 0.75f,
                        P2PStructuredProperties.MAO_LIMIT_PEERS.getValue());

        this.threadPool =
                Executors.newFixedThreadPool(
                        Runtime.getRuntime().availableProcessors(),
                        new ThreadFactoryBuilder().setNameFormat(
                                this.getClass().getSimpleName()
                                        + "-pool-thread-%d").build());
    }

    public ConcurrentHashMap<MessageId, Future<? extends Object>> getPendingResults() {
        return this.pendingResults;
    }

    public SparqlColander getColander() {
        // TODO: rework, that's a temporary enhancement
        if (EventCloudProperties.COLANDER_IN_MEMORY.getValue()) {
            return new SparqlColander(
                    new TransactionalTdbDatastoreBuilder().build());
        } else {
            return this.colander;
        }
    }

    @Override
    public void clear() {
        super.clear();

        this.pendingResults.clear();

        TransactionalDatasetGraph txnGraph =
                this.colander.getDatastore().begin(AccessMode.WRITE);
        txnGraph.delete(QuadruplePattern.ANY);
        txnGraph.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        super.close();

        try {
            this.colander.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.threadPool.shutdown();

            try {
                this.threadPool.awaitTermination(2, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}
