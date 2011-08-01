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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.reasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;

import fr.inria.eventcloud.api.QuadruplePattern;

/**
 * An atomic query is a {@link QuadruplePattern} that may have some constraints
 * associated to the variables.
 * 
 * @author lpellegr
 * 
 * @see SparqlDecomposer
 */
public final class AtomicQuery {

    // private final UUID id;

    public enum ParentQueryForm {
        ASK, CONSTRUCT, DESCRIBE, SELECT
    };

    private final ParentQueryForm parentQueryForm;

    private final QuadruplePattern quadruplePattern;

    // contains the variables and its associated positions
    private Map<String, Integer> vars;

    public AtomicQuery(ParentQueryForm form, Node graph, Node subject,
            Node predicate, Node object) {
        // this.id = UUID.randomUUID();
        this.parentQueryForm = form;
        this.quadruplePattern =
                new QuadruplePattern(graph, subject, predicate, object);
        this.vars = new HashMap<String, Integer>(4);

        Node[] nodes = {graph, subject, predicate, object};
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].isVariable()) {
                this.vars.put(nodes[i].getName(), i);
            }
        }
    }

    public boolean hasLiteralObject() {
        return this.quadruplePattern.getObject() != null
                && this.quadruplePattern.getObject().isLiteral();
    }

    public String getVarName(int index) {
        for (Entry<String, Integer> entry : this.vars.entrySet()) {
            if (entry.getValue().equals(index)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public int getVarIndex(String varName) {
        Integer result = vars.get(varName);
        if (result == null) {
            return -1;
        }

        return result;
    }

    public boolean hasVariable(String varName) {
        return getVarIndex(varName) != -1;
    }

    // public UUID getId() {
    // return this.id;
    // }

    public ParentQueryForm getParentQueryForm() {
        return this.parentQueryForm;
    }

    public QuadruplePattern getQuadruplePattern() {
        return this.quadruplePattern;
    }

    public Node getGraph() {
        return this.quadruplePattern.getGraph();
    }

    public Node getSubject() {
        return this.quadruplePattern.getSubject();
    }

    public Node getPredicate() {
        return this.quadruplePattern.getPredicate();
    }

    public Node getObject() {
        return this.quadruplePattern.getObject();
    }

    public Node[] toArray() {
        return this.quadruplePattern.toArray();
    }

    public Set<Var> getVariables() {
        Set<Var> vars = new HashSet<Var>();
        for (String varName : this.vars.keySet()) {
            vars.add(Var.alloc(varName));
        }
        return vars;
    }

    // /**
    // * Returns the triple pattern as an array of String where each component
    // is
    // * either a String if the triple pattern component depicts a value or
    // * {@code null} if the triple pattern component depicts a variable.
    // *
    // * @return an array of String where each component is either a String if
    // the
    // * triple pattern component depicts a value or {@code null} if the
    // * triple pattern component depicts a variable.
    // */
    // public String[] toArray() {
    // return new String[] {isVariable(this.elements[0])
    // ? null : this.elements[0], isVariable(this.elements[1])
    // ? null : this.elements[1], isVariable(this.elements[2])
    // ? null : this.elements[2], isVariable(this.elements[3])
    // ? null : this.elements[3]};
    // }

    // public String asSparql() {
    // if (this.sparql == null) {
    // switch (this.parentQueryForm) {
    // case ASK:
    // this.sparql = this.asAskSparql();
    // break;
    // case CONSTRUCT:
    // this.sparql = this.asConstruct();
    // break;
    // case DESCRIBE:
    // this.sparql = this.asDescribeSparql();
    // break;
    // case SELECT:
    // this.sparql = this.asSelectSparql();
    // break;
    // }
    // }
    //
    // return this.sparql;
    // }

    // private String asAskSparql() {
    // StringBuilder query = new StringBuilder();
    // query.append("ASK { GRAPH ");
    // query.append(SemanticHelper.toNTripleSyntax(this.elements[0]));
    // query.append(" { ");
    //
    // for (int i = 1; i < 4; i++) {
    // query.append(SemanticHelper.toNTripleSyntax(this.elements[i]));
    // query.append(" ");
    // }
    //
    // query.append("} }");
    // return query.toString();
    // }

    // public String asConstruct() {
    // String subjectAsString = RdfFormatter.format(this.quad.getSubject());
    // String predicateAsString =
    // RdfFormatter.format(this.quad.getPredicate());
    // String objectAsString = RdfFormatter.format(this.quad.getObject());
    //
    // StringBuilder query = new StringBuilder();
    // query.append("CONSTRUCT { ");
    // query.append(subjectAsString);
    // query.append(" ");
    // query.append(predicateAsString);
    // query.append(" ");
    // query.append(objectAsString);
    // query.append("} WHERE { GRAPH ");
    // query.append(RdfFormatter.format(this.quad.getGraph()));
    // query.append(" { ");
    // query.append(subjectAsString);
    // query.append(" ");
    // query.append(predicateAsString);
    // query.append(" ");
    // query.append(objectAsString);
    // query.append("} }");
    //
    // return query.toString();
    // }

    // private String asDescribeSparql() {
    // StringBuilder query = new StringBuilder();
    // query.append("DESCRIBE { ");
    //
    // for (int i = 1; i < 4; i++) {
    // query.append(SemanticHelper.toNTripleSyntax(this.elements[i]));
    // query.append(" ");
    // }
    //
    // query.append("} WHERE { GRAPH ");
    // query.append(SemanticHelper.toNTripleSyntax(this.elements[0]));
    // query.append(" { ");
    //
    // for (int i = 1; i < 4; i++) {
    // query.append(SemanticHelper.toNTripleSyntax(this.elements[i]));
    // query.append(" ");
    // }
    //
    // query.append("} }");
    // return query.toString();
    // }
    //
    // private String asSelectSparql() {
    // StringBuilder query = new StringBuilder();
    // query.append("SELECT ");
    //
    // for (int i = 1; i < 4; i++) {
    // query.append(SemanticHelper.toNTripleSyntax(this.elements[i]));
    // query.append(" ");
    // }
    //
    // query.append("WHERE { GRAPH ");
    // query.append(SemanticHelper.toNTripleSyntax(this.elements[0]));
    // query.append(" { ");
    //
    // for (int i = 1; i < 4; i++) {
    // query.append(SemanticHelper.toNTripleSyntax(this.elements[i]));
    // query.append(" ");
    // }
    //
    // query.append("} }");
    // return query.toString();
    // }

    public int getNumberOfVariables() {
        return this.vars.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.quadruplePattern.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof AtomicQuery
                && this.quadruplePattern.equals(((QuadruplePattern) obj));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("(");
        result.append(this.getGraph());
        result.append(" ");
        result.append(this.getSubject());
        result.append(" ");
        result.append(this.getPredicate());
        result.append(" ");
        result.append(this.getObject());
        result.append(")");
        return result.toString();
    }

    // private static final String[] varNames = {"g", "s", "p", "o"};

    // public static QuadruplePattern create(ParentQueryForm parentQueryForm) {
    // Node[] nodes = new Node[4];
    // for (int i = 0; i < 4; i++) {
    // nodes[i] = createNode(i);
    // }
    //
    // return new QuadruplePattern(
    // parentQueryForm, nodes[0], nodes[1], nodes[2], nodes[3]);
    // }

    // public static Node createNode(int index) {
    // if (ProActiveRandom.nextInt(10) > 7) {
    // return Node.createVariable(varNames[index]);
    // } else {
    // if (index > 2) {
    // return NodeGenerator.createNode(5, 10);
    // } else {
    // return NodeGenerator.createUri(5, 10);
    // }
    // }
    // }

}
