package fr.inria.eventcloud.datastore;

import java.io.File;
import java.util.UUID;

import org.ontoware.rdf2go.model.ModelSet;
import org.openrdf.rdf2go.RepositoryModelSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Sesame datastore which is initialized in memory by using {@link MemoryStore}.
 * 
 * @author lpellegr
 */
public class SesameInMemoryDatastore extends SemanticDatastore {

    public SesameInMemoryDatastore() {
        super(false);
    }

    public ModelSet createRootModel(File repositoryPath) {
        MemoryStore store = new MemoryStore();
        store.setPersist(false);
        store.setDataDir(new File("mem-" + UUID.randomUUID().toString()));
        Repository spacesRepository = new SailRepository(store);
        try {
            spacesRepository.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return new RepositoryModelSet(spacesRepository);
    }
    
}
