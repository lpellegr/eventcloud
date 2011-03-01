package fr.inria.eventcloud.exceptions;

import org.objectweb.proactive.core.node.Node;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;

/**
 * An exception thrown by trying to use the reified object of remote object B
 * from a remote object A when A and B are not on the same JVM (i.e. not using
 * the same {@link Node}).
 * 
 * @author lpellegr
 */
public class NotOnSameRuntimeException extends SemanticSpaceException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>NotOnSameRuntimeException</code> with no specified
     * detail message.
     */
    public NotOnSameRuntimeException() {
        super();
    }

}
