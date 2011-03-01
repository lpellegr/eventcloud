package fr.inria.eventcloud.datastore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.TriplePattern;
import org.ontoware.rdf2go.model.node.NodeOrVariable;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.ResourceOrVariable;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.UriOrVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.util.DSpaceProperties;

/**
 * Provides all methods that can be performed on a semantic space data store
 * conform to {@link RDF2Go}.
 * 
 * Operations are synchronized by using a {@link ReentrantReadWriteLock} which
 * allows to have multiple read access but only one write operation at the same
 * time. More over, write operations are not committed each time a write
 * operation operation is called (for performance reasons because commit implies
 * I/O).
 * 
 * In order to assert consistency when read operations are performed, two
 * mechanisms are introduced. First, a Thread periodically check if no write
 * lock is held by an another thread and if it is true it commits pending
 * modifications, otherwise it waits for next round. Secondly, if a read
 * operation is handled and some modifications must be committed then they are
 * commited before to execute the read operation.
 * 
 * @author lpellegr
 */
public abstract class SemanticDatastore implements SemanticDatastoreOperations {

    private static final Logger logger = LoggerFactory.getLogger(SemanticDatastore.class);

    private final Map<Model, Boolean> dirtyTable = new HashMap<Model, Boolean>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    
    private final Lock readLock = rwl.readLock();
    
    private final Lock writeLock = rwl.writeLock();
    
    private final ScheduledExecutorService consistencyCheckerThread = 
        Executors.newSingleThreadScheduledExecutor();

    private UUID id;

    protected File dataStorePath;

    protected ModelSet rootModel = null;

    private volatile boolean initialized = false;

    private boolean loadProperties = true;

    private String repositoryIdToRestore;

