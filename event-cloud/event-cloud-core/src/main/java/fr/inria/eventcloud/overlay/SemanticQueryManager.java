package fr.inria.eventcloud.overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;
import org.objectweb.proactive.extensions.p2p.structured.api.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PostProcessException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PreProcessException;
import org.objectweb.proactive.extensions.p2p.structured.messages.PendingReplyEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.AbstractReply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.AbstractRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.RequestReplyManager;
import org.openrdf.query.MalformedQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.api.messages.request.SparqlQuery;
import fr.inria.eventcloud.messages.reply.can.AnycastReply;
import fr.inria.eventcloud.messages.reply.can.SemanticReply;
import fr.inria.eventcloud.reasoner.ParsedSparqlQuery;
import fr.inria.eventcloud.reasoner.SparqlQueryIdentifier;
import fr.inria.eventcloud.reasoner.SparqlQueryReasoner;
import fr.inria.eventcloud.util.SparqlQueryFilter;

/**
 * {@link SemanticQueryManager} is an implementation of {@link RequestReplyManager}. It
 * is used for converting initial query from public API to private API,
 * dispatching query and to create response from public API by using responses
 * retrieved.
 * 
 * @author lpellegr
 */
public class SemanticQueryManager extends RequestReplyManager {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = 
    	LoggerFactory.getLogger(SemanticQueryManager.class);

    private final SparqlQueryReasoner parser = SparqlQueryReasoner.getInstance();
    
    private ConcurrentSkipListSet<UUID> queriesIdentifierMet = new ConcurrentSkipListSet<UUID>();
    
    private transient ExecutorService threadPool;
    
    /**
     * Contains the entries associated to the queries which are being performed
     * in local (i.e. query which are performed on the local datastore).
     */
    private final Map<UUID, FutureTask<Object>> pendingQueries = new ConcurrentHashMap<UUID, FutureTask<Object>>();
    
    public SemanticQueryManager() {

    }

    public Map<UUID, FutureTask<Object>> getPendingQueries() {
        return this.pendingQueries;
    }

    public ConcurrentSkipListSet<UUID> getQueriesIdentifierMet() {
        return this.queriesIdentifierMet;
    }
    
    /**
     * {@inheritDoc}
     */
    public Reply dispatch(Request query) throws DispatchException {
        if (!(query instanceof SparqlQuery)) {
            throw new DispatchException("Only SPARQL queries can be dispatched.");
        }

        try {
            ParsedSparqlQuery parsedQuery = this.parser.decompose((SparqlQuery) query);
        
            if (logger.isDebugEnabled()) {
                StringBuffer buf = new StringBuffer();
                buf.append("Query '");
                buf.append(query.toString());
                
				if (parsedQuery.getSubQueries().size() > 1) {
					buf.append("' is dispatched as:\n");
					for (SparqlQuery subQuery : parsedQuery.getSubQueries()) {
						buf.append("  --> ");
						buf.append(subQuery.toString());
						buf.append("\n");
					}

					logger.debug(buf.toString().substring(0, buf.length() - 1));
				} else {
					buf.append("' is dispatched.");
					logger.debug(buf.toString());
				}
            }

            final List<AbstractReply<?>> subQueryResponses = 
                		Collections.synchronizedList(
                				new ArrayList<AbstractReply<?>>(
                						parsedQuery.getSubQueries().size()));

            final CountDownLatch doneSignal = new CountDownLatch(parsedQuery.getSubQueries().size());
            // executes sub queries in parallel
            for (final SparqlQuery subQuery : parsedQuery.getSubQueries()) {
            	this.getThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						try {
							subQueryResponses.add(process(preProcess(subQuery)));
							doneSignal.countDown();
						} catch (PreProcessException e) {
							e.printStackTrace();
						}
					}
				});
            }
            try {
				doneSignal.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

            SemanticReply<?> semanticQueryResponseMessage = null;
            if (parsedQuery.requireFiltration()) {
            	long start = System.currentTimeMillis();
            	try {
                    semanticQueryResponseMessage = 
                        SparqlQueryFilter.getInstance().filter(
                        	(SparqlQuery) query, subQueryResponses);
                } catch (SemanticSpaceException e) {
                    e.printStackTrace();
                }
                semanticQueryResponseMessage.setFilterTime((int)(System.currentTimeMillis() - start));
            } else {
                semanticQueryResponseMessage = 
                    ((SemanticReply<?>) subQueryResponses.get(0));
            }
            
            return this.postProcess(semanticQueryResponseMessage);
        } catch (MalformedQueryException e) {
            throw new DispatchException("An error occured while parsing query", e);
        }
    }
    
    private synchronized ExecutorService getThreadPool() {
    	if (this.threadPool == null) {
    		// TODO choose the optimal size to use for the thread pool
    		this.threadPool =  Executors.newFixedThreadPool(10);
    	}
    	
    	return this.threadPool;
    }

    /**
     * Merges the specified response received with the response from the
     * {@link PendingResponseEntry} associated to the identifier of the 
     * specified <code>response</code>.
     * 
     * @param response
     *            the response received.
     *            
     * @return the {@link PendingResponseEntry} associated to the response identifier
     * 		   after it has been merged.
     */
    public PendingReplyEntry mergeResponseReceived(AbstractReply<?> response) {
		PendingReplyEntry entry = 
			super.getResponsesReceived().get(response.getId());

		synchronized (entry) {
			if (entry.getResponse() == null) {
				entry.setResponse(response);
			} else {
				entry.getResponse().incrementHopCount(response.getInboundHopCount());
				entry.getResponse().setOutboundHopCount(response.getOutboundHopCount());
				((AnycastReply<?>) entry.getResponse())
						.addAll(((AnycastReply<?>) response)
								.getDataRetrieved());
			}
		}
		return entry;
    }

    /**
     * {@inheritDoc}
     */
    public AbstractRequest<?> preProcess(Request query) throws PreProcessException {
        AbstractRequest<?> msg = null;

        if (query instanceof SparqlQuery) {
            try { 
                msg = SparqlQueryIdentifier.createQueryMessage((SparqlQuery)query);
            } catch (MalformedQueryException e) {
                throw new PreProcessException("Cannot identify query", e);
            }
        } else {
            throw new PreProcessException("Unknown query type");
        }

        return msg;
    }

    /**
     * {@inheritDoc}
     */
    public Reply postProcess(AbstractReply<?> privateResponse) 
    										throws PostProcessException {
        if (logger.isDebugEnabled()) {
            logger.debug(
            		"Before public response is created, outboundHopCount=" 
            		+ privateResponse.getOutboundHopCount() + ", inboundHopCount=" 
            		+ privateResponse.getInboundHopCount() + ", latency=" + privateResponse.getLatency()); 
        }
        
        return privateResponse.createResponse();
    }
}
