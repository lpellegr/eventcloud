package fr.inria.eventcloud.util;

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.messages.reply.AbstractReply;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.Statement;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.api.messages.request.SparqlAskQuery;
import fr.inria.eventcloud.api.messages.request.SparqlConstructQuery;
import fr.inria.eventcloud.api.messages.request.SparqlDescribeQuery;
import fr.inria.eventcloud.api.messages.request.SparqlQuery;
import fr.inria.eventcloud.datastore.SesameModelFactory;
import fr.inria.eventcloud.messages.reply.can.SparqlAskReply;
import fr.inria.eventcloud.messages.reply.can.SparqlConstructReply;
import fr.inria.eventcloud.messages.reply.can.SparqlDescribeReply;
import fr.inria.eventcloud.messages.reply.can.SparqlSelectReply;
import fr.inria.eventcloud.messages.reply.can.SemanticReply;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.rdf2go.wrappers.QueryResultTableWrapper;

/**
 * 
 * @author lpellegr
 */
public class SparqlQueryFilter {

    private static class LazyInitializer {
    	public static SparqlQueryFilter instance = new SparqlQueryFilter();
    }
    
    private transient ModelSet inMemoryDataStore;
    
    public SparqlQueryFilter() {
		this.inMemoryDataStore = SesameModelFactory.createModelSet();
		this.inMemoryDataStore.open();
    }
    
    /**
     * 
     * 
     * @param query
     *            the initial {@link SparqlQuery}.
     * @param subQueryResponses
     *            a list containing the responses associated to the initial
     *            SPARQL query which has been decomposed into sub-queries.
     * @return an unique response after having filtered sub responses.
     * @throws SemanticSpaceException
     *             if a problem occurs when data are filtered.
     */
    @SuppressWarnings("unchecked")
    public synchronized SemanticReply<?> filter(SparqlQuery query,
            	List<AbstractReply<?>> subQueryResponses) throws SemanticSpaceException {
        Model model = this.inMemoryDataStore.getModel(query.getSpaceURI());
        model.setAutocommit(false);
        ClosableIterator<Statement> it;
        ClosableIterableWrapper data;
        int inboundHopCount = 0;
        int outboundHopCount = 0;
        int latency = 0;
        int queryDatastoreTime = 0;
        
        for (AbstractReply<?> semanticQueryResponseMessage : subQueryResponses) {
        	inboundHopCount += semanticQueryResponseMessage.getInboundHopCount();
        	outboundHopCount += semanticQueryResponseMessage.getOutboundHopCount();
        	latency += semanticQueryResponseMessage.getLatency();
        	queryDatastoreTime += 
        		((SemanticReply<ClosableIterableWrapper>) semanticQueryResponseMessage)
        			.getQueryDatastoreTime();
        	
            data = ((SemanticReply<ClosableIterableWrapper>) 
                        semanticQueryResponseMessage).mergeAndGetDataRetrieved();
            if (data == null) {
                continue;
            }
            it = data.toRDF2Go().iterator();

            while (it.hasNext()) {
                model.addStatement(it.next());
            }
        }
        model.commit();

        SparqlConstructReply subQuery =
            ((SparqlConstructReply) subQueryResponses.get(0));
        
        SemanticReply<?> response;
        
        if (query instanceof SparqlAskQuery) {
            response = (SemanticReply<?>) new SparqlAskReply(
        	    	subQuery, subQuery.getKeyToReach(), 
        	    	this.inMemoryDataStore.sparqlAsk(query.toString()));
        } else if (query instanceof SparqlConstructQuery) {
            response = (SemanticReply<?>) new SparqlConstructReply(
        	    	subQuery, 
        	    	subQuery.getKeyToReach(), 
        	    	new ClosableIterableWrapper(
        	    		this.inMemoryDataStore.sparqlConstruct(
        	    			query.toString())));
        } else if (query instanceof SparqlDescribeQuery) {
            response = (SemanticReply<?>) new SparqlDescribeReply(
    	    	subQuery, 
    	    	subQuery.getKeyToReach(), 
    	    	new ClosableIterableWrapper(
    	    		this.inMemoryDataStore.sparqlDescribe(
    	    			query.toString())));
        } else {
            response = (SemanticReply<?>) new SparqlSelectReply(
    	    	subQuery, 
    	    	subQuery.getKeyToReach(), 
    	    	new QueryResultTableWrapper(this.inMemoryDataStore.sparqlSelect(query.toString())));
        }

        model.removeAll();
        model.commit();
        
        response.setHopCount(inboundHopCount);
        response.setOutboundHopCount(outboundHopCount);
        response.setLatency(latency);
        response.setQueryDatastoreTime(queryDatastoreTime);
        return response;
    }
    
	public static SparqlQueryFilter getInstance() {
		return LazyInitializer.instance;
    }
    
}
