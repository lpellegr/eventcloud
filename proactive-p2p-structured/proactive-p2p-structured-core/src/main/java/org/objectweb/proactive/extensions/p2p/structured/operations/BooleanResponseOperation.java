package org.objectweb.proactive.extensions.p2p.structured.operations;


/**
 * Defines a basic response for the {@link ResponseOperation}.
 * 
 * @author Laurent Pellegrino
 */
public class BooleanResponseOperation implements ResponseOperation {

    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates the status of the action : succeeded or not.
     */
    private boolean value = false;

    /**
     * Constructor.
     * 
     * Indicates if the neighbor has been correctly removed.
     */
    public BooleanResponseOperation(boolean value) {
        this.value = value;
    }

    /**
     * Indicates if the action has succeeded.
     * 
     * @return <code>true</code> if the action has succeeded, <code>false</code>
     *         otherwise.
     */
    public boolean getValue() {
        return this.value;
    }

}
