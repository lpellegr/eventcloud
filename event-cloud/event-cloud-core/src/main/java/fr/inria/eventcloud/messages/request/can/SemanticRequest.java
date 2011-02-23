package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Element;

import fr.inria.eventcloud.api.messages.request.SparqlQuery;

/**
 * SemanticQueryMessage is a query used in order to retrieve data from peer by
 * Resource Description Framework criteria which is a method for conceptual
 * description or modeling of information. This kind of request will query some
 * {@link Peer} (each maintains an RDF datastore) in order to retrieve results.
 * 
 * This kind of query must be used by a CAN structured peer-to-peer network only.
 * 
 * @author lpellegr
 */
public abstract class SemanticRequest extends AnycastRequest {

    private static final long serialVersionUID = 1L;

    private SparqlQuery sparqlQuery;

    /**
     * Constructs a new SemanticQueryMessage with the specified
     * <code>coordinatesToFind</code>.
     * 
     * @param coordinateToFind
     *            the coordinates to reach.
     */
    public SemanticRequest(Coordinate coordinateToFind) {
        super(coordinateToFind);
    }

    /**
     * Constructs a new SemanticQueryMessage with the specified
     * <code>query</code> and <code>coordinatesToFind</code>.
     * 
     * @param query
     *            the query to execute when coordinates are reached.
     * 
     * @param coordinateToFind
     *            the coordinates to reach.
     */
    public SemanticRequest(SparqlQuery query, Coordinate coordinateToFind) {
        super(coordinateToFind);
        this.sparqlQuery = query;
    }

    /**
     * Returns the {@link SparqlQuery} which is being handled.
     * 
     * @return the {@link SparqlQuery} which is being handled.
     */
    public SparqlQuery getSparqlQuery() {
        return this.sparqlQuery;
    }

    /**
     * Indicates if the key to reach has all its coordinate elements 
     * fixed with a not <code>null</code> value or not.
     * 
     * @return <code>true</code> if the key to reach has all its coordinate
     * 		   elements fixed with a not <code>null</code> value, 
     *         <code>false</code> otherwise.
     */
    public boolean keyToReachNotNullElements() {
        for (Element elt : super.getKeyToReach()) {
            if (elt == null) {
                return false;
            }
        }
        return true;
    }

}
