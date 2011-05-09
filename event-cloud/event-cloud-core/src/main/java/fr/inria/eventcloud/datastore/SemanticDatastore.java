package fr.inria.eventcloud.datastore;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.datastore.PersistentDatastore;
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
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.util.SemanticHelper;

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
public abstract class SemanticDatastore extends PersistentDatastore implements
        SemanticDatastoreOperations {

    private static final Logger logger =
            LoggerFactory.getLogger(SemanticDatastore.class);

    private final Map<Model, Boolean> dirtyTable =
            new HashMap<Model, Boolean>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private final Lock readLock = rwl.readLock();

    private final Lock writeLock = rwl.writeLock();

    private final ScheduledExecutorService consistencyCheckerThread =
            Executors.newSingleThreadScheduledExecutor();

    protected ModelSet rootModel = null;

    /**
     * Constructs a new SemanticDatastore.
     */
    public SemanticDatastore() {
        super(new File(EventCloudProperties.REPOSITORIES_PATH.getValue()));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (initialized.get()) {
                    // forces datastore close operation if not closed manually
                    close();
                }
            }
        });

        logger.info(
                "New repository with id {} has been created in {}",
                super.getId(), super.getPath());
    }

    /**
     * Creates a new RDF2Go {@link ModelSet} as root model.
     * 
     * @param repositoryPath
     *            where to store repository files.
     * @return the new RDF2Go root {@link Model} or {@link ModelSet}.
     */
    public abstract ModelSet createRootModel(File repositoryPath);

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
            logger.debug("Fix consistency due to read operation");
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

    /*
     * Implementation of SemanticDatastoreOperations interface
     */

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
     * Implementation of PersistentDatastore abstract methods
     */

    @Override
    protected void internalOpen() {
        super.path.mkdirs();

        this.rootModel = this.createRootModel(super.path);
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
     * {@inheritDoc}
     */
    @Override
    protected synchronized void internalClose(boolean remove) {
        this.consistencyCheckerThread.shutdown();
        this.fixConsistency();
        this.dirtyTable.clear();
        this.rootModel.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void internalClose() {
        this.internalClose(false);
    }

    /*
     * Implementation of PeerDataHandler interface
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void affectDataReceived(Object dataReceived) {
        ClosableIterator<Statement> data =
                ((ClosableIterableWrapper) dataReceived).toRDF2Go().iterator();

        this.addAll(EventCloudProperties.DEFAULT_CONTEXT, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object retrieveAllData() {
        return this.sparqlConstruct(
                EventCloudProperties.DEFAULT_CONTEXT,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object retrieveDataIn(Object interval) {
        Zone zone = (Zone) interval;

        Set<Statement> statementsToTransfert = new HashSet<Statement>();
        ClosableIterable<Statement> result =
                this.sparqlConstruct(
                        EventCloudProperties.DEFAULT_CONTEXT,
                        "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");

        ClosableIterator<Statement> it = result.iterator();
        Statement stmt;
        String subject;
        String predicate;
        String object;

        while (it.hasNext()) {
            stmt = it.next();

            subject =
                    SemanticHelper.parseTripleElement(stmt.getSubject()
                            .toString());
            predicate =
                    SemanticHelper.parseTripleElement(stmt.getPredicate()
                            .toString());
            object =
                    SemanticHelper.parseTripleElement(stmt.getObject()
                            .toString());

            // Yeah, manual filtering is really ugly!
            if (subject.compareTo(zone.getLowerBound((byte) 0).toString()) >= 0
                    && subject.compareTo(zone.getUpperBound((byte) 0)
                            .toString()) < 0
                    && predicate.compareTo(zone.getLowerBound((byte) 1)
                            .toString()) >= 0
                    && predicate.compareTo(zone.getUpperBound((byte) 1)
                            .toString()) < 0
                    && object.compareTo(zone.getLowerBound((byte) 2).toString()) >= 0
                    && object.compareTo(zone.getUpperBound((byte) 2).toString()) < 0) {

                statementsToTransfert.add(stmt);
            }
        }

        return new ClosableIterableWrapper(
                SemanticHelper.generateClosableIterable(statementsToTransfert));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object removeDataIn(Object interval) {
        Zone zone = (Zone) interval;

        ClosableIterable<Statement> queryResult =
                this.sparqlConstruct(
                        EventCloudProperties.DEFAULT_CONTEXT,
                        "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");

        ClosableIterator<Statement> it = queryResult.iterator();
        Statement stmt;
        String subject;
        String predicate;
        String object;

        Set<Statement> result = new HashSet<Statement>();

        while (it.hasNext()) {
            stmt = it.next();

            subject =
                    SemanticHelper.parseTripleElement(stmt.getSubject()
                            .toString());
            predicate =
                    SemanticHelper.parseTripleElement(stmt.getPredicate()
                            .toString());
            object =
                    SemanticHelper.parseTripleElement(stmt.getObject()
                            .toString());

            if (subject.compareTo(zone.getLowerBound((byte) 0).toString()) >= 0
                    && subject.compareTo(zone.getUpperBound((byte) 0)
                            .toString()) < 0
                    && predicate.compareTo(zone.getLowerBound((byte) 1)
                            .toString()) >= 0
                    && predicate.compareTo(zone.getUpperBound((byte) 1)
                            .toString()) < 0
                    && object.compareTo(zone.getLowerBound((byte) 2).toString()) >= 0
                    && object.compareTo(zone.getUpperBound((byte) 2).toString()) < 0) {
                result.add(stmt);
                this.removeStatement(EventCloudProperties.DEFAULT_CONTEXT, stmt);
            }

        }

        return new ClosableIterableWrapper(result);
    }

}
