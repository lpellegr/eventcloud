package fr.inria.eventcloud.api;

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
 * Defines operations that may be supported in order to be RDF2Go compatible.
 * 
 * @author lpellegr
 */
public interface SemanticSpaceOperations {

	// FIXME operations not yet supported, add it when necessary
	
	// public URI createFederation(Set<URI> spaces) throws
	// SemanticSpaceException;
	//
	// public void deleteFederation(URI fed) throws SemanticSpaceException;
	//
	// public void createSpaceRelation(URI sSpaces, URI relation, URI oSpace)
	// throws SemanticSpaceException;
	//
	// public URI subscribe(URI space, TriplePattern pattern) throws
	// SemanticSpaceException;
	//
	// public void unsubscribe(URI subscription) throws SemanticSpaceException;
	
    public Set<URI> listSpaces();

    public void addAll(URI space, Iterator<? extends Statement> other)
	    throws SemanticSpaceException;

    public void addStatement(URI space, Statement statement)
	    throws SemanticSpaceException;

    public void addStatement(URI space, Resource subject, URI predicate,
	    String literal) throws SemanticSpaceException;

    public void addStatement(URI space, Resource subject, URI predicate,
	    String literal, String languageTag) throws SemanticSpaceException;

    public void addStatement(URI space, Resource subject, URI predicate,
	    String literal, URI datatypeURI) throws SemanticSpaceException;

    public void addStatement(URI space, String subjectURIString, URI predicate,
	    String literal) throws SemanticSpaceException;

    public void addStatement(URI space, String subjectURIString, URI predicate,
	    String literal, String languageTag) throws SemanticSpaceException;

    public void addStatement(URI space, String subjectURIString, URI predicate,
	    String literal, URI datatypeURI) throws SemanticSpaceException;

    public ClosableIterable<Statement> sparqlConstruct(URI space, String query)
	    throws SemanticSpaceException;

    public QueryResultTable sparqlSelect(URI space, String queryString)
	    throws SemanticSpaceException;

    public boolean sparqlAsk(URI space, String query)
	    throws SemanticSpaceException;

    public ClosableIterator<Statement> findStatements(URI space,
	    TriplePattern triplepattern) throws SemanticSpaceException;

    public void removeAll(URI space) throws SemanticSpaceException;

    public void removeAll(URI space, Iterator<? extends Statement> statements)
	    throws SemanticSpaceException;

    public void removeStatement(URI space, Statement statement)
	    throws SemanticSpaceException;

    public void removeStatement(URI space, Resource subject, URI predicate,
	    String literal) throws SemanticSpaceException;

    public void removeStatement(URI space, Resource subject, URI predicate,
	    String literal, String languageTag) throws SemanticSpaceException;

    public void removeStatement(URI space, Resource subject, URI predicate,
	    String literal, URI datatypeURI) throws SemanticSpaceException;

    public void removeStatement(URI space, String subjectURIString,
	    URI predicate, String literal) throws SemanticSpaceException;

    public void removeStatement(URI space, String subjectURIString,
	    URI predicate, String literal, String languageTag)
	    throws SemanticSpaceException;

    public void removeStatement(URI space, String subjectURIString,
	    URI predicate, String literal, URI datatypeURI)
	    throws SemanticSpaceException;

    public void removeStatements(URI space, ResourceOrVariable subject,
	    UriOrVariable predicate, NodeOrVariable object)
	    throws SemanticSpaceException;

    public void removeStatements(URI space, TriplePattern triplePattern)
	    throws SemanticSpaceException;

    public boolean contains(URI space, Statement s)
	    throws SemanticSpaceException;

    public boolean contains(URI space, ResourceOrVariable subject,
	    UriOrVariable predicate, NodeOrVariable object)
	    throws SemanticSpaceException;

    public boolean contains(URI space, ResourceOrVariable subject,
	    UriOrVariable predicate, String plainLiteral)
	    throws SemanticSpaceException;

    public ClosableIterable<Statement> sparqlDescribe(URI space, String query)
	    throws SemanticSpaceException;

}
