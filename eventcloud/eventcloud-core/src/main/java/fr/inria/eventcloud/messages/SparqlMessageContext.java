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
package fr.inria.eventcloud.messages;

import java.io.Serializable;

/**
 * 
 * 
 * @author lpellegr
 */
public class SparqlMessageContext implements Serializable {

    private static final long serialVersionUID = 151L;

    private final String query;

    private final SparqlQueryType queryType;

    public SparqlMessageContext(String query, SparqlQueryType queryType) {
        this.query = query;
        this.queryType = queryType;
    }

    public String getQuery() {
        return this.query;
    }

    public SparqlQueryType getQueryType() {
        return this.queryType;
    }

}
