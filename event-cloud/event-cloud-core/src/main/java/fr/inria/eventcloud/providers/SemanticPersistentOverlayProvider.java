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
package fr.inria.eventcloud.providers;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * This class is used to build a {@link SemanticCanOverlay} with a
 * {@link TransactionalTdbDatastore} .
 * 
 * @author lpellegr
 */
public final class SemanticPersistentOverlayProvider extends
        SerializableProvider<SemanticCanOverlay> {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public SemanticCanOverlay get() {
        return new SemanticCanOverlay(
                new TransactionalTdbDatastore(
                        EventCloudProperties.getRepositoryPath()
                                .getAbsolutePath(),
                        EventCloudProperties.REPOSITORIES_AUTO_REMOVE.getValue()),
                new TransactionalTdbDatastore(
                        System.getProperty("java.io.tmpdir") + "/eventcloud/"
                                + UUID.randomUUID().toString(), true));
    }

}
