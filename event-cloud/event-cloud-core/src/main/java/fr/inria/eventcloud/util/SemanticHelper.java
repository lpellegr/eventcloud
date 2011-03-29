package fr.inria.eventcloud.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.TriplePattern;
import org.ontoware.rdf2go.model.impl.TriplePatternImpl;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import fr.inria.eventcloud.datastore.SemanticDatastoreOperations;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * @author lpellegr
 */
public class SemanticHelper {

    /**
     * Creates a construct SPARQL query as String from a
     * {@link SemanticDatastoreOperations#findStatements(URI, TriplePattern)}
     * identified by an {@link URIImpl} and a {@link TriplePattern}.
     * 
     * @param spaceURI
     *            the spaceURI to use.
     * @param triplePattern
     *            the tripplePatternToUse.
     * @return a construct SPARQL query as String.
     */
    public static String createConstructSparqlFrom(URI spaceURI, TriplePattern triplePattern) {
        StringBuffer buf = new StringBuffer("CONSTRUCT { ");

        if (triplePattern.getSubject() == null) {
            buf.append("?s");
        } else {
            buf.append("<");
            buf.append(triplePattern.getSubject());
            buf.append(">");
        }

        buf.append(" ");

        if (triplePattern.getPredicate() == null) {
            buf.append("?p");
        } else {
            buf.append("<");
            buf.append(triplePattern.getPredicate());
            buf.append(">");
        }

        buf.append(" ");

        if (triplePattern.getObject() == null) {
            buf.append("?o");
        } else {
            buf.append("<");
            buf.append(triplePattern.getObject());
            buf.append(">");
        }

        buf.append("} WHERE { GRAPH <");
        buf.append(spaceURI);
        buf.append("> { ");

        if (triplePattern.getSubject() == null) {
            buf.append("?s");
        } else {
            buf.append("<");
            buf.append(triplePattern.getSubject());
            buf.append(">");
        }

        buf.append(" ");

        if (triplePattern.getPredicate() == null) {
            buf.append("?p");
        } else {
            buf.append("<");
            buf.append(triplePattern.getPredicate());
            buf.append(">");
        }

        buf.append(" ");

        if (triplePattern.getObject() == null) {
            buf.append("?o");
        } else {
            buf.append("<");
            buf.append(triplePattern.getObject());
            buf.append(">");
        }

        buf.append(" } . }");

        return buf.toString();
    }

    public static String createConstructSparqlFrom(URI spaceURI, String subject, String predicate,
            String object) {
        return SemanticHelper.createConstructSparqlFrom(spaceURI, SemanticHelper
                .constructTriplePatternFrom(spaceURI, subject, predicate, object));
    }

//    /**
//     * Creates a new {@link Statement} from a set of String triples.
//     * 
//     * @param context
//     *            the context associated to the statement to generate.
//     * @param subject
//     *            the subject to use.
//     * @param predicate
//     *            the predicate to use.
//     * @param object
//     *            the object to use.
//     * @return a new {@link Statement}.
//     */
//    public static Statement constructStatementFrom(URI context, String subject, String predicate, String object) {
//        return new StatementImpl(context, subject == null || subject.startsWith("?") ? null
//                : RDF2GoBuilder.createURI(subject), predicate == null || predicate.startsWith("?") ? null
//                : RDF2GoBuilder.createURI(predicate), object == null || object.startsWith("?") ? null
//                : RDF2GoBuilder.createURI(object));
//    }

    /**
     * Creates a new {@link TriplePattern} from a set of String triples.
     * 
     * @param context
     *            the context associated to the statement to generate.
     * @param subject
     *            the subject to use.
     * @param predicate
     *            the predicate to use.
     * @param object
     *            the object to use.
     * @return a new {@link TriplePattern}.
     */
    public static TriplePattern constructTriplePatternFrom(URI context, String subject,
            String predicate, String object) {
        return new TriplePatternImpl(subject == null || subject.startsWith("?") ? null : RDF2GoBuilder
                .createURI(subject), predicate == null || predicate.startsWith("?") ? null : RDF2GoBuilder
                .createURI(predicate), object == null || object.startsWith("?") ? null : RDF2GoBuilder
                .createURI(object));
    }

