package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;

import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;

/**
 * Response operation associated to {@link SparqlConstructOperation}.
 * 
 * @author lpellegr
 */
public class SparqlConstructResponseOperation implements ResponseOperation {

	private static final long serialVersionUID = 1L;

	private final ClosableIterableWrapper result;

	public SparqlConstructResponseOperation(ClosableIterableWrapper result) {
		this.result = result;
	}

	public ClosableIterableWrapper getResult() {
		return this.result;
	}
	
}
