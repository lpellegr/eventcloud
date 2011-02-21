package org.objectweb.proactive.extensions.p2p.structured.operations;


/**
 * Response used to returned various type of objects.
 * 
 * @author Laurent Pellegrino
 */
public class GenericResponseOperation<T> implements ResponseOperation {

    private static final long serialVersionUID = 1L;
    
    private T value;

    public GenericResponseOperation(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

}
