package fr.inria.eventcloud.overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanRequestResponseManager;
import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;

import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.messages.request.can.SparqlRequest;
import fr.inria.eventcloud.messages.response.can.SparqlResponse;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.rdf2go.wrappers.QueryResultTableWrapper;
import fr.inria.eventcloud.reasoner.SparqlColander;
import fr.inria.eventcloud.reasoner.SparqlReasoner;

/**
 * {@link SparqlRequestResponseManager} is an implementation of
 * {@link CanRequestResponseManager} for managing SPARQL queries over a CAN
 * network.
 * 
 * @author lpellegr
 */
public class SparqlRequestResponseManager extends CanRequestResponseManager {

	private static final long serialVersionUID = 1L;

	private transient SparqlReasoner reasoner;
	
	private transient SparqlColander colander;
	
	private final ConcurrentHashMap<UUID, Future<ClosableIterableWrapper>> 
								pendingRequestsResult
									= new ConcurrentHashMap<UUID, Future<ClosableIterableWrapper>>();
	
    private transient ExecutorService threadPool;
    
    public SparqlRequestResponseManager() {
    	super();
    }

	/**
	 * Dispatches a SPARQL Ask query over the overlay network.
	 * 
	 * @param sparqlAskQuery
	 *            the SPARQL ASK query to execute.
	 *            
	 * @return a response corresponding to the type of the query dispatched.
	 */
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery) {
    	List<SparqlResponse> responses = 
    		this.dispatch(this.getReasoner().parseSparql(sparqlAskQuery));

    	boolean result = 
    		this.getColander().filterSparqlAsk(sparqlAskQuery, responses);
    	
    	long[] measurements = this.aggregateMeasurements(responses);

    	return new SparqlAskResponse(
    					measurements[0], measurements[1], 
    					measurements[2], result);
    }
    
    /**
	 * Dispatches a SPARQL Construct query over the overlay network.
	 * 
	 * @param sparqlConstructQuery
	 *            the SPARQL CONSTRUCT query to execute.
	 *            
	 * @return a response corresponding to the type of the query dispatched.
	 */
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery) {
    	List<SparqlResponse> responses = 
    		this.dispatch(this.getReasoner().parseSparql(sparqlConstructQuery));
    	
    	ClosableIterable<Statement> result = 
    		this.getColander().filterSparqlConstruct(sparqlConstructQuery, responses);
    	
    	long[] measurements = this.aggregateMeasurements(responses);

    	return new SparqlConstructResponse(
    					measurements[0], measurements[1], 
    					measurements[2], new ClosableIterableWrapper(result));
    }
    
    /**
	 * Dispatches a SPARQL DESCRIBE query over the overlay network.
	 * 
	 * @param sparqlDescribeQuery
	 *            the SPARQL DESCRIBE query to execute.
	 *            
	 * @return a response corresponding to the type of the query dispatched.
	 */
    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery) {
    	List<SparqlResponse> responses = 
    		this.dispatch(this.getReasoner().parseSparql(sparqlDescribeQuery));
    	
    	ClosableIterable<Statement> result = 
    		this.getColander().filterSparqlDescribe(sparqlDescribeQuery, responses);
    	
    	long[] measurements = this.aggregateMeasurements(responses);

    	return new SparqlDescribeResponse(
    					measurements[0], measurements[1], 
    					measurements[2], new ClosableIterableWrapper(result));
    }
    
    /**
	 * Dispatches a SPARQL SELECT query over the overlay network.
	 * 
	 * @param sparqlSelectQuery
	 *            the SPARQL SELECT query to execute.
	 * @return a response corresponding to the type of the query dispatched.
	 */
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery) {
    	List<SparqlResponse> responses = 
    		this.dispatch(this.getReasoner().parseSparql(sparqlSelectQuery));
    	
    	QueryResultTable result = 
    		this.getColander().filterSparqlSelect(sparqlSelectQuery, responses);
    	
    	long[] measurements = this.aggregateMeasurements(responses);

    	return new SparqlSelectResponse(
    					measurements[0], measurements[1], 
    					measurements[2], new QueryResultTableWrapper(result));
    }

	/**
	 * Returns the measurements after having merged the intermediate results.
	 * 
	 * @param responses
	 *            the list of responses containing the measurements.
	 * 
	 * @return the measurements after having merged the intermediate results.
	 *         The measurements which are returned are respectively the
	 *         {@code inboundHopCount}, the {@code outboundHopCount} and the
	 *         {@code latency}.
	 */
    private long[] aggregateMeasurements(List<SparqlResponse> responses) {
    	long outboundHopCount = 0;
    	long latency = 0;
    	
    	for (SparqlResponse response : responses) {
    		if (response.getLatency() > latency) {
    			latency = response.getLatency();
    		}
    		
    		outboundHopCount += response.getOutboundHopCount();
    	}
    	
    	return new long[] {
    				outboundHopCount, // inboundHopCount = outboundHopCount
    				outboundHopCount,
    				latency
    			};
    }
    
    public List<SparqlResponse> dispatch(List<? extends SparqlRequest> requests) {
    	final List<SparqlResponse> replies = Collections.synchronizedList(new ArrayList<SparqlResponse>(requests.size()));
    	final CountDownLatch doneSignal = new CountDownLatch(requests.size());
         
        for (final SparqlRequest request : requests) {
        	this.getThreadPool().execute(new Runnable() {
        		@Override
				public void run() {
					try {
						replies.add((SparqlResponse) SparqlRequestResponseManager.super.dispatch(request));
					} catch (DispatchException e) {
						e.printStackTrace();
					} finally {
						doneSignal.countDown();
					}
				}
        	});
        }
         
        try {
        	doneSignal.await();
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
		}

        return replies;
    }
    
    public ConcurrentHashMap<UUID, Future<ClosableIterableWrapper>> getPendingRequestsResult() {
		return pendingRequestsResult;
	}

	public synchronized ExecutorService getThreadPool() {
    	if (this.threadPool == null) {
    		// TODO choose the optimal size to use for the thread pool
    		this.threadPool =  Executors.newFixedThreadPool(20);
    	}
    	
    	return this.threadPool;
    }
	
	private synchronized SparqlReasoner getReasoner() {
		if (this.reasoner == null) {
			this.reasoner = new SparqlReasoner();
		}
		return this.reasoner;
	}
    
	private synchronized SparqlColander getColander() {
		if (this.colander == null) {
			this.colander = new SparqlColander();
		}
		return this.colander;
	}
	
}
