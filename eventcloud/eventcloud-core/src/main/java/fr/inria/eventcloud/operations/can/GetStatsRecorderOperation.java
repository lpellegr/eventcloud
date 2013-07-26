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
package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

import fr.inria.eventcloud.datastore.stats.StatsRecorder;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Operation used to retrieve the current {@link StatsRecorder} instance for the
 * misc datastore.
 * 
 * @author lpellegr
 */
public class GetStatsRecorderOperation extends CallableOperation {

    private static final long serialVersionUID = 160L;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperation handle(StructuredOverlay overlay) {
        return new GetStatsRecordeResponseOperation(
                ((SemanticCanOverlay) overlay).getMiscDatastore()
                        .getStatsRecorder());
    }

}
