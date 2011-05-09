/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.reasoner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import fr.inria.eventcloud.util.SemanticHelper;

/**
 * An atomic sparql is a representation of a sparql query which contains no
 * conjunction or disjunction but only a triple pattern with some modifiers to
 * apply on it.
 * 
 * @author lpellegr
 * 
 * @see SparqlDecomposer
 */
public class AtomicSparqlQuery {

    private final UUID id;

    // contains respectively the subject, the predicate and the object
    private final String[] tripleElements;

    // indicates if the object is a literal value
    private boolean literalObject = false;

    // contains the variables and its associated positions
    private Map<String, Integer> vars;

    // a string representation of this atomic sparql as
    // a sparql construct query
    private transient String sparqlConstruct;

    private List<FilterConstraint> filterConstraints;

    public AtomicSparqlQuery(String subject, String predicate, String object) {
        this(new String[] {subject, predicate, object});
    }

    public AtomicSparqlQuery(String[] tripleElements) {
        this.id = UUID.randomUUID();

        if (tripleElements.length != 3) {
            throw new IllegalArgumentException(
                    "An atomic sparql must have three elements!");
        }

        for (String tripleElt : tripleElements) {
            if (tripleElt == null) {
                throw new IllegalArgumentException(
                        "An atomic sparql element can not be null!");
            }
        }

        this.tripleElements = tripleElements;

        for (int i = 0; i < this.tripleElements.length; i++) {
            if (isVariable(this.tripleElements[i])) {
                if (this.vars == null) {
                    this.vars = new HashMap<String, Integer>(3);
                }

                this.vars.put(this.tripleElements[i], i);
            }
        }

        if (this.tripleElements[2].startsWith("\"")) {
            this.literalObject = true;
        }
    }

    public boolean hasLiteralObject() {
        return this.literalObject;
    }

    public String getVariable(int index) {
        for (Entry<String, Integer> entry : this.vars.entrySet()) {
            if (entry.getValue().equals(index)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public int hasVariable(String elt) {
        Integer result = this.vars.get(elt);
        if (result == null) {
            return -1;
        } else {
            return result;
        }
    }

    public boolean containsFilterConstraints() {
        return this.filterConstraints.size() > 0;
    }

    public UUID getId() {
        return id;
    }

    public String getSubject() {
        return this.tripleElements[0];
    }

    public String getSubjectWithNullVariable() {
        return isVariable(this.tripleElements[0])
                ? null : this.tripleElements[0];
    }

    public String getPredicate() {
        return this.tripleElements[1];
    }

    public String getPredicateWithNullVariable() {
        return isVariable(this.tripleElements[1])
                ? null : this.tripleElements[1];
    }

    public String getObject() {
        return this.tripleElements[2];
    }

    public String getObjectWithNullVariable() {
        return isVariable(this.tripleElements[2])
                ? null : this.tripleElements[2];
    }

    public Set<String> getVariables() {
        return this.vars.keySet();
    }

    /**
     * Returns the triple pattern as an array of String where each component is
     * either a String if the triple pattern component depicts a value or
     * {@code null} if the triple pattern component depicts a variable.
     * 
     * @return an array of String where each component is either a String if the
     *         triple pattern component depicts a value or {@code null} if the
     *         triple pattern component depicts a variable.
     */
    public String[] toArray() {
        return new String[] {
                isVariable(this.tripleElements[0])
                        ? null : this.tripleElements[0],
                isVariable(this.tripleElements[1])
                        ? null : this.tripleElements[1],
                isVariable(this.tripleElements[2])
                        ? null : this.tripleElements[2]};
    }

    public int getNumberOfVariables() {
        return this.vars.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.tripleElements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AtomicSparqlQuery) {
            AtomicSparqlQuery triple = ((AtomicSparqlQuery) obj);
            return this.tripleElements[0].equals(triple.getSubject())
                    && this.tripleElements[1].equals(triple.getPredicate())
                    && this.tripleElements[2].equals(triple.getObject());
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.getSubject());
        result.append(" ");
        result.append(this.getPredicate());
        result.append(" ");
        result.append(this.getObject());
        return result.toString();
    }

    public String toConstruct() {
        if (this.sparqlConstruct == null) {
            StringBuilder query = new StringBuilder();
            query.append("CONSTRUCT { ");
            for (String elt : this.tripleElements) {
                query.append(SemanticHelper.toNTripleSyntax(elt));
                query.append(" ");
            }
            query.append("} WHERE { ");
            for (String elt : this.tripleElements) {
                query.append(SemanticHelper.toNTripleSyntax(elt));
                query.append(" ");
            }
            query.append("}");
            this.sparqlConstruct = query.toString();
        }

        return this.sparqlConstruct;
    }

    public static final boolean isVariable(String elt) {
        return elt.startsWith("?");
    }

    public static class FilterConstraint {

    }

}
