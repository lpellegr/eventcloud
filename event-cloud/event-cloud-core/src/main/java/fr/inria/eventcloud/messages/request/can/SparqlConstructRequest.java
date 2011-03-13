package fr.inria.eventcloud.messages.request.can;


import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.messages.response.can.SparqlConstructResponse;
import fr.inria.eventcloud.overlay.can.SemanticCanOverlay;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.reasoner.AtomicSparqlQuery;
import fr.inria.eventcloud.util.DSpaceProperties;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * 
 * @author lpellegr
 */
public class SparqlConstructRequest extends SparqlRequest {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = 
            LoggerFactory.getLogger(SparqlConstructRequest.class);

    public SparqlConstructRequest(AtomicSparqlQuery query) {
        super(new DefaultAnycastConstraintsValidator(
        		SemanticHelper.createCoordinateWithNullValues(
        				query.getSubjectWithNullVariable(), 
        				query.getPredicateWithNullVariable(), 
        				query.getObjectWithNullVariable())), 
        		query.toConstruct());

        logger.debug("New SparqlConstructRequest created");
    }

    public SparqlConstructResponse createResponse() {
    	return new SparqlConstructResponse(this);
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public ClosableIterableWrapper queryDatastore(AbstractCanOverlay overlay) {
		return new ClosableIterableWrapper(
						((SemanticCanOverlay) overlay).getDatastore().sparqlConstruct(
								DSpaceProperties.DEFAULT_CONTEXT, super.getSparqlConstructQuery()));
	}

}