    /**
     * Helper method to combine query results for construct queries.
     * 
     * @param statements
     *            statements to be iterable.
     * @return combined results.
     */
    @SuppressWarnings("serial")
    public static <T> ClosableIterable<T> generateClosableIterable(final Set<T> statements) {
        return new ClosableIterable<T>() {
            public ClosableIterator<T> iterator() {
                return (ClosableIterator<T>) generateClosableIterator(statements);
            }
        };
    }

    /**
     * Helper method to construct result iterator.
     * 
     * @param rows
     *            rows to set.
     * @return result iterator.
     */
    public static <T> ClosableIterator<T> generateClosableIterator(final Set<T> rows) {
        return new ClosableIterator<T>() {
            private Iterator<T> iterator = rows.iterator();

            public void close() {
                // cannot really close
            }

            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            public T next() {
                return this.iterator.next();
            }

            public void remove() {
                this.iterator.remove();
            }
        };
    }

    /**
     * Helper method to construct result iterator.
     * 
     * @param rows
     *            rows to set.
     * @return result iterator.
     */
    public static <T> ClosableIterator<T> generateClosableIterator(final List<T> rows) {
        return new ClosableIterator<T>() {
            private Iterator<T> iterator = rows.iterator();

            public void close() {
                // cannot really close
            }

            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            public T next() {
                return this.iterator.next();
            }

            public void remove() {
                this.iterator.remove();
            }
        };
    }

    /**
     * Helper method to combine query results for select queries.
     * 
     * @param variables
     *            variables to set.
     * @param queryRows
     *            rows to set.
     * @return combined results.
     */
    public static QueryResultTable generateQueryResultTable(final List<String> variables,
            final Set<QueryRow> queryRows) {
        return new QueryResultTable() {

            private static final long serialVersionUID = 1L;

            public List<String> getVariables() {
                return variables;
            }

            public ClosableIterator<QueryRow> iterator() {
                return (ClosableIterator<QueryRow>) generateClosableIterator(queryRows);
            }

        };
    }

    /**
     * Helper method to combine query results for select queries.
     * 
     * @param variables
     *            variables to set.
     * @param queryRows
     *            rows to set.
     * @return combined results.
     */
    public static QueryResultTable generateQueryResultTable(final List<String> variables,
            final List<QueryRow> queryRows) {
        return new QueryResultTable() {

            private static final long serialVersionUID = 1L;

            public List<String> getVariables() {
                return variables;
            }

            public ClosableIterator<QueryRow> iterator() {
                return (ClosableIterator<QueryRow>) generateClosableIterator(queryRows);
            }
        };
    }

	/**
	 * Parses a triple element to remove prefixes and some characters specific
	 * to the RDF syntax used. This suppression is done to improve the load
	 * balancing (i.e. especially to avoid to have some values with popular
	 * prefixes managed by the same zone).
	 */
	public static String parseTripleElement(String value) {
		try {
			java.net.URI uri = new java.net.URI(value);
			
			int slashIndex = value.lastIndexOf("/");
			int sharpIndex = value.lastIndexOf("#");

			// # or / is the last character
			if (slashIndex == value.length() - 1
					|| sharpIndex == value.length() - 1) {
				value = value.substring(0, value.length() - 1);
				slashIndex = value.lastIndexOf("/");
				sharpIndex = value.lastIndexOf("#");
			}

			if (slashIndex < 7 && sharpIndex == -1) {
				if (uri.getScheme().equals("http")
						&& value.startsWith("http://www.")) {
					return value.substring(11);
				} 
				return value.substring(uri.getScheme().length() + 3);
			} else if (slashIndex > sharpIndex) {
				return value.substring(slashIndex + 1, value.length());
			} else if (slashIndex < sharpIndex) {
				return value.substring(sharpIndex + 1, value.length());
			} 
			
			return "";
		} catch (URISyntaxException e) {
			if (value.startsWith("_:")) {
				return value.substring(2);
			} else if (value.startsWith("\"")) {
				return value.substring(1, value.length() - 1);
			} else {
				return value;
			}
		}
	}
	
	public static Coordinate createCoordinateWithNullValues(String subject, String predicate, String object) {
		return new Coordinate(
        			 	subject == null ? null : new SemanticElement(subject),
        			 	predicate == null ? null : new SemanticElement(predicate),
        			 	object == null ? null : new SemanticElement(object));
	}
	
