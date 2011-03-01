package fr.inria.eventcloud.kernel;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.TriplePattern;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.NodeOrVariable;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.ResourceOrVariable;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.UriOrVariable;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.api.SemanticSpaceManagement;
import fr.inria.eventcloud.datastore.OwlimDatastore;
import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.kernel.operations.datastore.DatastoreOperation;
import fr.inria.eventcloud.kernel.operations.datastore.DatastoreResponseOperation;


/**
 * SemanticSpaceOverlayKernel is used to simplify the deployment of storage
 * engine on several machines. Moreover, a kernel may be associated with
 * multiple {@link Peer}s but all the peers and the kernel must be on the same
 * machine.
 * 
 * A {@link SemanticSpaceOverlayKernel} can optionally be constructed with a
 * given {@link NodeProvider}. When it is used, the calls to
 * {@link SemanticSpaceManagement} that implies to create new active objects use
 * the {@link NodeProvider} in order to deploy the new active objects on
 * {@link Node}s provided by the {@link NodeProvider}.
 * 
 * It is possible to use a SemanticSpaceOverlaykernel through a
 * {@link ManagementConnector} to perform operations restricted to
 * administrators of SemanticSpaces via {@link SemanticSpaceManagement}.
 * 
 * @author lpellegr
 */
public class SemanticSpaceOverlayKernel implements InitActive, EndActive, Serializable {

    private static final long serialVersionUID = 1L;

    private transient SemanticDatastore dataStore;

    private List<Tracker> trackers = new ArrayList<Tracker>();

    private String bindingName;

    private final boolean autoRemove;

    public SemanticSpaceOverlayKernel() {
        this.autoRemove = false;
    }
    
    /**
     * Constructor with trackers in parameters.
     * 
     * @param trackers
     *            trackers stubs that are used in order to maintains an entry
     *            point for a network.
     */
    public SemanticSpaceOverlayKernel(Tracker[] trackers) {
        this();
        
        for (Tracker tracker : trackers) {
            this.trackers.add(tracker);
        }
    }

    public SemanticSpaceOverlayKernel(Tracker[] trackers, boolean autoRemove) {
        this.autoRemove = autoRemove;
        
        for (Tracker tracker : trackers) {
            this.trackers.add(tracker);
        }
    }

    public SemanticDatastore getDataStore() {
        return this.dataStore;
    }

    public File getDataStorePath() {
        return this.dataStore.getDataStorePath();
    }

    public SemanticSpaceOverlayKernel getLocalReference() {
        return this;
    }

    public DatastoreResponseOperation<?> send(DatastoreOperation operation) {
        return operation.handle(this.dataStore);
    }

    public void initActivity(Body body) {
        this.dataStore = new OwlimDatastore(this.autoRemove);
        this.dataStore.open();
    }

    public void endActivity(Body body) {
        this.dataStore.close();
    }

    /**
     * @return the bindingName
     */
    public String getBindingName() {
        return this.bindingName;
    }

    /**
     * Returns a random Chord {@link Peer} from the tracker list maintained.
     * 
     * @return a random Chord {@link Peer} from the tracker list maintained.
     */
    public Tracker getRandomTracker() {
        return this.trackers.get(ProActiveRandom.nextInt(this.trackers.size()));
    }

    public String register() {
        return this.register(null);
    }

