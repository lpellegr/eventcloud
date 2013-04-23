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
package fr.inria.eventcloud.benchmarks.pubsub.overlay;

import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Custom overlay to maintain some measurements.
 * 
 * @author lpellegr
 */
public class CustomSemanticOverlay extends SemanticCanOverlay {

    public long publicationsStorageEndTime;

    public long subscriptionsStorageEndTime;

    public CustomSemanticOverlay(
            TransactionalTdbDatastore subscriptionsDatastore,
            TransactionalTdbDatastore miscDatastore,
            TransactionalTdbDatastore colanderDatastore) {
        super(subscriptionsDatastore, miscDatastore, colanderDatastore);
    }

}
