package fr.inria.eventcloud.datastore;

import java.io.File;
import java.util.Properties;

import org.ontoware.rdf2go.ModelFactory;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.ModelSet;

/**
 * Defines a BigOwlim binding in order to perform some frequent operations.
 * 
 * @author lpellegr
 */
public class OwlimDatastore extends SemanticDatastore {

    private boolean autoRemove = false;

    /**
     * Constructs a new Owlim datastore. The auto-remove property is set to
     * false, so it doesn't remove the repository when it is closed.
     */
    public OwlimDatastore() {
        this(false);
    }

    /**
     * Constructor.
     * 
     * @param autoRemove
     *            indicates if the repository must be removed when it is closed.
     */
    public OwlimDatastore(boolean autoRemove) {
        super();
        this.autoRemove = autoRemove;
    }

    public ModelSet createRootModel(File repositoryPath) {
        Properties props = new Properties();
        props.setProperty(
                ModelFactory.STORAGE, repositoryPath.getAbsolutePath());
        props.setProperty("auto-remove", this.autoRemove
                ? "true" : "false");

        RDF2Go.register("com.ontotext.trree.rdf2go.OwlimModelFactory");

        return RDF2Go.getModelFactory().createModelSet(props);
    }

    public boolean isAutoRemoveEnabled() {
        return this.autoRemove;
    }

}
