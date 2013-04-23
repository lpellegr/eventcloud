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
package fr.inria.eventcloud.api.wrappers;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;

/**
 * ResultSetMemWrapper is used to make a Jena {@link ResultSet} serializable. It
 * is important to notice this wrapper will store in memory all the solutions.
 * This is done when the object is instanciated.
 * 
 * @author lpellegr
 */
public class ResultSetMemWrapper extends ResultSetWrapper {

    private static final long serialVersionUID = 150L;

    /**
     * Constructs a wrapper for the specified {@code resultSet}.
     * 
     * @param resultSet
     *            the result set to wrap.
     */
    public ResultSetMemWrapper(ResultSet resultSet) {
        super(new ResultSetMem(resultSet));
    }

    /**
     * Constructs a wrapper for the specified {@code resultSetMem}.
     * 
     * @param resultSetMem
     *            the result set to wrap.
     */
    public ResultSetMemWrapper(ResultSetMem resultSetMem) {
        super(resultSetMem);
    }

}
