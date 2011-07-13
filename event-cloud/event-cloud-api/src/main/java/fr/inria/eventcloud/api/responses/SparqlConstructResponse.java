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
package fr.inria.eventcloud.api.responses;

import fr.inria.eventcloud.api.wrappers.ModelWrapper;

/**
 * The response returned by a call to
 * {@link SemanticPeer#executeSparqlConstruct(String)}.
 * 
 * @author lpellegr
 */
public class SparqlConstructResponse extends SparqlResponse<ModelWrapper> {

    private static final long serialVersionUID = 1L;

    public SparqlConstructResponse(long inboundHopCount, long outboundHopCount,
            long latency, long queryDatastoreTime, ModelWrapper result) {
        super(inboundHopCount, outboundHopCount, latency, queryDatastoreTime,
                result);
    }

}