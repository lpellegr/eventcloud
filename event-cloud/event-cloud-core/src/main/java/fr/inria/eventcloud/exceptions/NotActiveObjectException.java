package fr.inria.eventcloud.exceptions;

import org.objectweb.proactive.core.mop.StubObject;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;

/**
 * An exception thrown if an object which is not an active object is casted into
 * {@link StubObject}.
 * 
 * @author lpellegr
 */
public class NotActiveObjectException extends SemanticSpaceException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>NotActiveObjectException</code> with no specified
     * detail message.
     */
    public NotActiveObjectException() {
        super();
    }

}
