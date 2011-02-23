package fr.inria.eventcloud.api.messages.reply;

import java.util.HashSet;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.rdf2go.model.Statement;

import fr.inria.eventcloud.api.messages.request.SparqlConstructQuery;
import fr.inria.eventcloud.messages.reply.can.SparqlConstructReply;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.util.SemanticHelper;


/**
 * Associated response for {@link SparqlConstructQuery}.
 * 
 * @author lpellegr
 */
public class SparqlConstructResponse extends
        Sparqlresponse<ClosableIterable<Statement>> {

    private static final long serialVersionUID = 1L;
    
    public SparqlConstructResponse(SparqlConstructReply response) {
        super(SemanticHelper.generateClosableIterable(new HashSet<Statement>()),
              response.getOutboundHopCount(),
              response.getInboundHopCount(),
              response.getLatency(),
              response.getQueryDatastoreTime(),
              response.getFilterTime());

        ClosableIterableWrapper dataRetrieved = response.mergeAndGetDataRetrieved();
        if (dataRetrieved != null) {
            this.result = dataRetrieved.toRDF2Go();
        }
    }

}
