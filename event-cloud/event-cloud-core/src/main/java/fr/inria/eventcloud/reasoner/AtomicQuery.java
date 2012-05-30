/**
 * Copyright (c) 2011-2012 INRIA.
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

import java.util.Arrays;
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

    public enum ParentQueryForm {
        ASK, CONSTRUCT, DESCRIBE, SELECT
    };

    private final ParentQueryForm parentQueryForm;

    private final Node nodes[];

    // contains the variables and its associated positions
    // TODO: this field could be transient?
    private Map<String, Integer> vars;

    public AtomicQuery(ParentQueryForm form, Node graph, Node subject,
            Node predicate, Node object) {
        this.parentQueryForm = form;
        this.nodes = new Node[] {graph, subject, predicate, object};

        this.vars = new HashMap<String, Integer>(4);
        Node[] nodes = {graph, subject, predicate, object};
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].isVariable()) {
                this.vars.put(nodes[i].getName(), i);
            }
        }
    }

    public boolean hasLiteralObject() {
        return this.nodes[2] != null && this.nodes[2].isLiteral();
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
        Integer result = this.vars.get(varName);
        if (result == null) {
            return -1;
        }

        return result;
    }

    public boolean hasVariable(String varName) {
        return this.getVarIndex(varName) != -1;
    }

    public ParentQueryForm getParentQueryForm() {
        return this.parentQueryForm;
    }

    public QuadruplePattern getQuadruplePattern() {
        return new QuadruplePattern(
                replaceVarNodeByNodeAny(this.nodes[0]),
                replaceVarNodeByNodeAny(this.nodes[1]),
                replaceVarNodeByNodeAny(this.nodes[2]),
                replaceVarNodeByNodeAny(this.nodes[3]));
    }

    public Node getGraph() {
        return this.nodes[0];
    }

    public Node getSubject() {
        return this.nodes[1];
    }

    public Node getPredicate() {
        return this.nodes[2];
    }

    public Node getObject() {
        return this.nodes[3];
    }

    public Node[] toArray() {
        return this.nodes;
    }

    public Set<Var> getVariables() {
        Set<Var> vars = new HashSet<Var>();
        for (String varName : this.vars.keySet()) {
            vars.add(Var.alloc(varName));
        }
        return vars;
    }

    public int getNumberOfVariables() {
        return this.vars.size();
    }

    private static final Node replaceVarNodeByNodeAny(Node node) {
        if (node.isVariable()) {
            return Node.ANY;
        }

        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.nodes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof AtomicQuery
                && Arrays.equals(this.nodes, ((AtomicQuery) obj).toArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder('(');
        result.append(this.getGraph());
        result.append(' ');
        result.append(this.getSubject());
        result.append(' ');
        result.append(this.getPredicate());
        result.append(' ');
        result.append(this.getObject());
        result.append(')');
        return result.toString();
    }

}
