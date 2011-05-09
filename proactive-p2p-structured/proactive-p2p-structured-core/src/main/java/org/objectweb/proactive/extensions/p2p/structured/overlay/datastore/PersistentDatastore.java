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
