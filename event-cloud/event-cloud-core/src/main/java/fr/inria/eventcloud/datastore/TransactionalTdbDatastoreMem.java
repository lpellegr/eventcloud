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

import com.hp.hpl.jena.tdb.StoreConnection;

/**
 * A {@link TransactionalTdbDatastore} that is allocated and works in memory.
 * This is useful for testing purposes.
 * 
 * @author lpellegr
 */
public final class TransactionalTdbDatastoreMem extends
        TransactionalTdbDatastore {

    public TransactionalTdbDatastoreMem() {
        super(StoreConnection.createMemUncached(), false);
    }

}
