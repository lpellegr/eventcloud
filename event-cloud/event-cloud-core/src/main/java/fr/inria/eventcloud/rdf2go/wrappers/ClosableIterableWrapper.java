package fr.inria.eventcloud.rdf2go.wrappers;

import java.util.HashSet;
import java.util.Set;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.impl.StatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.util.SemanticHelper;

/**
 * The {@code ClosableIterableWrapper} wraps a RDF2Go type value.
 * {@link ClosableIterable}.
 * 
 * @author lpellegr
 */
public class ClosableIterableWrapper implements
        RDF2GoWrapper<ClosableIterable<Statement>> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger =
            LoggerFactory.getLogger(ClosableIterableWrapper.class);

    private final Set<Statement> data = new HashSet<Statement>();

    public ClosableIterableWrapper() {

    }

    public ClosableIterableWrapper(Set<Statement> statements) {
        for (Statement stmt : statements) {
            this.data.add(new StatementImpl(
                    stmt.getContext(), stmt.getSubject(), stmt.getPredicate(),
                    stmt.getObject()));
        }

        logger.debug(
                "ClosableIterableWrapper initialized with {} data.",
                this.data.size());
    }

    public ClosableIterableWrapper(ClosableIterable<Statement> closableIterable) {
        ClosableIterator<Statement> it = closableIterable.iterator();
        Statement currentStmt;
        while (it.hasNext()) {
            currentStmt = it.next();
            this.data.add(new StatementImpl(
                    currentStmt.getContext(), currentStmt.getSubject(),
                    currentStmt.getPredicate(), currentStmt.getObject()));
        }

        logger.debug(
                "ClosableIterableWrapper initialized with {} data.",
                this.data.size());
    }

    public void addAll(Set<Statement> set) {
        this.data.addAll(set);
    }

    public Set<Statement> getData() {
        return this.data;
    }

    public ClosableIterable<Statement> toRDF2Go() {
        return SemanticHelper.generateClosableIterable(new HashSet<Statement>(
                this.data));
    }

}
