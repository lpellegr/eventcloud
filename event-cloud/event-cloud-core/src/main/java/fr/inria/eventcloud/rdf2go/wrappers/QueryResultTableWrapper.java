package fr.inria.eventcloud.rdf2go.wrappers;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;

import fr.inria.eventcloud.util.SemanticHelper;

/**
 * The <code>QueryResultTableWrapper</code> wraps a value of the 
 * RDF2Go type {@link QueryResultTable}.
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

        return SemanticHelper.generateQueryResultTable(this.variables, queryRows);
    }

}
