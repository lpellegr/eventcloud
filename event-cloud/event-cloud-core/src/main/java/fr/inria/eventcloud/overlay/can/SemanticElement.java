package fr.inria.eventcloud.overlay.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;

import fr.inria.eventcloud.util.SemanticHelper;


/**
 * Represents a semantic coordinate element. This kind of element 
 * extends {@link StringElement} and removes some prefix which are 
 * specific to semantic data in order to improve the load balancing. 
 * 
 * @author lpellegr
 */
public class SemanticElement extends StringElement {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new coordinate element with the specified <code>value</code>.
	 * 
	 * @param value the value that will be parsed.
	 */
	public SemanticElement(String value) {
		super(SemanticHelper.parseTripleElement(value));
	}
	
	public SemanticElement(Resource value) {
		super(SemanticHelper.parseTripleElement(value.toString()));
	}
	
	public SemanticElement(Node value) {
		super(SemanticHelper.parseTripleElement(value.toString()));
	}
	
}
