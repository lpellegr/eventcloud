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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.impl.QueryRowImpl;
import org.ontoware.rdf2go.model.node.Node;

/**
 * The <code>QueryRowWrapper</code> wraps a value of the RDF2Go type
 * {@link QueryRow}.
 * 
 * @author lpellegr
 */
public class QueryRowWrapper implements RDF2GoWrapper<QueryRow> {

    private static final long serialVersionUID = 1L;

    private final Map<String, Node> values = new HashMap<String, Node>();

    public QueryRowWrapper(QueryRow qr, List<String> variables) {
        for (String var : variables) {
            this.values.put(var, qr.getValue(var));
        }
    }

    public Map<String, Node> getValues() {
        return this.values;
    }

    public int hashCode() {
        return this.values.hashCode();
    }

    public boolean equals(Object obj) {
        return (obj instanceof QueryRowWrapper)
                && (this.values.equals(((QueryRowWrapper) obj).getValues()));
    }

    public QueryRow toRDF2Go() {
        return new QueryRowImpl() {
            private static final long serialVersionUID = 1L;

            private final Map<String, Node> values =
                    QueryRowWrapper.this.values;

            public String getLiteralValue(String varname)
                    throws ModelRuntimeException {
                return this.values.get(varname).toString();
            }

            public Node getValue(String varname) {
                return this.values.get(varname);
            }
        };
    }

}
