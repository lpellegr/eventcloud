package fr.inria.eventcloud.datastore;

import org.ontoware.rdf2go.model.ModelSet;
import org.openrdf.rdf2go.RepositoryModelSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * 
 * @author lpellegr
 */
public class SesameModelFactory {

    public static ModelSet createModelSet() {
	MemoryStore store = new MemoryStore();
	store.setPersist(false);
	final Repository spacesRepository = new SailRepository(store);
	try {
	    spacesRepository.initialize();
	} catch (RepositoryException e) {
	    e.printStackTrace();
	}

	return new RepositoryModelSet(spacesRepository);
    }
    
}