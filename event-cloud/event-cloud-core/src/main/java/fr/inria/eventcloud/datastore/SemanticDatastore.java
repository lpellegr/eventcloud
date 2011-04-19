package fr.inria.eventcloud.datastore;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

import fr.inria.eventcloud.config.EventCloudProperties;

/**
 * Provides all methods that can be performed on a semantic context data store
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
 * modifications, otherwise it waits for the next round. Secondly, if a read
 * operation is handled and some modifications must be committed then they are
 * committed before to execute the read operation.
 * 
 * @author lpellegr
 */
public abstract class SemanticDatastore implements SemanticDatastoreOperations {

    private static final Logger logger =
            LoggerFactory.getLogger(SemanticDatastore.class);

    private final Map<Model, Boolean> dirtyTable =
            new HashMap<Model, Boolean>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private final Lock readLock = rwl.readLock();

    private final Lock writeLock = rwl.writeLock();

    private final ScheduledExecutorService consistencyCheckerThread =
            Executors.newSingleThreadScheduledExecutor();

    private final UUID id;

    protected File dataStorePath;

    protected ModelSet rootModel = null;

    protected AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Constructs a new SemanticDatastore.
     */
    public SemanticDatastore() {
        this.id = UUID.randomUUID();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                close();
            }
        });

        if (logger.isDebugEnabled()) {
            logger.debug("New datastore with id '" + this.id
                    + "' has been created.");
        }
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
    public void open() {
        if (this.initialized.getAndSet(true)) {
            throw new IllegalStateException(
                    "SemanticDataStore already initialized");
        }

        this.dataStorePath =
                new File(
                        EventCloudProperties.REPOSITORIES_PATH.getValue(),
                        this.id.toString());
        this.dataStorePath.mkdirs();

        this.rootModel = this.createRootModel(this.dataStorePath);
        this.rootModel.open();
        this.rootModel.setAutocommit(false);

        this.consistencyCheckerThread.scheduleWithFixedDelay(
                new Runnable() {
                    public void run() {
                        tryToFixConsistency();
                    }
                }, 0, EventCloudProperties.CONSISTENCY_TIMEOUT.getValue(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * Shutdown the repository.
     */
    public synchronized void close() {
        if (this.initialized.get()) {
            this.consistencyCheckerThread.shutdown();
            this.fixConsistency();
            this.dirtyTable.clear();
            this.rootModel.close();
            this.initialized.set(false);
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

    public void atomicAddAll(URI context, Iterator<? extends Statement> triples) {
        Model model = this.rootModel.getModel(context);
        model.setAutocommit(false);
        model.addAll(triples);
        model.commit();
    }

    // Interface implementation -------------------------

    public void addAll(URI context, Iterator<? extends Statement> other) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(context);
            model.addAll(other);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI context, Statement statement) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(context);
            model.addStatement(statement);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI context, Resource subject, URI predicate,
                             String literal) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(context);
            model.addStatement(subject, predicate, literal);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI context, Resource subject, URI predicate,
                             String literal, String languageTag) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(context);
            model.addStatement(subject, predicate, literal, languageTag);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI context, Resource subject, URI predicate,
                             String literal, URI datatypeURI) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(context);
            model.addStatement(subject, predicate, literal, datatypeURI);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI context, String subjectURIString,
                             URI predicate, String literal) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(context);
            model.addStatement(subjectURIString, predicate, literal);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI context, String subjectURIString,
                             URI predicate, String literal, String languageTag) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(context);
            model.addStatement(
                    subjectURIString, predicate, literal, languageTag);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void addStatement(URI context, String subjectURIString,
                             URI predicate, String literal, URI datatypeURI) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(context);
            model.addStatement(
                    subjectURIString, predicate, literal, datatypeURI);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public boolean contains(URI context, Statement s) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(context).contains(s);
        } finally {
            this.readLock.unlock();
        }
    }

    public boolean contains(URI context, ResourceOrVariable subject,
                            UriOrVariable predicate, NodeOrVariable object) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(context).contains(
                    subject, predicate, object);
        } finally {
            this.readLock.unlock();
        }
    }

    public boolean contains(URI context, ResourceOrVariable subject,
                            UriOrVariable predicate, String plainLiteral) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(context).contains(
                    subject, predicate, plainLiteral);
        } finally {
            this.readLock.unlock();
        }
    }

    public ClosableIterator<Statement> findStatements(URI context,
                                                      TriplePattern triplepattern) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(context).findStatements(
                    triplepattern);
        } finally {
            this.readLock.unlock();
        }
    }

    public void removeAll(URI context) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(context);
            model.removeAll();
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeAll(URI context, Iterator<? extends Statement> statements) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(context);
            model.removeAll(statements);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI context, Statement statement) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(context);
            model.removeStatement(
                    statement.getSubject(), statement.getPredicate(),
                    statement.getObject());
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI context, Resource subject, URI predicate,
                                String literal) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(context);
            model.removeStatement(subject, predicate, literal);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI context, Resource subject, URI predicate,
                                String literal, String languageTag) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(context);
            model.removeStatement(subject, predicate, literal, languageTag);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI context, Resource subject, URI predicate,
                                String literal, URI datatypeURI) {
        this.writeLock.lock();
        try {
            Model model = this.rootModel.getModel(context);
            model.removeStatement(subject, predicate, literal, datatypeURI);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI context, String subjectURIString,
                                URI predicate, String literal) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(context);
            model.removeStatement(subjectURIString, predicate, literal);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI context, String subjectURIString,
                                URI predicate, String literal,
                                String languageTag) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(context);
            model.removeStatement(
                    subjectURIString, predicate, literal, languageTag);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatement(URI context, String subjectURIString,
                                URI predicate, String literal, URI datatypeURI) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(context);
            model.removeStatement(
                    subjectURIString, predicate, literal, datatypeURI);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatements(URI context, ResourceOrVariable subject,
                                 UriOrVariable predicate, NodeOrVariable object) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(context);
            model.removeStatements(subject, predicate, object);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void removeStatements(URI context, TriplePattern triplePattern) {
        this.writeLock.lock();

        try {
            Model model = this.rootModel.getModel(context);
            model.removeStatements(triplePattern);
            this.updateDirtyTable(model);
        } finally {
            this.writeLock.unlock();
        }
    }

    public boolean sparqlAsk(URI context, String query) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(context).sparqlAsk(query);
        } finally {
            this.readLock.unlock();
        }
    }

    public ClosableIterable<Statement> sparqlConstruct(URI context, String query) {

        this.fixConsistency();

        this.readLock.lock();
        try {
            Model mod = this.rootModel.getModel(context);
            return mod.sparqlConstruct(query);
        } finally {
            this.readLock.unlock();
        }
    }

    public ClosableIterable<Statement> sparqlDescribe(URI context, String query) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(context).sparqlDescribe(query);
        } finally {
            this.readLock.unlock();
        }
    }

    public QueryResultTable sparqlSelect(URI context, String queryString) {
        this.fixConsistency();

        this.readLock.lock();
        try {
            return this.rootModel.getModel(context).sparqlSelect(queryString);
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

}
