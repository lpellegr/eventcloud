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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.rdf2go.wrappers;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;

import fr.inria.eventcloud.util.SemanticHelper;

/**
 * The <code>QueryResultTableWrapper</code> wraps a value of the RDF2Go type
 * {@link QueryResultTable}.
 * 
 * @author lpellegr
 */
public class QueryResultTableWrapper implements RDF2GoWrapper<QueryResultTable> {

    private static final long serialVersionUID = 1L;

    private final List<String> variables;

    private final List<QueryRowWrapper> rows = new ArrayList<QueryRowWrapper>();

    public QueryResultTableWrapper(QueryResultTable qrt) {
        this.variables = qrt.getVariables();
        ClosableIterator<QueryRow> it = qrt.iterator();
        while (it.hasNext()) {
            this.rows.add(new QueryRowWrapper(it.next(), this.variables));
        }
    }

    public List<QueryRowWrapper> getRows() {
        return this.rows;
    }

    public QueryResultTable toRDF2Go() {
        List<QueryRow> queryRows = new ArrayList<QueryRow>(this.rows.size());
        for (QueryRowWrapper qrw : this.rows) {
            queryRows.add(qrw.toRDF2Go());
        }

        return SemanticHelper.generateQueryResultTable(
                this.variables, queryRows);
    }

}
