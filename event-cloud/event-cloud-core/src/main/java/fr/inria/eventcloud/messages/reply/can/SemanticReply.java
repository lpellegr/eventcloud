package fr.inria.eventcloud.messages.reply.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;

import fr.inria.eventcloud.api.messages.request.SparqlQuery;
import fr.inria.eventcloud.messages.request.can.SemanticRequest;

/**
 * @author lpellegr
 */
public abstract class SemanticReply<T> extends AnycastReply<T> {

    private static final long serialVersionUID = 1L;

    private SparqlQuery sparqlQuery;

    private int filterTime = 0;
    
    private int queryDatastoreTime = 0;
    
    public SemanticReply(
            SemanticReply<?> response,
            Coordinate coordinateToReach) {
        super(response, coordinateToReach);
        this.sparqlQuery = response.getSparqlQuery();
    }

    public SemanticReply(
            SemanticRequest query,
            Coordinate coordinateToReach) {
        super(query, coordinateToReach);
        this.sparqlQuery = query.getSparqlQuery();
    }

    public T queryDataStore(StructuredOverlay overlay) {
    	long start = System.currentTimeMillis();
    	T result = this.performQueryDataStore(overlay);
    	this.queryDatastoreTime += 
    		(int) (System.currentTimeMillis() - start);
    	
    	return result;
    }
    
    public abstract T performQueryDataStore(StructuredOverlay overlay);
    
    /**
     * Returns the {@link SparqlQuery} which is being handled.
     * 
     * @return the {@link SparqlQuery} which is being handled.
     */
    public SparqlQuery getSparqlQuery() {
        return this.sparqlQuery;
    }

	/**
	 * Returns the filter time in ms (i.e. the time wasted to filter the results
	 * returned by the sub-queries). A value equals to 0 means the initial query
	 * has not been decomposed.
	 * 
	 * @return the filter time in ms (i.e. the time wasted to filter the results
	 *         returned by the sub-queries). A value equals to 0 means the
	 *         initial query has not been decomposed.
	 */
	public int getFilterTime() {
		return this.filterTime;
	}

	/**
	 * Returns a value representing the time spent in querying the datastores.
	 * 
	 * @return a value representing the time spent in querying the datastores.
	 */
	public int getQueryDatastoreTime() {
		return this.queryDatastoreTime;
	}

	public void setFilterTime(int filterTime) {
		this.filterTime = filterTime;
	}

	public void setQueryDatastoreTime(int queryDatastoreTime) {
		this.queryDatastoreTime = queryDatastoreTime;
	}

}
