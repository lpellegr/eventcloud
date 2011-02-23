package fr.inria.eventcloud.messages;

import java.io.Serializable;


/**
 * Represents an {@link Object} as an array of bytes.
 *
 * @author lpellegr
 */
public class ObjectAsByteArray implements Serializable {
    
    private static final long serialVersionUID = 1L;
  
    private final byte[] bytes; 

    /**
     * Constructor.
     */
    public ObjectAsByteArray(byte[] b) {
        this.bytes = b;
    }

    /**
     * Returns the internal byte array. Note that this is a live
     * reference to the actual data, not a copy.
     */
    public byte[] getBytes() {
        return this.bytes;
    }

}