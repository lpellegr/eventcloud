/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.reasoner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.riot.out.OutputLangUtils;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.TokenType;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.util.ExprUtils;

import fr.inria.eventcloud.api.QuadruplePattern;

/**
 * Atomic queries are {@link QuadruplePattern}s that may contain sequence
 * modifiers such as limit, offset, order by, etc. but also filter constraints
 * for any declared variable.
 * 
 * @author lpellegr
 * @author mantoine
 * 
 * @see SparqlDecomposer
 */
public final class AtomicQuery implements Serializable {

    private static final long serialVersionUID = 160L;

    private static final char[] posNames = {'g', 's', 'p', 'o'};

    private transient Node nodes[];

    private transient VarDetails[] varDetails;

    private transient Op opRepresentation;

    /* 
     * Sequence modifiers 
     * 
     * Projection is ignored because results have to be filtered 
     * again once they are received. Offset must also be ignored 
     * because we can not foretell the final indexes that will be 
     * associated to the results returned by this atomic query. 
     * Indeed, applying an offset to each atomic query may filter 
     * results that should be available in the final result.
     */

    // eliminates duplicate solutions
    private boolean distinct = false;
    // permits duplicate solutions to be eliminated
    private boolean reduced = false;
    // puts an upper bound on the number of solutions returned
    private long limit = Long.MIN_VALUE;
    // put the solutions in order
    private transient List<SortCondition> orderBy;

    // filter constraints
    private transient List<ExprList> filterConstraints;

    public AtomicQuery(Node graph, Node subject, Node predicate, Node object) {
        this.nodes = new Node[] {graph, subject, predicate, object};
    }

    public AtomicQuery() {
        this.nodes = null;
    };

    public boolean hasLiteralObject() {
        return this.nodes[2] != null && this.nodes[2].isLiteral();
    }

    public Node getNode(int index) {
        if (index < 0 || index > 3) {
            throw new IllegalArgumentException("Illegal index: " + index);
        }

        return this.nodes[index];
    }

    public String getVarName(int index) {
        for (VarDetails varDetail : this.getVarDetails()) {
            if (varDetail.index == index) {
                return varDetail.name;
            }
        }

        return null;
    }

    public String[] getVarNames() {
        String[] result = new String[this.getVarDetails().length];

        for (int i = 0; i < result.length; i++) {
            result[i] = this.getVarDetails()[i].name;
        }

        return result;
    }

    public String getVarNamesAsString() {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < this.getVarDetails().length; i++) {
            buf.append(posNames[this.getVarDetails()[i].index]);
            buf.append('=');
            buf.append(this.getVarDetails()[i].name);

            if (i < this.getVarDetails().length - 1) {
                buf.append(',');
            }
        }

