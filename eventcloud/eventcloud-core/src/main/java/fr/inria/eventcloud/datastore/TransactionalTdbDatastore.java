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
package fr.inria.eventcloud.datastore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.overlay.datastore.Datastore;
import org.objectweb.proactive.extensions.p2p.structured.utils.Files;
import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.tdb.StoreConnection;
import com.hp.hpl.jena.tdb.base.block.FileMode;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.sys.SystemTDB;
import com.hp.hpl.jena.tdb.transaction.TDBTransactionException;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.overlay.can.SemanticZone;

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

    private final Location storeLocation;

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
        this.storeLocation = location;
        this.autoRemove = autoRemove;

        this.registerPlugins();
    }

    /**
     * In-memory store for testing purpose only.
     */
    protected TransactionalTdbDatastore() {
        this.storeConnection = StoreConnection.createMemUncached();
        this.storeLocation = null;
        this.autoRemove = false;

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
                if (this.storeLocation != null) {
                    StoreConnection.release(this.storeLocation);
                }
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
            if (SystemUtil.isWindows()
                    && SystemTDB.fileMode() == FileMode.mapped) {
                // FIXME: TDB uses mapped files and due to a Java bug
                // (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154),
                // the repository cannot be removed before to force garbage
                // collection
                System.gc();
            }

            try {
                Files.deleteDirectory(this.storeLocation.getDirectoryPath());
            } catch (IOException e) {
                log.error(
                        "The deletion of the repository "
                                + this.storeLocation.getDirectoryPath()
                                + " has failed", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> retrieveDataIn(Object interval) {
        return this.retrieveDataIn(interval, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> removeDataIn(Object interval) {
        return this.retrieveDataIn(interval, true);
    }

    private List<Quadruple> retrieveDataIn(Object interval, boolean remove) {
        SemanticZone zone = (SemanticZone) interval;
        SemanticElement graph, subject, predicate, object;

        List<Quadruple> result = new ArrayList<Quadruple>();
        List<Quadruple> quadruplesToRemove = null;
        if (remove) {
            quadruplesToRemove = new ArrayList<Quadruple>();
        }

        TransactionalDatasetGraph txnGraph = this.begin(AccessMode.READ_ONLY);
        try {
            QuadrupleIterator it = txnGraph.find(QuadruplePattern.ANY);

            while (it.hasNext()) {
                Quadruple quad = it.next();

                graph = new SemanticElement(quad.getGraph().toString());
                subject = new SemanticElement(quad.getSubject().toString());
                predicate = new SemanticElement(quad.getPredicate().toString());
                object = new SemanticElement(quad.getObject().toString());

                if (graph.compareTo(zone.getLowerBound((byte) 0)) >= 0
                        && graph.compareTo(zone.getUpperBound((byte) 0)) < 0
                        && subject.compareTo(zone.getLowerBound((byte) 1)) >= 0
                        && subject.compareTo(zone.getUpperBound((byte) 1)) < 0
                        && predicate.compareTo(zone.getLowerBound((byte) 2)) >= 0
                        && predicate.compareTo(zone.getUpperBound((byte) 2)) < 0
                        && object.compareTo(zone.getLowerBound((byte) 3)) >= 0
                        && object.compareTo(zone.getUpperBound((byte) 3)) < 0) {
                    result.add(quad);

                    if (remove) {
                        quadruplesToRemove.add(quad);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        if (remove) {
            txnGraph = this.begin(AccessMode.WRITE);
            try {
                for (Quadruple q : quadruplesToRemove) {
                    txnGraph.delete(q);
                }
                txnGraph.commit();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                txnGraph.end();
            }
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

        try {
            txnGraph.add((List<Quadruple>) dataReceived);
            txnGraph.commit();
        } finally {
            txnGraph.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> retrieveAllData() {
        List<Quadruple> result = null;

        TransactionalDatasetGraph txnGraph = this.begin(AccessMode.READ_ONLY);

        try {
            result = Lists.newArrayList(txnGraph.find(QuadruplePattern.ANY));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        return result;
    }

}
