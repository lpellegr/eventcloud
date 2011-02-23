package fr.inria.eventcloud.api.messages.reply;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;

import fr.inria.eventcloud.messages.reply.can.SparqlConstructReply;

/**
 * This abstract class which contains many common information about
 * SPARQL responses like {@link SparqlAskResponse}, {@link SparqlConstructReply} 
 * and {@link SparqlSelectResponse}.
 * 
 * @author lpellegr
 */
public abstract class Sparqlresponse<T> implements Reply {

    private static final long serialVersionUID = 1L;
    
    protected T result;

    private int outboundHopCount;
    
    private int inboundHopCount;
    
    private int latency;
    
    private int queryDatastoreTime;
    
    private int filterTime;
    
    public Sparqlresponse() {
        
    }
    
    public Sparqlresponse(T result, int outboundHopCount, int inboundHopCount, int latency, int queryDatastoreTime, int filterTime) {
        this.result = result;
        this.outboundHopCount = outboundHopCount;
        this.inboundHopCount = inboundHopCount;
        this.latency = latency;
        this.queryDatastoreTime = queryDatastoreTime;
        this.filterTime = filterTime;
    }

    public int getLatency() {
        return this.latency;
    }

    public int getQueryDatastoreTime() {
		return this.queryDatastoreTime;
	}

	public int getFilterTime() {
		return this.filterTime;
	}

	public int getOutboundHopCount() {
        return this.outboundHopCount;
    }

    public int getInboundHopCount() {
        return this.inboundHopCount;
    }

    public T getResult() {
        return this.result;
    }

}