    public String register(String id) {
        if (this.bindingName == null) {
            try {
                this.bindingName = PAActiveObject.registerByName(PAActiveObject.getStubOnThis(),
                        id == null ? UUID.randomUUID().toString() : id + "/kernel");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.bindingName;
    }

    public void atomicAddAll(URI space, Iterator<? extends Statement> triples) {
		this.dataStore.atomicAddAll(space, triples);
    }
    
    /*
     * Delegate methods to datastore.
     */
    public void addAll(URI space, Iterator<? extends Statement> other)
            throws SemanticSpaceException {
        this.dataStore.addAll(space, other);
    }

    public void addStatement(URI space, Resource subject, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException {
        this.dataStore.addStatement(space, subject, predicate, literal, languageTag);
    }

    public void addStatement(URI space, Resource subject, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException {
        this.dataStore.addStatement(space, subject, predicate, literal, datatypeURI);
    }

    public void addStatement(URI space, Resource subject, URI predicate, String literal)
            throws SemanticSpaceException {
        this.dataStore.addStatement(space, subject, predicate, literal);
    }

    public void addStatement(URI space, Statement statement) throws SemanticSpaceException {
        this.dataStore.addStatement(space, statement);
    }

    public void addStatement(URI space, String subjectURIString, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException {
        this.dataStore.addStatement(space, subjectURIString, predicate, literal, languageTag);
    }

    public void addStatement(URI space, String subjectURIString, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException {
        this.dataStore.addStatement(space, subjectURIString, predicate, literal, datatypeURI);
    }

    public void addStatement(URI space, String subjectURIString, URI predicate, String literal)
            throws SemanticSpaceException {
        this.dataStore.addStatement(space, subjectURIString, predicate, literal);
    }

    public boolean contains(URI space, ResourceOrVariable subject, UriOrVariable predicate,
            NodeOrVariable object) throws SemanticSpaceException {
        return this.dataStore.contains(space, subject, predicate, object);
    }

    public boolean contains(URI space, ResourceOrVariable subject, UriOrVariable predicate,
            String plainLiteral) throws SemanticSpaceException {
        return this.dataStore.contains(space, subject, predicate, plainLiteral);
    }

    public boolean contains(URI space, Statement s) throws SemanticSpaceException {
        return this.dataStore.contains(space, s);
    }

    public ClosableIterator<Statement> findStatements(URI space, TriplePattern triplepattern)
            throws SemanticSpaceException {
        return this.dataStore.findStatements(space, triplepattern);
    }

    public boolean hasStatements(URI space) throws SemanticSpaceException {
        return this.dataStore.sparqlAsk(space,
                    "ASK { GRAPH<" + space + "> { ?s ?p ?o } . }");
    }

    public void open() {
        this.dataStore.open();
    }

    public void removeAll(URI space, Iterator<? extends Statement> statements)
            throws SemanticSpaceException {
        this.dataStore.removeAll(space, statements);
    }

    public void removeAll(URI space) throws SemanticSpaceException {
        this.dataStore.removeAll(space);
    }

    public void removeStatement(URI space, Resource subject, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException {
        this.dataStore.removeStatement(space, subject, predicate, literal, languageTag);
    }

    public void removeStatement(URI space, Resource subject, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException {
        this.dataStore.removeStatement(space, subject, predicate, literal, datatypeURI);
    }

    public void removeStatement(URI space, Resource subject, URI predicate, String literal)
            throws SemanticSpaceException {
        this.dataStore.removeStatement(space, subject, predicate, literal);
    }

    public void removeStatement(URI space, Statement statement) throws SemanticSpaceException {
        this.dataStore.removeStatement(space, statement);
    }

    public void removeStatement(URI space, String subjectURIString, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException {
        this.dataStore.removeStatement(space, subjectURIString, predicate, literal, languageTag);
    }

    public void removeStatement(URI space, String subjectURIString, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException {
        this.dataStore.removeStatement(space, subjectURIString, predicate, literal, datatypeURI);
    }

    public void removeStatement(URI space, String subjectURIString, URI predicate, String literal)
            throws SemanticSpaceException {
        this.dataStore.removeStatement(space, subjectURIString, predicate, literal);
    }

    public void removeStatements(URI space, ResourceOrVariable subject, UriOrVariable predicate,
            NodeOrVariable object) throws SemanticSpaceException {
        this.dataStore.removeStatements(space, subject, predicate, object);
    }

    public void removeStatements(URI space, TriplePattern triplePattern)
            throws SemanticSpaceException {
        this.dataStore.removeStatements(space, triplePattern);
    }

    public boolean sparqlAsk(URI space, String query) throws SemanticSpaceException {
        return this.dataStore.sparqlAsk(space, query);
    }

    public ClosableIterable<Statement> sparqlConstruct(URI space, String query)
            throws SemanticSpaceException {
        return this.dataStore.sparqlConstruct(space, query);
    }
    
    public ClosableIterable<Statement> sparqlDescribe(URI space, String query)
            throws SemanticSpaceException {
        return this.dataStore.sparqlDescribe(space, query);
    }

    public QueryResultTable sparqlSelect(URI space, String queryString)
            throws SemanticSpaceException {
        return this.dataStore.sparqlSelect(space, queryString);
    }

}
