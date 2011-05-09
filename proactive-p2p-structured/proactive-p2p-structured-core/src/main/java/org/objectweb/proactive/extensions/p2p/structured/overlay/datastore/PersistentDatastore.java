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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.overlay.datastore;

import java.io.File;

/**
 * A persistent datastore is a datastore that writes data on a disk.
 * 
 * @author lpellegr
 */
public abstract class PersistentDatastore extends Datastore {

    protected final File path;

    protected PersistentDatastore(File parentPath) {
        super();
        this.path = new File(parentPath, super.id.toString());
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
     * 
     * @param remove
     *            indicates whether the repository associated to the datastore
     *            has to be removed.
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    /**
     * Closes the repository (e.g. releases the resources). If someone attempt
     * to call this method whereas the datastore is not initialized, then the an
     * exception is thrown.
     */
    public void close(boolean remove) {
        if (!super.initialized.compareAndSet(true, false)) {
            throw new IllegalStateException("datastore not initialized");
        } else {
            this.internalClose(remove);
        }
    }

    protected abstract void internalClose(boolean remove);

}
