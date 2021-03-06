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

import org.objectweb.proactive.extensions.p2p.structured.utils.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.tdb.StoreConnection;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.transaction.TDBTransactionException;

import fr.inria.eventcloud.datastore.stats.StatsRecorder;

/**
 * Wraps an instance of {@link StoreConnection}. As for {@link StoreConnection},
 * only one JVM at a time can access to a given datastore location.
 * 
 * @author lpellegr
 */
public class TransactionalTdbDatastore extends Datastore {

    private static final Logger LOG =
            LoggerFactory.getLogger(TransactionalTdbDatastore.class);

    private final boolean autoRemove;

    private Dataset dataset;

    private Location location;

    private final StatsRecorder statsRecorder;

    /**
     * Creates a new datastore that stores data into the specified
     * {@code repositoryPath}.
     * 
     * @param location
     *            the path where the internal datastore files are created.
     * 
     * @param statsRecorder
     *            the stats recorder instance to use.
     * 
     * @param autoRemove
     *            indicates whether the repository has to be removed or not when
     *            it is closed.
     */
    protected TransactionalTdbDatastore(Location location,
            StatsRecorder statsRecorder, boolean autoRemove) {

        this.location = location;
        this.autoRemove = autoRemove;
        this.statsRecorder = statsRecorder;

        this.registerPlugins();
    }

    /**
     * In-memory store for testing purpose only.
     */
    protected TransactionalTdbDatastore(StatsRecorder statsRecorder) {
        this(null, statsRecorder, false);
    }

    public TransactionalDatasetGraph begin(AccessMode mode) {
        this.dataset.begin(mode.toJena());
        return new TransactionalDatasetGraphImpl(
                this.dataset, this.statsRecorder);
    }

    private void registerPlugins() {
        // TypeMapper.getInstance().registerDatatype(
        // VariableDatatype.getInstance());

        FunctionRegistry.get().put(
                WithoutPrefixFunction.URI, WithoutPrefixFunction.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void _open() {
        if (this.location != null && !this.location.isMem()) {
            // creates location directory if it does not already exist
            new File(this.location.getDirectoryPath()).mkdirs();

            this.dataset = TDBFactory.createDataset(this.location);
        } else {
            this.dataset = TDBFactory.createDataset();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void _close() {
        boolean released = false;

        // TODO: avoid active wait. The best solution consist in providing a
        // patch to Jena in order to force a wait by using wait/notify mechanism
        // when Location#release is called
        while (!released) {
            try {
                TDBFactory.release(this.dataset);
                released = true;
            } catch (TDBTransactionException e) {
                // it is only used to detect that they are still some
                // active transactions to close before to release
                Thread.yield();
            }
        }

        if (this.autoRemove) {
            try {
                Files.deleteDirectory(this.location.getDirectoryPath());
            } catch (IOException e) {
                LOG.error("The deletion of the repository "
                        + this.location.getDirectoryPath() + " has failed", e);
            }
        }
    }

    public StatsRecorder getStatsRecorder() {
        return this.statsRecorder;
    }

    public boolean isMemory() {
        return this.location == null || this.location.isMemUnique()
                || this.location.isMem();
    }

}
