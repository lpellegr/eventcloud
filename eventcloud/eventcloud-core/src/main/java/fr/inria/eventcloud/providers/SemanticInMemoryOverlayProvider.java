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
package fr.inria.eventcloud.providers;

import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastoreBuilder;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * This class is used to build a {@link SemanticCanOverlay} with in-memory
 * datastores.
 * 
 * @author lpellegr
 */
public final class SemanticInMemoryOverlayProvider extends
        SerializableProvider<SemanticCanOverlay> {

    private static final long serialVersionUID = 130L;

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticCanOverlay get() {
        TransactionalTdbDatastoreBuilder miscDatastoreBuilder =
                new TransactionalTdbDatastoreBuilder();

        if (EventCloudProperties.RECORD_STATS_MISC_DATASTORE.getValue()) {
            miscDatastoreBuilder.recordStats();
        }

        return new SemanticCanOverlay(
                new TransactionalTdbDatastoreBuilder().build(),
                miscDatastoreBuilder.build(),
                new TransactionalTdbDatastoreBuilder().build());
    }

}
