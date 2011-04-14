package org.objectweb.proactive.extensions.p2p.structured.operations;

/**
 * Defines a basic boolean (true/false) response operation.
 * 
 * @author lpellegr
 */
public class BooleanResponseOperation extends GenericResponseOperation<Boolean> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * Indicates if the neighbor has been correctly removed.
     */
    public BooleanResponseOperation(boolean value) {
        super(value);
    }

}