    /**
     * Constructor.
     * 
     * @param loadProperties
     *            indicates if it is necessary to load properties from
     *            preference file or not.
     */
    public SemanticDatastore(boolean loadProperties) {
        this.id = UUID.randomUUID();
        this.loadProperties = loadProperties;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (initialized) {
                    close();
                }
            }
        });

        logger.debug("New datastore with uuid={} has been created", this.id);
    }

    public SemanticDatastore() {
        this(true);
    }

    /**
     * Creates a new RDF2Go {@link ModelSet} as root model.
     * 
     * @param repositoryPath
     *            where to store repository files.
     * @return the new RDF2Go root {@link Model} or {@link ModelSet}.
     */
    public abstract ModelSet createRootModel(File repositoryPath);

    /**
     * Initialize the datastore.
     */
    public synchronized void open() {
        if (this.initialized) {
            throw new IllegalStateException("SemanticDataStore already initialized");
        }

        File repositoryPath = null;
        if (this.loadProperties) {
            this.loadProperties();
            repositoryPath = new File(this.dataStorePath, this.id.toString());
            repositoryPath.mkdirs();
        }

        this.rootModel = this.createRootModel(repositoryPath);
        this.rootModel.open();
        this.rootModel.setAutocommit(false);

        this.consistencyCheckerThread.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                tryToFixConsistency();
            }
        }, 0, DSpaceProperties.DSPACE_CONSISTENCY_TIMEOUT.getValue(), TimeUnit.MILLISECONDS);

        this.initialized = true;
    }

    /**
     * Loads properties.
     */
    private void loadProperties() {
    	String configurationFile = 
    	    DSpaceProperties.DSPACE_CONFIGURATION_FILE.getValue();
        File spaceConfigurationFile = null;
        if (configurationFile != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading properties from -D{}={}",
                			 DSpaceProperties.DSPACE_CONFIGURATION_FILE,
                			 configurationFile);
            }
            spaceConfigurationFile = new File(configurationFile);
            
            try {
                this.loadPropertiesFrom(spaceConfigurationFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File defaultSpacePropertiesPath = new File(
            									DSpaceProperties.getDefaultPathForConfigurationFiles(),
                    							"space.properties");
            if (logger.isDebugEnabled()) {
                logger.debug("Loading properties from default path=" + defaultSpacePropertiesPath);
            }
            if (defaultSpacePropertiesPath.exists()) {
                this.parseProperties(defaultSpacePropertiesPath);
            } else {
                this.dataStorePath = new File(
                						"/tmp/dspace/repositories");
            }
        }
    }

    /**
     * Loads properties from a specified properties file.
     * 
     * @param spaceConfigurationFile
     *            the path to the configuration file to load.
     * @throws FileNotFoundException
     *             if specified path doesn't exist.
     */
    private void loadPropertiesFrom(File spaceConfigurationFile) throws FileNotFoundException {
        if (spaceConfigurationFile != null && spaceConfigurationFile.exists()) {
            this.parseProperties(spaceConfigurationFile);
        } else {
            throw new FileNotFoundException(spaceConfigurationFile.getAbsolutePath() + " not found");
        }
    }

    /**
     * Parses properties file.
     * 
     * @param spaceConfigurationFile
     *            the properties file to parse.
     */
    private void parseProperties(File spaceConfigurationFile) {
        Properties props = new Properties();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(spaceConfigurationFile);
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Properties loaded=" + props.values());
        }

        if (props.getProperty("repositories.path") != null) {
            this.dataStorePath = new File(props.getProperty("repositories.path"));
        } else {
            throw new IllegalArgumentException(
                    "'repositories.path' property is not defined in "
                            + spaceConfigurationFile.getAbsolutePath());
        }

        String repositoryID = 
            DSpaceProperties.DSPACE_REPOSITORY_RESTORE_ID.getValue();
        if (repositoryID != null) {
            this.repositoryIdToRestore = repositoryID;
            File repositoryToRestore = new File(this.dataStorePath, this.repositoryIdToRestore);
            if (!repositoryToRestore.exists() || this.repositoryIdToRestore == null) {
                throw new IllegalArgumentException("Repository '"
                        + repositoryToRestore.getAbsolutePath() + "' doesn't exist");
            } else {
                this.id = UUID.fromString(this.repositoryIdToRestore);
            }
        }

        if (!this.dataStorePath.exists()) {
            this.dataStorePath.mkdirs();
        }
    }

    /**
     * Shutdown the repository.
     */
    public synchronized void close() {
        if (this.initialized) {
            this.consistencyCheckerThread.shutdown();
            this.fixConsistency();
            this.dirtyTable.clear();
            this.rootModel.close();
            this.initialized = false;
        }
    }

    private void updateDirtyTable(Model model) {
        this.dirtyTable.put(model, true);
    }

    private void commitPendingModifications() {
        for (Entry<Model, Boolean> entry : this.dirtyTable.entrySet()) {
            if (entry.getValue()) {
                entry.getKey().commit();
                entry.setValue(false);
            }
        }
    }

    /**
     * Called by each read operations to be sure that all pending modifications
     * are committed.
     */
    public void fixConsistency() {
        this.writeLock.lock();
        try {
            this.commitPendingModifications();
			logger.debug("Fix consistency due to read operation.");
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * Called by the consistency checker thread in order to try to fix
     * consistency by committing pending modifications if no one is using the
     * data store.
     */
    public void tryToFixConsistency() {
        if (this.writeLock.tryLock()) {
            try {
                this.commitPendingModifications();
                logger.trace("Fix consistency by consistency checker thread.");
            } finally {
                this.writeLock.unlock();
            }
        }
    }

    public void atomicAddAll(URI space, Iterator<? extends Statement> triples) {
    	Model model = this.rootModel.getModel(space);
    	model.setAutocommit(false);
    	model.addAll(triples);
    	model.commit();
    }
    
    // Interface implementation -------------------------

    public void addAll(URI space, Iterator<? extends Statement> other)
            throws SemanticSpaceException {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(space);
            model.addAll(other);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI space, Statement statement) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(space);
            model.addStatement(statement);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI space, Resource subject, URI predicate, String literal) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(space);
            model.addStatement(subject, predicate, literal);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI space, Resource subject, URI predicate,
                             String literal, String languageTag) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(space);
            model.addStatement(subject, predicate, literal, languageTag);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI space, Resource subject, URI predicate,
                             String literal, URI datatypeURI) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(space);
            model.addStatement(subject, predicate, literal, datatypeURI);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI space, String subjectURIString, URI predicate,
                             String literal) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(space);
            model.addStatement(subjectURIString, predicate, literal);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI space, String subjectURIString, URI predicate,
                             String literal, String languageTag) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(space);
            model.addStatement(subjectURIString, predicate, literal, languageTag);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI space, String subjectURIString, URI predicate,
                             String literal, URI datatypeURI) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(space);
            model.addStatement(subjectURIString, predicate, literal, datatypeURI);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public boolean contains(URI space, Statement s) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(space).contains(s);
        } finally {
            this.readLock.unlock();
        }
    }

    public boolean contains(URI space, ResourceOrVariable subject,
                            UriOrVariable predicate, NodeOrVariable object) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(space).contains(subject, predicate, object);
        } finally {
            this.readLock.unlock();
        }
    }

    public boolean contains(URI space, ResourceOrVariable subject,
                            UriOrVariable predicate, String plainLiteral) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(space).contains(subject, predicate, plainLiteral);
        } finally {
            this.readLock.unlock();
        }
    }

    public ClosableIterator<Statement> findStatements(URI space,
            TriplePattern triplepattern) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(space).findStatements(triplepattern);
        } finally {
            this.readLock.unlock();
        }
    }

    public void removeAll(URI space) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(space);
            model.removeAll();
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeAll(URI space, Iterator<? extends Statement> statements) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(space);
            model.removeAll(statements);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI space, Statement statement) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(space);
            model.removeStatement(statement.getSubject(),
                    statement.getPredicate(), statement.getObject());
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI space, Resource subject, URI predicate, 
                                String literal) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(space);
            model.removeStatement(subject, predicate, literal);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI space, Resource subject, URI predicate,
                                String literal, String languageTag) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(space);
            model.removeStatement(subject, predicate, literal, languageTag);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI space, Resource subject, URI predicate,
                                String literal, URI datatypeURI) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(space);
            model.removeStatement(subject, predicate, literal, datatypeURI);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI space, String subjectURIString, URI predicate,
                                String literal) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(space);
            model.removeStatement(subjectURIString, predicate, literal);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI space, String subjectURIString, URI predicate,
                                String literal, String languageTag) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(space);
            model.removeStatement(subjectURIString, predicate, literal, languageTag);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI space, String subjectURIString, URI predicate,
            String literal,
            URI datatypeURI) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(space);
            model.removeStatement(subjectURIString, predicate, literal, datatypeURI);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatements(URI space, ResourceOrVariable subject,
            UriOrVariable predicate,
            NodeOrVariable object) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(space);
            model.removeStatements(subject, predicate, object);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatements(URI space, TriplePattern triplePattern) {
        this.writeLock.lock();
        
        try {
            Model model = this.rootModel.getModel(space);
            model.removeStatements(triplePattern);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public boolean sparqlAsk(URI space, String query) {
        this.fixConsistency();
        
        this.readLock.lock();
        try {
            return this.rootModel.getModel(space).sparqlAsk(query);
        } finally {
            this.readLock.unlock();
        }
    }

    public ClosableIterable<Statement> sparqlConstruct(URI space, String query) {
        
        this.fixConsistency();

        this.readLock.lock();
        try {
            Model mod = this.rootModel.getModel(space);
            return mod.sparqlConstruct(query);
        } finally {
            this.readLock.unlock();
        }
    }
    
    public ClosableIterable<Statement> sparqlDescribe(URI space, String query) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(space).sparqlDescribe(query);
        } finally {
            this.readLock.unlock();
        }
    }

    public QueryResultTable sparqlSelect(URI space, String queryString) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(space).sparqlSelect(queryString);
        } finally {
            this.readLock.unlock();
        }
    }

    /*
     * Accessors & Mutators
     */

    public File getDataStorePath() {
        return this.dataStorePath;
    }

    public UUID getId() {
        return this.id;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public boolean isLoadProperties() {
        return this.loadProperties;
    }

    public void setLoadProperties(boolean loadProperties) {
        this.loadProperties = loadProperties;
    }

}
