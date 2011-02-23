package fr.inria.eventcloud.tracker;

import java.util.Iterator;
import java.util.Set;

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

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;

/**
 * Provides operations that can be performed from a <code>SemanticTracker</code>
 * . These operations are the same that {@link SemanticSpaceOperations} except
 * that it doesn't provide join space, leave space and list joined spaces.
 * 
 * @author lpellegr
 */
public interface SemanticTracker {

    // ***** JOIN LEAVE LIST SPACES *****//
    // public void joinSpace(URI space) throws SemanticSpaceException;

    // public void leaveSpace(URI space) throws SemanticSpaceException;

    public Set<URI> listSpaces();

    // public Set<URI> listJoinedSpaces();

    // ***** SPACE RELATIONS *****//
    public URI createFederation(Set<URI> spaces) throws SemanticSpaceException;

    public void deleteFederation(URI fed) throws SemanticSpaceException;

    public void createSpaceRelation(URI sSpaces, URI relation, URI oSpace)
            throws SemanticSpaceException;

    // ***** REGISTER ENDPOINT *****//
    public void registerEndPoint(URI endpoint) throws SemanticSpaceException;

    // ***** RDF MANIPULATION METHODS (RDF2GO COMPATIBLE) *****//
    public void addAll(URI space, Iterator<? extends Statement> other)
            throws SemanticSpaceException;

    public void addStatement(URI space, Statement statement) throws SemanticSpaceException;

    public void addStatement(URI space, Resource subject, URI predicate, String literal)
            throws SemanticSpaceException;

    public void addStatement(URI space, Resource subject, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException;

    public void addStatement(URI space, Resource subject, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException;

    public void addStatement(URI space, String subjectURIString, URI predicate, String literal)
            throws SemanticSpaceException;

    public void addStatement(URI space, String subjectURIString, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException;

    public void addStatement(URI space, String subjectURIString, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException;

    public ClosableIterable<Statement> sparqlConstruct(URI space, String query)
            throws SemanticSpaceException;

    public QueryResultTable sparqlSelect(URI space, String queryString)
            throws SemanticSpaceException;

    public boolean sparqlAsk(URI space, String query) throws SemanticSpaceException;

    public ClosableIterator<Statement> findStatements(URI space, TriplePattern triplepattern)
            throws SemanticSpaceException;

    // public Diff getDiff(URI space, Iterator<? extends Statement> other)
    // throws SemanticSpaceException;

    public void removeAll(URI space) throws SemanticSpaceException;

    public void removeAll(URI space, Iterator<? extends Statement> statements)
            throws SemanticSpaceException;

    public void removeStatement(URI space, Statement statement) throws SemanticSpaceException;

    public void removeStatement(URI space, Resource subject, URI predicate, String literal)
            throws SemanticSpaceException;

    public void removeStatement(URI space, Resource subject, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException;

    public void removeStatement(URI space, Resource subject, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException;

    public void removeStatement(URI space, String subjectURIString, URI predicate, String literal)
            throws SemanticSpaceException;

    public void removeStatement(URI space, String subjectURIString, URI predicate, String literal,
            String languageTag) throws SemanticSpaceException;

    public void removeStatement(URI space, String subjectURIString, URI predicate, String literal,
            URI datatypeURI) throws SemanticSpaceException;

    public void removeStatements(URI space, ResourceOrVariable subject, UriOrVariable predicate,
            NodeOrVariable object) throws SemanticSpaceException;

    public void removeStatements(URI space, TriplePattern triplePattern)
            throws SemanticSpaceException;

    public boolean contains(URI space, Statement s) throws SemanticSpaceException;

    public boolean contains(URI space, ResourceOrVariable subject, UriOrVariable predicate,
            NodeOrVariable object) throws SemanticSpaceException;

    public boolean contains(URI space, ResourceOrVariable subject, UriOrVariable predicate,
            String plainLiteral) throws SemanticSpaceException;

    // public void readFrom(URI space, Reader in) throws SemanticSpaceException;
    // public void readFrom(URI space, InputStream in, Syntax syntax) throws
    // SemanticSpaceException;
    // public void readFrom(URI space, InputStream in, Syntax syntax, String
    // baseURI) throws SemanticSpaceException;
    // public void readFrom(URI space, Reader reader, Syntax syntax, String
    // baseURI) throws SemanticSpaceException;
    // public void writeTo(URI space, OutputStream out, Syntax syntax) throws
    // SemanticSpaceException;

    // public long countStatements(URI space, TriplePattern pattern) throws
    // SemanticSpaceException;
    // public ClosableIterable<Statement> queryConstruct(URI space, String
    // query, String querylanguage) throws SemanticSpaceException;
    // public ClosableIterable<Statement> querySelect(URI space, String query,
    // String querylanguage) throws SemanticSpaceException;
    // ClosableIterable<Statement> sparqlDescribe(URI space, String query)
    // throws SemanticSpaceException;
    // public void update(URI space, DiffReader diff) throws
    // SemanticSpaceException;
    // BlankNode addReificationOf(URI space, Statement statement) throws
    // SemanticSpaceException;
    // Resource addReificationOf(URI space, Statement statement, Resource
    // resource) throws SemanticSpaceException;
    // boolean hasReifications(URI space, Statement statement) throws
    // SemanticSpaceException;
    // Collection<Resource> getAllReificationsOf(URI space, Statement statement)
    // throws SemanticSpaceException;
    // public void deleteReification(URI space, Resource reificationResource)
    // throws SemanticSpaceException;

}
