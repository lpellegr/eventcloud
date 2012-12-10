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

import java.io.File;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

import com.google.common.base.Preconditions;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastoreBuilder;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * This class is used to build a {@link SemanticCanOverlay} with a
 * {@link TransactionalTdbDatastore} .
 * 
 * @author lpellegr
 */
public final class SemanticPersistentOverlayProvider extends
        SerializableProvider<SemanticCanOverlay> {

    private static final long serialVersionUID = 140L;

    private String streamUrl;

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticCanOverlay get() {
        Preconditions.checkNotNull(this.streamUrl);

        File repositoryPath =
                EventCloudProperties.getRepositoryPath(this.streamUrl);

        TransactionalTdbDatastoreBuilder miscDatastoreBuilder =
                new TransactionalTdbDatastoreBuilder(new File(
                        repositoryPath, "misc")).deleteFilesAfterClose(EventCloudProperties.REPOSITORIES_AUTO_REMOVE.getValue());

        if (EventCloudProperties.RECORD_STATS_MISC_DATASTORE.getValue()) {
            miscDatastoreBuilder.recordStats();
        }

        // datastore used to store publications, historical data, etc.
        TransactionalTdbDatastore miscDatastore = miscDatastoreBuilder.build();

        // datastore used to store subscriptions
        TransactionalTdbDatastore subscriptionsDatastore =
                new TransactionalTdbDatastoreBuilder(new File(
                        repositoryPath, "subscriptions")).deleteFilesAfterClose(
                        EventCloudProperties.REPOSITORIES_AUTO_REMOVE.getValue())
                        .build();

        // datastore used to filter intermediate results for SPARQL requests
        // from a SparqlColander
        TransactionalTdbDatastore colanderDatastore;
        if (EventCloudProperties.COLANDER_IN_MEMORY.getValue()) {
            colanderDatastore = new TransactionalTdbDatastoreBuilder().build();
        } else {
            colanderDatastore =
                    new TransactionalTdbDatastoreBuilder(
                            new File(
                                    EventCloudProperties.COLANDER_REPOSITORIES_PATH.getValue(),
                                    UUID.randomUUID().toString())).deleteFilesAfterClose()
                            .build();
        }

        return new SemanticCanOverlay(
                subscriptionsDatastore, miscDatastore, colanderDatastore);
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

}
