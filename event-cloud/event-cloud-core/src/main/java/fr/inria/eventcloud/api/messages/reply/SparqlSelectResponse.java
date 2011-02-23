package fr.inria.eventcloud.api.messages.reply;

import java.util.ArrayList;
import java.util.HashSet;

import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;

import fr.inria.eventcloud.api.messages.request.SparqlSelectQuery;
import fr.inria.eventcloud.messages.reply.can.SparqlSelectReply;
import fr.inria.eventcloud.rdf2go.wrappers.QueryResultTableWrapper;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Associated response for {@link SparqlSelectQuery}.
 * 
 * @author lpellegr
 */
public class SparqlSelectResponse extends
        Sparqlresponse<QueryResultTable> {

    private static final long serialVersionUID = 1L;

    public SparqlSelectResponse(SparqlSelectReply response) {
        super(SemanticHelper.generateQueryResultTable(new ArrayList<String>(), new HashSet<QueryRow>()),
              response.getOutboundHopCount(),
              response.getInboundHopCount(),
              response.getLatency(),
              response.getQueryDatastoreTime(),
              response.getFilterTime());
        
        QueryResultTableWrapper dataRetrieved = response.mergeAndGetDataRetrieved();
        if (dataRetrieved != null) {
            this.result = dataRetrieved.toRDF2Go();
        }
    }
    
}