        return buf.toString();
    }

    public static Node[] parseVarNamesFromString(String varNames) {
        String[] tokens = varNames.split(",");

        Node[] result = new Node[4];

        for (int i = 0; i < tokens.length; i++) {
            String posName = tokens[i].substring(0, 1);
            Node var =
                    NodeFactory.createVariable(tokens[i].substring(
                            2, tokens[i].length()));

            if (posName.equals("g")) {
                result[0] = var;
            } else if (posName.equals("s")) {
                result[1] = var;
            } else if (posName.equals("p")) {
                result[2] = var;
            } else if (posName.equals("o")) {
                result[3] = var;
            }
        }

        return result;
    }

    public int getVarIndex(String varName) {
        for (VarDetails varDetail : this.getVarDetails()) {
            if (varDetail.name.equals(varName)) {
                return varDetail.index;
            }
        }

        return -1;
    }

    public boolean containsVariable(String varName) {
        for (VarDetails varDetail : this.getVarDetails()) {
            if (varDetail.name.equals(varName)) {
                return true;
            }
        }

        return false;
    }

    public synchronized Op getOpRepresentation() {
        if (this.opRepresentation == null) {
            BasicPattern bp = new BasicPattern();
            bp.add(Triple.create(
                    this.filterAndTransformNodeVariableToVar(this.getSubject()),
                    this.filterAndTransformNodeVariableToVar(this.getPredicate()),
                    this.filterAndTransformNodeVariableToVar(this.getObject())));

            // named graph
            Op op = new OpBGP(bp);

            // apply filter constraints
            if (this.filterConstraints != null) {
                for (ExprList expr : this.filterConstraints) {
                    op = OpFilter.filter(expr, op);
                }
            }

            op =
                    new OpGraph(
                            this.filterAndTransformNodeVariableToVar(this.getGraph()),
                            op);

            if (this.orderBy != null) {
                op = new OpOrder(op, this.orderBy);
            }

            // projection
            op = new OpProject(op, ImmutableList.copyOf(this.getVars()));

            // apply sequence modifiers
            if (this.distinct) {
                op = new OpDistinct(op);
            }
            if (this.reduced) {
                op = OpReduced.create(op);
            }
            if (this.hasLimit()) {
                // offset is ignored by using the internal Jena default value
                op = new OpSlice(op, Long.MIN_VALUE, this.limit);
            }

            this.opRepresentation = op;
        }

        return this.opRepresentation;
    }

    public Node filterAndTransformNodeVariableToVar(Node node) {
        if (node.isVariable()) {
            return Var.alloc(node);
        } else {
            return node;
        }
    }

    private synchronized VarDetails[] getVarDetails() {
        if (this.varDetails == null) {
            int nbVars = 0;

            for (int i = 0; i < this.nodes.length; i++) {
                if (this.nodes[i].isVariable()) {
                    nbVars++;
                }
            }

            this.varDetails = new VarDetails[nbVars];

            int j = 0;
            for (int i = 0; i < this.nodes.length; i++) {
                if (this.nodes[i].isVariable()) {
                    this.varDetails[j] =
                            new VarDetails(this.nodes[i].getName(), i);
                    j++;
                }
            }
        }

        return this.varDetails;
    }

    private static final Node replaceVarNodeByNodeAny(Node node) {
        if (node.isVariable()) {
            return Node.ANY;
        }

        return node;
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
        return this.nodes.clone();
    }

    public List<Var> getVars() {
        Builder<Var> result = new ImmutableList.Builder<Var>();

        for (VarDetails varDetail : this.getVarDetails()) {
            result.add(Var.alloc(varDetail.name));
        }

        return result.build();
    }

    public int getNbVars() {
        return this.getVarDetails().length;
    }

    public boolean isDistinct() {
        return this.distinct;
    }

    /**
     * Returns {@code true} if the subscription requires filtering evaluation,
     * {@code false} otherwise.
     */
    public boolean isFilterEvaluationRequired() {
        return !this.filterConstraints.isEmpty();
    }

    public boolean isReduced() {
        return this.reduced;
    }

    public void setFilterConstraints(List<ExprList> filterConstraints) {
        this.filterConstraints = filterConstraints;
    }

    public List<ExprList> getFilterConstraints() {
        return this.filterConstraints;
    }

    public long getLimit() {
        return this.limit;
    }

    public List<SortCondition> getOrderBy() {
        return this.orderBy;
    }

    public boolean hasLimit() {
        return this.limit >= 0;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public void setReduced(boolean reduced) {
        this.reduced = reduced;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public void setOrderBy(List<SortCondition> sortConditions) {
        this.orderBy = sortConditions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof AtomicQuery
                && this.getOpRepresentation().equals(
                        ((AtomicQuery) obj).getOpRepresentation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.getOpRepresentation().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return OpAsQuery.asQuery(this.getOpRepresentation()).toString();
    }

    private void readObject(ObjectInputStream in) {
        try {
            in.defaultReadObject();

            // reads sort conditions
            int nbSortConditions = in.readInt();

            if (nbSortConditions > 0) {
                this.orderBy = new ArrayList<SortCondition>(nbSortConditions);

                for (int i = 0; i < nbSortConditions; i++) {
                    int direction = in.readInt();

                    String s = in.readUTF();
                    Expr expr = ExprUtils.parse(s);

                    this.orderBy.add(new SortCondition(expr, direction));
                }
            }

            // reads filter conditions
            int nbFilterConditions = in.readInt();
            if (nbFilterConditions > 0) {
                this.filterConstraints =
                        new ArrayList<ExprList>(nbFilterConditions);

                for (int i = 0; i < nbFilterConditions; i++) {
                    String s = in.readUTF();
                    Expr expr = ExprUtils.parse(s);
                    this.filterConstraints.add(new ExprList(expr));
                }
            } else {
                this.filterConstraints = new ArrayList<ExprList>();
            }

            // read nodes
            this.nodes = new Node[4];
            Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in);

            for (int i = 0; i < this.nodes.length; i++) {
                Token token = tokenizer.next();

                Node node;
                if (token.getType() == TokenType.VAR) {
                    node = NodeFactory.createVariable(token.getImage());
                } else {
                    node = token.asNode();
                }

                this.nodes[i] = node;
            }
        } catch (Throwable t) {
            // needed to catch SPARQL parse exceptions
            // otherwise ProActive eats it
            t.printStackTrace();
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        // write sort conditions
        if (this.orderBy != null) {
            out.writeInt(this.orderBy.size());

            for (SortCondition sortCondition : this.orderBy) {
                out.writeInt(sortCondition.getDirection());
                // previous write erased parenthesis for str(?x)
                // out.writeUTF(WriterExpr.asString(sortCondition.getExpression()));
                out.writeUTF(ExprUtils.fmtSPARQL(sortCondition.getExpression()));
            }
        } else {
            out.writeInt(0);
        }

        // write filter conditions
        if (this.filterConstraints != null) {
            out.writeInt(this.filterConstraints.size());
            for (ExprList exprList : this.filterConstraints) {
                String s = ExprUtils.fmtSPARQL(exprList);
                out.writeUTF(s);
            }
        } else {
            out.writeInt(0);
        }

        OutputStreamWriter outWriter = new OutputStreamWriter(out);

        // write nodes
        for (int i = 0; i < this.nodes.length; i++) {

            OutputLangUtils.output(outWriter, this.nodes[i], null);

            if (i < this.nodes.length - 1) {
                outWriter.write(' ');
            }
        }
        outWriter.flush();

    }

    private static final class VarDetails {

        public final String name;

        public final int index;

        public VarDetails(String name, int index) {
            super();
            this.name = name;
            this.index = index;
        }

    }

}
