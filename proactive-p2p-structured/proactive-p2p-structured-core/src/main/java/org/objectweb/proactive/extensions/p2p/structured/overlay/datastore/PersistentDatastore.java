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
package org.objectweb.proactive.extensions.p2p.structured.overlay.datastore;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.extensions.p2p.structured.utils.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A persistent datastore is a datastore that writes data on a disk.
 * 
 * @author lpellegr
 */
public abstract class PersistentDatastore extends Datastore {

    private static final Logger log =
            LoggerFactory.getLogger(PersistentDatastore.class);

    protected final File path;

    protected final boolean autoRemove;

    protected PersistentDatastore(File parentPath, boolean autoRemove) {
        super();
        this.path = new File(parentPath, super.id.toString());
        this.autoRemove = autoRemove;

        if (autoRemove) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    if (initialized.get()) {
                        close();
                    }
                }
            });
        }
    }

    /**
     * Returns the absolute path to the repository on the local filesystem.
     * 
     * @return the absolute path to the repository on the local filesystem.
     */
    public File getPath() {
        return this.path;
    }

    /**
     * Closes the datastore and removes the repository from the disk if the
     * {@code remove} parameter is set to {@code true}.
     */
    @Override
    public void close() {
        if (!super.initialized.compareAndSet(true, false)) {
            throw new IllegalStateException("datastore not initialized");
        } else {
            if (this.autoRemove) {
                try {
                    Files.deleteDirectory(this.path);
                    log.info("Repository {} has been deleted", this.path);
                } catch (IOException e) {
                    log.error("The deletion of the repository " + this.path
                            + " has failed", e);
                }
            }

            this.internalClose();
        }
    }

}
