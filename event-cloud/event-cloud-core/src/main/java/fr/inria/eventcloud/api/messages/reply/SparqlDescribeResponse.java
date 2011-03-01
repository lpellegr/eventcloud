package fr.inria.eventcloud.api.messages.reply;

import java.util.HashSet;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.rdf2go.model.Statement;

import fr.inria.eventcloud.api.messages.request.SparqlConstructQuery;
import fr.inria.eventcloud.messages.reply.can.SparqlDescribeReply;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Associated response for {@link SparqlConstructQuery}.
 * 
 * @author lpellegr
 */
public class SparqlDescribeResponse extends
        Sparqlresponse<ClosableIterable<Statement>> {

    private static final long serialVersionUID = 1L;
    
    public SparqlDescribeResponse(SparqlDescribeReply describeQueryResponseMessage) {
        super(SemanticHelper.generateClosableIterable(new HashSet<Statement>()),
              describeQueryResponseMessage.getOutboundHopCount(),
              describeQueryResponseMessage.getInboundHopCount(),
              describeQueryResponseMessage.getLatency(),
              describeQueryResponseMessage.getQueryDatastoreTime(),
              describeQueryResponseMessage.getFilterTime());

        ClosableIterableWrapper dataRetrieved = describeQueryResponseMessage.mergeAndGetDataRetrieved();
        if (dataRetrieved != null) {
            this.result = dataRetrieved.toRDF2Go();
        }
    }

}
