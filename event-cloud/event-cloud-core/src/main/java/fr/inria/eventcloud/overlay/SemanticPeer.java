package fr.inria.eventcloud.overlay;

import static com.google.common.base.Preconditions.checkNotNull;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerImpl;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.api.SemanticFactory;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.datastore.OwlimDatastore;
import fr.inria.eventcloud.messages.request.can.AddStatementRequest;
import fr.inria.eventcloud.messages.request.can.RemoveStatementRequest;
import fr.inria.eventcloud.messages.request.can.RemoveStatementsRequest;

/**
 * A SemanticPeer is a peer constructed by using a {@link CanOverlay}. It
 * provides semantic operations to insert, to remove and to retrieve semantic
 * data by executing a SPARQL query.
 * <p>
 * Warning, it is strongly recommended to use {@link SemanticFactory} in order
 * to create a new active object of type SemanticPeer.
 * 
 * @author lpellegr
 */
public class SemanticPeer extends PeerImpl {

    private static final long serialVersionUID = 1L;

    public SemanticPeer() {
        super(new CanOverlay(
                new SparqlRequestResponseManager(), new OwlimDatastore()));
    }

    /*
     * Operations specific to semantic peer
     */

    public BooleanWrapper addStatement(URI context, Statement stmt) {
        try {
            PAFuture.waitFor(super.send(new AddStatementRequest(context, stmt)));
        } catch (DispatchException e) {
            e.printStackTrace();
        }
        return new BooleanWrapper(true);
    }

    public BooleanWrapper removeStatement(URI context, Statement stmt) {
        try {
            PAFuture.waitFor(super.send(new RemoveStatementRequest(
                    context, stmt)));
        } catch (DispatchException e) {
            e.printStackTrace();
        }
        return new BooleanWrapper(true);
    }

    public BooleanWrapper removeStatements(URI context, Statement stmt) {
        try {
            PAFuture.waitFor(super.send(new RemoveStatementsRequest(
                    context, stmt)));
        } catch (DispatchException e) {
            e.printStackTrace();
        }
        return new BooleanWrapper(true);
    }

    public SparqlAskResponse executeSparqlAsk(String sparqlAsk) {
        return ((SparqlRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlAsk(checkNotNull(sparqlAsk));
    }

    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstruct) {
        return ((SparqlRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlConstruct(checkNotNull(sparqlConstruct));
    }

    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribe) {
        return ((SparqlRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlDescribe(checkNotNull(sparqlDescribe));
    }

    public SparqlSelectResponse executeSparqlSelect(String sparqlSelect) {
        return ((SparqlRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlSelect(checkNotNull(sparqlSelect));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initActivity(Body body) {
        body.setImmediateService("addStatement", false);
        body.setImmediateService("removeStatement", false);
        body.setImmediateService("removeStatements", false);
        body.setImmediateService("executeSparqlAsk", false);
        body.setImmediateService("executeSparqlConstruct", false);
        body.setImmediateService("executeSparqlDescribe", false);
        body.setImmediateService("executeSparqlSelect", false);

        super.initActivity(body);
    }

}
