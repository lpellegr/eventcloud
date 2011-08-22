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

import org.objectweb.proactive.extensions.p2p.structured.utils.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * A persistent Jena datastore that relies on Jena TDB.
 * 
 * @author lpellegr
 */
public class PersistentJenaTdbDatastore extends SynchronizedJenaDatasetGraph {

    private static final Logger log =
            LoggerFactory.getLogger(PersistentJenaTdbDatastore.class);

    private final File repositoryPath;

    private final boolean autoRemove;

    /**
     * Creates a new persistent datastore that stores data into a folder from
     * the Operating System temporary directory.
     */
    public PersistentJenaTdbDatastore() {
        this(new File(System.getProperty("java.io.tmpdir")), true);
    }

    /**
     * Creates a new datastore that stores data into the specified
     * {@code repositoryPath}.
     * 
     * @param repositoryPath
     *            the path where to store the data.
     * 
     * @param autoRemove
     *            indicates whether the repository has to be removed or not when
     *            it is closed.
     */
    public PersistentJenaTdbDatastore(File repositoryPath, boolean autoRemove) {
        super();
        this.autoRemove = autoRemove;
        this.repositoryPath = new File(repositoryPath, super.id.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DatasetGraph createDatasetGraph() {
        return TDBFactory.createDatasetGraph(this.repositoryPath.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalOpen() {
        if (!this.repositoryPath.exists()) {
            this.repositoryPath.mkdirs();
        }

        super.datastore = this.createDatasetGraph();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalClose() {
        this.datastore.close();

        if (this.autoRemove) {
            try {
                Files.deleteDirectory(this.repositoryPath);
                log.info("Repository {} has been deleted", this.repositoryPath);
            } catch (IOException e) {
                log.error("The deletion of the repository "
                        + this.repositoryPath + " has failed", e);
            }
        }
    }

}
