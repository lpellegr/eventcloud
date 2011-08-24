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
package fr.inria.eventcloud.builders;

import java.io.File;

import org.objectweb.proactive.extensions.p2p.structured.builders.StructuredOverlayBuilder;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.PersistentJenaTdbDatastore;
import fr.inria.eventcloud.overlay.SparqlRequestResponseManager;

/**
 * This class is used to build a {@link CanOverlay} with a
 * {@link SparqlRequestResponseManager} and a {@link PersistentJenaTdbDatastore}
 * .
 * 
 * @author lpellegr
 */
public class SemanticPersistentOverlayBuilder extends StructuredOverlayBuilder {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public StructuredOverlay build() {
        return new CanOverlay(
                new SparqlRequestResponseManager(),
                new PersistentJenaTdbDatastore(
                        new File(
                                EventCloudProperties.REPOSITORIES_PATH.getValue()),
                        EventCloudProperties.REPOSITORIES_AUTO_REMOVE.getValue()));
    }

}
