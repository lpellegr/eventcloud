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
package fr.inria.eventcloud.datastore;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.datastore.Datastore;
import org.objectweb.proactive.extensions.p2p.structured.utils.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.tdb.StoreConnection;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.transaction.TDBTransactionException;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * Wraps an instance of {@link StoreConnection}. As for {@link StoreConnection},
 * only one JVM at a time can access to a given datastore location.
 * 
 * @author lpellegr
 */
public class TransactionalTdbDatastore extends Datastore {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionalTdbDatastore.class);

    private final StoreConnection storeConnection;

    private final boolean autoRemove;

    /**
     * Creates a new datastore that stores data into the specified
     * {@code repositoryPath}.
     * 
     * @param location
     *            the path where the internal datastore files are created.
     * 
     * @param autoRemove
     *            indicates whether the repository has to be removed or not when
     *            it is closed.
     */
    public TransactionalTdbDatastore(String location, boolean autoRemove) {
        this(new Location(location), autoRemove);
    }

    protected TransactionalTdbDatastore(Location location, boolean autoRemove) {
        if (!location.isMem()) {
            new File(location.getDirectoryPath()).mkdirs();
        }

        this.storeConnection = StoreConnection.make(location);
        this.autoRemove = autoRemove;

        this.registerPlugins();
    }

    protected TransactionalTdbDatastore(StoreConnection storeConnection,
            boolean autoRemove) {
        this.storeConnection = storeConnection;
        this.autoRemove = autoRemove;

        this.registerPlugins();
    }

    public TransactionalDatasetGraph begin(AccessMode mode) {
        return new TransactionalDatasetGraphImpl(
                this.storeConnection.begin(mode.toJena()));
    }

    private void registerPlugins() {
        TypeMapper.getInstance().registerDatatype(
                VariableDatatype.getInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalOpen() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalClose() {
        boolean released = false;

        // TODO: avoid active wait. The best solution consist in providing a
        // patch to Jena in order to force a wait by using wait/notify mechanism
        // when Location#release is called
        while (!released) {
            try {
                StoreConnection.release(this.storeConnection.getLocation());
                released = true;
            } catch (TDBTransactionException e) {
                // it is only used to detect that they are still some
                // active transactions to close before to release
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (this.autoRemove) {
            try {
                Files.deleteDirectory(this.storeConnection.getLocation()
                        .getDirectoryPath());
            } catch (IOException e) {
                log.error("The deletion of the repository "
                        + this.storeConnection.getLocation().getDirectoryPath()
                        + " has failed", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> retrieveDataIn(Object interval) {
        return this.retrieveDataIn(interval, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> removeDataIn(Object interval) {
        return this.retrieveDataIn(interval, true);
    }

    private Collection<Quadruple> retrieveDataIn(Object interval, boolean remove) {
        Zone zone = (Zone) interval;
        String graph, subject, predicate, object;

        Collection<Quadruple> result = new Collection<Quadruple>();
        Collection<Quadruple> quadruplesToRemove = null;
        if (remove) {
            quadruplesToRemove = new Collection<Quadruple>();
        }

        TransactionalDatasetGraph txnGraph = this.begin(AccessMode.READ_ONLY);
        for (Quadruple quad : txnGraph.find(QuadruplePattern.ANY)) {
            graph = SemanticElement.parseElement(quad.getGraph().toString());
            subject =
                    SemanticElement.parseElement(quad.getSubject().toString());
            predicate =
                    SemanticElement.parseElement(quad.getPredicate().toString());
            object = SemanticElement.parseElement(quad.getObject().toString());

            if (graph.compareTo(zone.getLowerBound((byte) 0).getValue()) >= 0
                    && graph.compareTo(zone.getUpperBound((byte) 0).getValue()) < 0
                    && subject.compareTo(zone.getLowerBound((byte) 1)
                            .getValue()) >= 0
                    && subject.compareTo(zone.getUpperBound((byte) 1)
                            .getValue()) < 0
                    && predicate.compareTo(zone.getLowerBound((byte) 2)
                            .getValue()) >= 0
                    && predicate.compareTo(zone.getUpperBound((byte) 2)
                            .getValue()) < 0
                    && object.compareTo(zone.getLowerBound((byte) 3).getValue()) >= 0
                    && object.compareTo(zone.getUpperBound((byte) 3).getValue()) < 0) {
                result.add(quad);

                if (remove) {
                    quadruplesToRemove.add(quad);
                }
            }
        }
        txnGraph.close();

        if (remove) {
            txnGraph = this.begin(AccessMode.WRITE);
            for (Quadruple q : quadruplesToRemove) {
                txnGraph.delete(q);
            }
            txnGraph.commit();
            txnGraph.close();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void affectDataReceived(Object dataReceived) {
        TransactionalDatasetGraph txnGraph = this.begin(AccessMode.WRITE);
        txnGraph.add((Collection<Quadruple>) dataReceived);
        txnGraph.commit();
        txnGraph.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> retrieveAllData() {
        Collection<Quadruple> result = null;

        TransactionalDatasetGraph txnGraph = this.begin(AccessMode.READ_ONLY);
        result = Collection.from(txnGraph.find(QuadruplePattern.ANY));
        txnGraph.close();

        return result;
    }

}
