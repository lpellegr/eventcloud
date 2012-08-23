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

import org.objectweb.proactive.extensions.p2p.structured.utils.Files;
import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.tdb.StoreConnection;
import com.hp.hpl.jena.tdb.base.block.FileMode;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.sys.SystemTDB;
import com.hp.hpl.jena.tdb.transaction.TDBTransactionException;

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

    public TransactionalTdbDatastore(File location, boolean autoRemove) {
        this(location.getAbsolutePath(), autoRemove);
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
    protected void _open() {

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

}
