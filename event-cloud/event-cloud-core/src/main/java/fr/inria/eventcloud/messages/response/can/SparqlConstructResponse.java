package fr.inria.eventcloud.messages.response.can;

import java.util.HashSet;
import java.util.Set;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Statement;

import fr.inria.eventcloud.messages.request.can.SparqlConstructRequest;
import fr.inria.eventcloud.messages.request.can.SparqlRequest;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Response associated to {@link SparqlConstructRequest}.
 * 
 * @author lpellegr
 */
public class SparqlConstructResponse extends SparqlResponse {

    private static final long serialVersionUID = 1L;

    public SparqlConstructResponse(SparqlRequest request) {
        super(request);
    }

    public ClosableIterable<Statement> getResults() {
    	Set<Statement> stmts = new HashSet<Statement>();
    	
    	for (ClosableIterableWrapper ciw : super.getDeserializedResults()) {
    		ClosableIterator<Statement> it = ciw.toRDF2Go().iterator();
    		while (it.hasNext()) {
    			stmts.add(it.next());
    		}
    	}
    	
    	return SemanticHelper.generateClosableIterable(stmts);
    }

}
