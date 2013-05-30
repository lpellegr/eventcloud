/**
 * Copyright (c) 2011-2013 INRIA.
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

import com.hp.hpl.jena.tdb.base.file.Location;

import fr.inria.eventcloud.datastore.stats.NullStatsRecorder;
import fr.inria.eventcloud.datastore.stats.StatsRecorder;

/**
 * Builder associated to {@link TransactionalTdbDatastore}.
 * 
 * @author lpellegr
 */
public class TransactionalTdbDatastoreBuilder {

    private final Location location;

    private boolean deleteFilesAfterClose = false;

    private StatsRecorder statsRecorder;

    /**
     * Creates a new builder whose the {@link #build()} method will create an
     * in-memory {@link TransactionalTdbDatastore}. This is useful for testing
     * purposes.
     */
    public TransactionalTdbDatastoreBuilder() {
        this(null, NullStatsRecorder.getInstance());
    }

    public TransactionalTdbDatastoreBuilder(String location) {
        this(new Location(location));
    }

    public TransactionalTdbDatastoreBuilder(File location) {
        this(location.getAbsolutePath());
    }

    public TransactionalTdbDatastoreBuilder(Location location) {
        this(location, NullStatsRecorder.getInstance());
    }

    public TransactionalTdbDatastoreBuilder(Location location,
            StatsRecorder statsRecorder) {
        this.location = location;
        this.statsRecorder = statsRecorder;
    }

    public TransactionalTdbDatastoreBuilder deleteFilesAfterClose() {
        return this.deleteFilesAfterClose(true);
    }

    public TransactionalTdbDatastoreBuilder deleteFilesAfterClose(boolean value) {
        this.deleteFilesAfterClose = value;
        return this;
    }

    public TransactionalTdbDatastoreBuilder recordStats(StatsRecorder statsRecorder) {
        this.statsRecorder = statsRecorder;
        return this;
    }

    public TransactionalTdbDatastore build() {
        if (this.location == null) {
            return new TransactionalTdbDatastore(this.statsRecorder);
        }

        return new TransactionalTdbDatastore(
                this.location, this.statsRecorder, this.deleteFilesAfterClose);
    }

}
