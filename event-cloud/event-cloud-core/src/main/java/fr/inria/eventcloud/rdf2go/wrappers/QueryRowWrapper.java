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
