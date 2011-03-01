package fr.inria.eventcloud.kernel.operations.datastore;

import java.io.Serializable;

/**
 * 
 * @author lpellegr
 */
public class DatastoreResponseOperation<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private T value;

    public DatastoreResponseOperation(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

}
