/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.validator;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.MulticastConstraintsValidator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy;
import com.hp.hpl.jena.sparql.expr.ExprTransformer;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.util.ExprUtils;

import fr.inria.eventcloud.messages.request.SparqlAtomicRequest;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;
import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * This class is the default validator for {@link SparqlAtomicRequest}. This
 * validator assumes that all coordinate elements set to {@code null} match the
 * constraints. It also leverage to the filter constraints that are specified to
 * improve routing.
 * 
 * @author mantoine
 */
public final class AtomicQueryConstraintsValidator extends
        MulticastConstraintsValidator<SemanticCoordinate> {

    private static final long serialVersionUID = 160L;

    private AtomicQuery atomicQuery;

    private FilterTransformer transformer;

    public AtomicQueryConstraintsValidator(AtomicQuery query) {
        super(
                checkNotNull(AtomicQueryConstraintsValidator.replaceVariablesByNull(query)));
        this.atomicQuery = query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validatesKeyConstraints(StructuredOverlay overlay) {
        return this.validatesKeyConstraints(((SemanticCanOverlay) overlay).getZone());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validatesKeyConstraints(Zone<SemanticCoordinate> zone) {
        this.transformer = new FilterTransformer(zone, this.atomicQuery);
        // check fixed parts
        for (byte i = 0; i < super.key.getValue().size(); i++) {
            // if coordinate is null we skip the test
            if (super.key.getValue().getCoordinate(i) != null) {
                // the specified overlay does not contains the key
                if (zone.contains(i, super.key.getValue().getCoordinate(i)) != 0) {
                    return false;
                }
            }
        }

        if (this.atomicQuery.getFilterConstraints().size() > 0) {
            for (int i = 0; i < this.atomicQuery.getFilterConstraints().size(); i++) {
                ExprTransformer.transform(
                        this.getTransformer(),
                        this.atomicQuery.getFilterConstraints().get(i));
            }
            return this.getTransformer().finalDecision;
        } else {
            // no filter condition to apply on this atomic query
            return true;
        }
    }

    public AtomicQuery getAtomicQuery() {
        return this.atomicQuery;
    }

    // used by AtomicQueryConstraintsValidatorTest only
    public void setAtomicQuery(AtomicQuery atomicQuery) {
        this.atomicQuery = atomicQuery;
    }

    private class FilterTransformer extends ExprTransformCopy implements
            Serializable {

        private static final long serialVersionUID = 160L;

        private AtomicQuery atomicQuery;

        private Zone<SemanticCoordinate> zone;

        private boolean finalDecision;

        public FilterTransformer(Zone<SemanticCoordinate> zone,
                AtomicQuery atomicQuery) {
            super();
            this.zone = zone;
            this.atomicQuery = atomicQuery;
            this.finalDecision = false;
        }

        @Override
        public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {

            if (func != null) {
                if (!(func instanceof E_LogicalAnd || func instanceof E_LogicalOr)) {
                    Expr variable;
                    String constant;

                    // we have to remove str(?x) to keep only ?x for this
                    // validator
                    if (func.getArg1().toString().startsWith("str(")) {
                        expr1 =
                                ExprUtils.parse(func.getArg1()
                                        .toString()
                                        .substring(
                                                func.getArg1()
                                                        .toString()
                                                        .indexOf("(", 0) + 1,
                                                func.getArg1()
                                                        .toString()
                                                        .length() - 1));
                    } else if (func.getArg2().toString().startsWith("str(")) {
                        expr2 =
                                ExprUtils.parse(func.getArg2()
                                        .toString()
                                        .substring(
                                                func.getArg2()
                                                        .toString()
                                                        .indexOf("(", 0) + 1,
                                                func.getArg2()
                                                        .toString()
                                                        .length() - 1));
                    }

                    if (expr1.isVariable()) {
                        variable = expr1;
                    } else {
                        variable = expr2;
                    }

                    if (func.getArg1().isConstant()) {
                        constant =
                                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI(expr1.getConstant()
                                        .asString()));
                    } else {
                        constant =
                                SemanticCoordinate.applyDopingFunction(NodeFactory.createURI(expr2.getConstant()
                                        .asString()));
                    }

                    int dimension =
                            this.atomicQuery.getVarIndex(variable.getVarName());

                    int compareToLowerBound =
                            SemanticCoordinate.applyDopingFunction(
                                    NodeFactory.createURI(this.zone.getLowerBound(
                                            (byte) dimension)
                                            .getValue()))
                                    .compareTo(constant);

                    int compareToUpperBound =
                            SemanticCoordinate.applyDopingFunction(
                                    NodeFactory.createURI(this.zone.getUpperBound(
                                            (byte) dimension)
                                            .getValue()))
                                    .compareTo(constant);

                    if (func.getArg1().isVariable()) {
                        if (func.getOpName().equals(">")
                                && compareToUpperBound > 0) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else if (func.getOpName().equals(">")
                                && compareToUpperBound <= 0) {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        } else if (func.getOpName().equals(">=")
                                && compareToUpperBound >= 0) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else if (func.getOpName().equals(">=")
                                && compareToUpperBound < 0) {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        } else if (func.getOpName().equals("<")
                                && compareToLowerBound < 0) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else if (func.getOpName().equals("<")
                                && compareToLowerBound >= 0) {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        } else if (func.getOpName().equals("<=")
                                && compareToLowerBound <= 0) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else if (func.getOpName().equals("<=")
                                && compareToLowerBound > 0) {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        } else if (func.getOpName().equals("=")
                                && (compareToLowerBound == 0
                                        || compareToUpperBound == 0 || (compareToUpperBound > 0 && compareToLowerBound < 0))) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else if (func.getOpName().equals("!=")) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        }
                    } else // arg1 is constant
                    {
                        if (func.getOpName().equals(">")
                                && compareToLowerBound < 0) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else if (func.getOpName().equals(">")
                                && compareToLowerBound >= 0) {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        } else if (func.getOpName().equals(">=")
                                && compareToLowerBound <= 0) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else if (func.getOpName().equals(">=")
                                && compareToLowerBound > 0) {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        } else if (func.getOpName().equals("<")
                                && compareToUpperBound > 0) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else if (func.getOpName().equals("<")
                                && compareToUpperBound <= 0) {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        } else if (func.getOpName().equals("<=")
                                && compareToUpperBound >= 0) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else if (func.getOpName().equals("<=")
                                && compareToUpperBound < 0) {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        } else if (func.getOpName().equals("=")
                                && (compareToLowerBound == 0
                                        || compareToUpperBound == 0 || (compareToUpperBound > 0 && compareToLowerBound < 0))) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else if (func.getOpName().equals("!=")) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        }
                    }
                } else if (func instanceof E_LogicalAnd
                        || func instanceof E_LogicalOr) {
                    // if expr looks like ?x > ?x (mainly because of Jena)
                    // we cannot use this validator so query is sent to peer
                    try {
                        if (func.getArg1().getVarName().equals(
                                func.getArg2().getVarName())) {
                            return new E_Bool(true);
                        }
                    } catch (NullPointerException e) {
                    };

                    ExprFunction2 exprfunc2_1 = (ExprFunction2) func.getArg1();
                    Expr exprfunc2_1_bool =
                            this.transform(
                                    exprfunc2_1, exprfunc2_1.getArg1(),
                                    exprfunc2_1.getArg2());
                    ExprFunction2 exprfunc2_2 = (ExprFunction2) func.getArg2();
                    Expr exprfunc2_2_bool =
                            this.transform(
                                    exprfunc2_2, exprfunc2_2.getArg1(),
                                    exprfunc2_2.getArg2());
                    E_Bool bool1 = (E_Bool) exprfunc2_1_bool;
                    E_Bool bool2 = (E_Bool) exprfunc2_2_bool;
                    if (func instanceof E_LogicalAnd) {
                        if (bool1.isBool() && bool2.isBool()) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        }
                    } else if (func instanceof E_LogicalOr) {
                        if (bool1.isBool() || bool2.isBool()) {
                            this.finalDecision = true;
                            return new E_Bool(true);
                        } else {
                            this.finalDecision = false;
                            return new E_Bool(false);
                        }
                    } else {
                        this.finalDecision = false;
                        return new E_Bool(false);
                    }

                }
            }
            return new E_Bool(false);
        }

    }

    private class E_Bool extends ExprFunction0 {

        private boolean bool;

        public E_Bool(boolean bool) {
            super(Boolean.toString(bool));
            this.setBool(bool);
        }

        @Override
        public NodeValue eval(FunctionEnv env) {
            return NodeValue.nvNothing;
        }

        @Override
        public Expr copy() {
            return new E_Bool(false);
        }

        public boolean isBool() {
            return this.bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        @Override
        public String toString() {
            return Boolean.toString(this.isBool());
        }

    }

    public FilterTransformer getTransformer() {
        return this.transformer;
    }

    public void setTransformer(FilterTransformer transformer) {
        this.transformer = transformer;
    }

    private static Point<SemanticCoordinate> replaceVariablesByNull(AtomicQuery query) {
        SemanticCoordinate[] tab =
                new SemanticCoordinate[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];

        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            if (query.getNode(i) != null && !query.getNode(i).equals(Node.ANY)
                    && !query.getNode(i).isVariable()) {
                tab[i] = new SemanticCoordinate(query.getNode(i));
            }
        }

        return new Point<SemanticCoordinate>(tab);
    }

}
