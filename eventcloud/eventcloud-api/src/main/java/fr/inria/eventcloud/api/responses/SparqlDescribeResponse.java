/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.api.responses;

import fr.inria.eventcloud.api.wrappers.ModelWrapper;

/**
 * The response returned for a Describe SPARQL query form that has been
 * executed.
 * 
 * @author lpellegr
 */
public class SparqlDescribeResponse extends SparqlResponse<ModelWrapper> {

    private static final long serialVersionUID = 160L;

    public SparqlDescribeResponse(SparqlQueryStatistics stats,
            ModelWrapper result) {
        super(stats, result);
    }

}
