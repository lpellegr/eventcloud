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
 * The <code>ClosableIterableWrapper</code> wraps a value of the RDF2Go type
 * {@link ClosableIterable}.
 * 
 * @author lpellegr
 */
public class ClosableIterableWrapper implements
        RDF2GoWrapper<ClosableIterable<Statement>> {

    private static final long serialVersionUID = 1L;

    private static final transient Logger logger =
            LoggerFactory.getLogger(ClosableIterableWrapper.class);

    private final Set<Statement> data = new HashSet<Statement>();

    public ClosableIterableWrapper() {

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

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "ClosableIterableWrapper initialized with {} data.",
                    this.data.size());
        }
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
