package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.overlay.can.SemanticCanOverlay;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;

/**
 * Operation used to execute a SPARQL Construct query directly on a given peer.
 * 
 * @author lpellegr
 */
public class SparqlConstructOperation implements Operation {

	private static final long serialVersionUID = 1L;

	private URI context;
	
	private String sparqlConstructQuery;

	public SparqlConstructOperation(URI context, String sparqlConstructQuery) {
		this.context = context;
		this.sparqlConstructQuery = sparqlConstructQuery;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseOperation handle(StructuredOverlay overlay) {
		return new SparqlConstructResponseOperation(
					new ClosableIterableWrapper(
							((SemanticCanOverlay) overlay).getDatastore().sparqlConstruct(
									context, this.sparqlConstructQuery)));
	}
	
}
