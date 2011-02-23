package fr.inria.eventcloud.rdf2go.wrappers;

import java.io.Serializable;

/**
 * Defines contract that all RDF2Go wrappers must verify.
 * 
 * @author lpellegr
 */
public interface RDF2GoWrapper<T> extends Serializable {

    /**
     * Converts the current wrapper object to a concrete RDF2Go object.
     * 
     * @return a concrete RDF2Go object.
     */
    public abstract T toRDF2Go();

}