	public static Coordinate createCoordinateWithNullValues(Statement stmt) {
		checkNotNull(stmt);
    	
    	return new Coordinate(
    					stmt.getSubject() == null ? null : new SemanticElement(stmt.getSubject()),
    					stmt.getPredicate() == null ? null : new SemanticElement(stmt.getPredicate()),
    					stmt.getObject() == null ? null : new SemanticElement(stmt.getObject()));
	}
	
    public static Coordinate createCoordinateWithoutNullValues(Statement stmt) {
    	checkNotNull(stmt);
    	
    	return new Coordinate(
    					new SemanticElement(checkNotNull(stmt.getSubject())),
    					new SemanticElement(checkNotNull(stmt.getPredicate())),
    					new SemanticElement(checkNotNull(stmt.getObject())));
    }

    public static String beautifyStatements(ClosableIterable<Statement> statements) {
        ClosableIterator<Statement> it = statements.iterator();
        Statement stmt = null;
        if (!it.hasNext()) {
            return null;
        }

        StringBuffer buf = new StringBuffer();
        while (it.hasNext()) {
            stmt = it.next();
            buf.append("  - <" + stmt.getSubject() + "," + stmt.getPredicate() + ","
                    + stmt.getObject() + ">\n");
        }
        return buf.toString();
    }

    public static String beautifyStatements(QueryResultTable table) {
        ClosableIterator<QueryRow> it = table.iterator();
        if (!it.hasNext()) {
            return null;
        }

        QueryRow row = null;
        StringBuffer buf = new StringBuffer();
        while (it.hasNext()) {
            row = it.next();
            buf.append("  - [");

            List<String> vars = table.getVariables();
            for (int i = 0; i < vars.size(); i++) {
                buf.append(row.getValue(vars.get(i)));
                if (i < vars.size() - 1) {
                    buf.append(", ");
                }
            }

            buf.append("]\n");
        }
        return buf.substring(0, buf.length() - 1);
    }

    public static Statement generateRandomStatement() {
        return RDF2GoBuilder.createStatement(
        						generateRandomURI(10), 
        						generateRandomURI(10),
        						generateRandomURI(10));
    }

    public static URI generateRandomURI(int length) {
        return RDF2GoBuilder.createURI(
                StringUtil.generateRandomString(
                        "http://", 
                        1 + ProActiveRandom.nextInt(length), 
                        new char[][] { { '0', '9' }, { 'A', 'Z' },
                        { 'a', 'z' } }));
    }

    
    public static Set<Statement> asSet(ClosableIterable<Statement> it)  {
        Set<Statement> result = new HashSet<Statement>();
        ClosableIterator<Statement> iterator = it.iterator();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }
    
	public static String toNTripleSyntax(String tripleElt) {
		if (!tripleElt.startsWith("?") && !tripleElt.startsWith("\"")) {
			StringBuilder triple = new StringBuilder("<");
			triple.append(tripleElt);
			triple.append(">");
			return triple.toString();
		} else {
			return tripleElt;
		}
	}

    
    public static String toString(ClosableIterable<Statement> collection) {
    	ClosableIterator<Statement> it = collection.iterator();
    	StringBuffer buf = new StringBuffer();
    	
    	while (it.hasNext()) {
    		buf.append(it.next());
    	}
    	
    	return buf.toString();
    }
    
    public static String toString(QueryResultTable table) {
    	StringBuilder buf = new StringBuilder();
    	ClosableIterator<QueryRow> it = table.iterator();
    	
    	if (!it.hasNext()) {
    		buf.append("no result");
    		return buf.toString();
    	}
    	
		for (int i=0; i<table.getVariables().size(); i++) {
			buf.append("?");
			buf.append(table.getVariables().get(i));
			if (i < table.getVariables().size()) {
				buf.append("\t");
			}
		}
		buf.append("\n");
		
		int index = 0;
		QueryRow row;

		while (it.hasNext()) {
    		index = 0;
    		row = it.next();
			for (String var : table.getVariables()) {
				buf.append(row.getValue(var));
				if (index < table.getVariables().size()) {
					buf.append("\t\t");
				}
				index++;
			}
			
			buf.append("\n");
		}
		
    	return buf.toString();
    }
    
    public static long size(ClosableIterator<Statement> it) {
        long count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        it.close();
        return count;
    }
    
    public static long size(ClosableIterable<Statement> iterable) {
        return size(iterable.iterator());
    }
    
}
