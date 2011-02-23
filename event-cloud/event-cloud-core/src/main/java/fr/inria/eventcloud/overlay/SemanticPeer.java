package fr.inria.eventcloud.overlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.TriplePattern;
import org.ontoware.rdf2go.model.node.NodeOrVariable;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.ResourceOrVariable;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.UriOrVariable;

import at.sti2.semanticspaces.api.ISemanticSpace;
import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.api.SemanticFactory;
import fr.inria.eventcloud.api.SemanticSpaceOperations;

/**
 * A SemanticPeer is an extension of {@link Peer} which implements
 * {@link ISemanticSpace} interface in order to be RDF2Go compatible and to
 * offer the possibility to query SemanticSpaces.
 * 
 * Warning, you may use {@link SemanticFactory} in order to create a new
 * instance of SemanticPeer.
 * 
 * @author lpellegr
 */
public class SemanticPeer extends Peer implements SemanticSpaceOperations {

    private static final long serialVersionUID = 1L;

    private List<Tracker> remoteTrackers = new ArrayList<Tracker>();
    
    public SemanticPeer() {
        super();
    }

    public SemanticPeer(Tracker remoteTracker, SemanticStructuredOverlay overlay) {
        super((StructuredOverlay) overlay);
        this.remoteTrackers.add(remoteTracker);
    }
    
    public SemanticPeer(List<Tracker> remoteTrackers, SemanticStructuredOverlay overlay) {
        super((StructuredOverlay)overlay);
        for (Tracker remoteTracker: remoteTrackers) {
            this.remoteTrackers.add(remoteTracker);
        }
    }
    
    public List<Tracker> getTrackers() {
        return this.remoteTrackers;
    }
    
    /*
     * SemanticSpaceOperations implementation
     */
    
    public void addAll(URI space, Iterator<? extends Statement> other)
            throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).addAll(space, other);
    }

    public void addStatement(URI space, Statement statement) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).addStatement(space, statement);
    }

    public void addStatement(URI space, Resource subject, URI predicate, String literal)
            throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).addStatement(space, subject, predicate, literal);
    }

    public void addStatement(URI space, Resource subject, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).addStatement(space, subject, predicate, literal);
    }

    public void addStatement(URI space, Resource subject, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).addStatement(space, subject, predicate, literal,
                datatypeURI);
    }

    public void addStatement(URI space, String subjectURIString, URI predicate, String literal)
            throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).addStatement(space, subjectURIString, predicate,
                literal);
    }

    public void addStatement(URI space, String subjectURIString, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).addStatement(space, subjectURIString, predicate,
                literal, languageTag);
    }

    public void addStatement(URI space, String subjectURIString, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).addStatement(space, subjectURIString, predicate,
                literal, datatypeURI);
    }

    public boolean contains(URI space, Statement s) throws SemanticSpaceException {
        return ((SemanticSpaceOperations) super.overlay).contains(space, s);
    }

    public boolean contains(URI space, ResourceOrVariable subject, UriOrVariable predicate,
            NodeOrVariable object) throws SemanticSpaceException {
        return ((SemanticSpaceOperations) super.overlay).contains(space, subject, predicate,
                object);
    }

    public boolean contains(URI space, ResourceOrVariable subject, UriOrVariable predicate,
            String plainLiteral) throws SemanticSpaceException {
        return ((SemanticSpaceOperations) super.overlay).contains(space, subject, predicate,
                plainLiteral);
    }

    public ClosableIterator<Statement> findStatements(URI space, TriplePattern triplepattern)
            throws SemanticSpaceException {
        return ((SemanticSpaceOperations) super.overlay).findStatements(space, triplepattern);
    }

    public Set<URI> listSpaces() {
        return ((SemanticSpaceOperations) super.overlay).listSpaces();
    }

    public void removeAll(URI space) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeAll(space);
    }

    public void removeAll(URI space, Iterator<? extends Statement> statements)
            throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeAll(space, statements);
    }

    public void removeStatement(URI space, Statement statement) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeStatement(space, statement);
    }

    public void removeStatement(URI space, Resource subject, URI predicate, String literal)
            throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeStatement(space, subject, predicate,
                literal);
    }

    public void removeStatement(URI space, Resource subject, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeStatement(space, subject, predicate,
                literal);
    }

    public void removeStatement(URI space, Resource subject, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeStatement(space, subject, predicate,
                literal, datatypeURI);
    }

    public void removeStatement(URI space, String subjectURIString, URI predicate, String literal)
            throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeStatement(space, subjectURIString,
                predicate, literal);
    }

    public void removeStatement(URI space, String subjectURIString, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeStatement(space, subjectURIString,
                predicate, literal, languageTag);
    }

    public void removeStatement(URI space, String subjectURIString, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeStatement(space, subjectURIString,
                predicate, literal, datatypeURI);
    }

    public void removeStatements(URI space, ResourceOrVariable subject, UriOrVariable predicate,
            NodeOrVariable object) throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeStatements(space, subject, predicate,
                object);
    }

    public void removeStatements(URI space, TriplePattern triplePattern)
            throws SemanticSpaceException {
        ((SemanticSpaceOperations) super.overlay).removeStatements(space, triplePattern);
    }

    public boolean sparqlAsk(URI space, String query) throws SemanticSpaceException {
        return ((SemanticSpaceOperations) super.overlay).sparqlAsk(space, query);
    }

    public ClosableIterable<Statement> sparqlConstruct(URI space, String query)
            throws SemanticSpaceException {
        return ((SemanticSpaceOperations) super.overlay).sparqlConstruct(space, query);
    }

    public ClosableIterable<Statement> sparqlDescribe(URI space, String query)
	    throws SemanticSpaceException {
    	return ((SemanticSpaceOperations) super.overlay).sparqlDescribe(space, query);
    }
    
    public QueryResultTable sparqlSelect(URI space, String queryString)
            throws SemanticSpaceException {
        return ((SemanticSpaceOperations) super.overlay).sparqlSelect(space, queryString);
    }
    
    /*
     * Overrides some methods from super class for
     * ProActive stubs generation. 
     */
    
    @Override
    public boolean equals(Object obj) {
    	return super.equals(obj);
    }

    @Override
    public UUID getId() {
    	return super.getId();
    }
    
    @Override
    public OverlayType getType() {
    	return super.getType();
    }
    
    @Override
    public int hashCode() {
		return super.hashCode();
	}
    
    @Override
	public String toString() {
		return super.toString();
	}
    
}
