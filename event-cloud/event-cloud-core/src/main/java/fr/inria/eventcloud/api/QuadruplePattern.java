package fr.inria.eventcloud.api;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * A QuadruplePattern is {@link Quadruple} where each component value may be
 * either a regular value (the same as a Quadruple) or a variable. A variable is
 * constructed by using the {@link Node#ANY} or the {@code null} value.
 * <p>
 * For example, a quadruple {@code Q=(Node.ANY, v1, v2, v3)} means that we want
 * to retrieve all the quadruples that have a graph value set to any value and a
 * subject, predicate and object value respectively set to {@code v1},
 * {@code v2} and {@code v3}.
 * 
 * @author lpellegr
 */
public final class QuadruplePattern extends Quadruple {

    private static final long serialVersionUID = 1L;

    /**
     * QuadruplePattern that may be used to retrieve all the quadruples.
     */
    public static final QuadruplePattern ANY = new QuadruplePattern(
            Node.ANY, Node.ANY, Node.ANY, Node.ANY);

    public QuadruplePattern(Node g, Node s, Node p, Node o) {
        super(replaceNullWithVarNode(g), replaceNullWithVarNode(s),
                replaceNullWithVarNode(p), replaceNullWithVarNode(o), false);
    }

    private static Node replaceNullWithVarNode(Node node) {
        if (node == null || node instanceof Node_Variable) {
            return Node.ANY;
        }

        return node;
    }

}
