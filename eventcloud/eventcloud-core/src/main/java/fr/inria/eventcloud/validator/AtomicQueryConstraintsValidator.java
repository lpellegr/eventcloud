package fr.inria.eventcloud.validator;

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

import static com.google.common.base.Preconditions.checkNotNull;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.AnycastConstraintsValidator;

import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy;
import com.hp.hpl.jena.sparql.expr.ExprTransformer;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * This class is the default validator for {@link AnycastRequestRouter}. This
 * validator assumes that all coordinate elements set to {@code null} match the
 * constraints.
 * 
 * @author lpellegr
 */
public final class AtomicQueryConstraintsValidator extends
        AnycastConstraintsValidator<StringCoordinate> {

    private static final long serialVersionUID = 1L;

    private AtomicQuery atomicQuery;

    private FilterTransformer transformer;

    /**
     * Creates a new {@code DefaultAnycastConstraintsValidator} which is a very
     * permissive constraints validator (i.e. the valitor validates the
     * constraints on any peer).
     */
    public AtomicQueryConstraintsValidator() {
        super(
                new StringCoordinate(
                        new StringElement[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()]));
    }

    /**
     * Creates a new {@code DefaultAnycastConstraintsValidator} with the
     * specified {@code key} to reach.
     * 
     * @param key
     *            the key to reach.
     */
    public AtomicQueryConstraintsValidator(StringCoordinate key) {
        super(checkNotNull(key));
    }

    public AtomicQueryConstraintsValidator(AtomicQuery atomicQuery) {
        super(
                new StringCoordinate(
                        new StringElement[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()]));
        this.atomicQuery = atomicQuery;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validatesKeyConstraints(StructuredOverlay overlay) {
        this.validatesKeyConstraints(((CanOverlay) overlay).getZone());
        return this.transformer.finalDecision;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validatesKeyConstraints(Zone zone) {
        this.transformer = new FilterTransformer(zone, this.atomicQuery);
        for (int i = 0; i < this.atomicQuery.getFilterConstraints().size(); i++) {
            ExprTransformer.transform(
                    this.getTransformer(),
                    this.atomicQuery.getFilterConstraints().get(i));
        }
        return this.getTransformer().finalDecision;
    }

    public AtomicQuery getAtomicQuery() {
        return this.atomicQuery;
    }

    // used by AtomicQueryConstraintsValidatorTest only
    public void setAtomicQuery(AtomicQuery atomicQuery) {
        this.atomicQuery = atomicQuery;
    }

    private static class FilterTransformer extends ExprTransformCopy {

        private AtomicQuery atomicQuery;

        private Zone zone;

        private boolean finalDecision;

        public FilterTransformer(Zone zone, AtomicQuery atomicQuery) {
            super();
            this.zone = zone;
            this.atomicQuery = atomicQuery;
            this.finalDecision = false;
        }

        @Override
        public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {
            if (func != null) {
                if (!(func instanceof E_LogicalAnd || func instanceof E_LogicalOr)) {
                    Expr variable, constant;
                    System.out.println("ExprTransformBase.transform(ExprFunction2, Expr, Expr)");

                    if (func.getArg1().isVariable()) {
                        variable = expr1;
                    } else {
                        variable = expr2;
                    }

                    if (func.getArg1().isConstant()) {
                        constant = expr1;
                    } else {
                        constant = expr2;
                    }

                    int dimension =
                            this.atomicQuery.getVarIndex(variable.getVarName());
                    int compareToLowerBound =
                            this.zone.getLowerBound((byte) dimension)
                                    .getStringValue()
                                    .compareTo(
                                            constant.getConstant().asString());

                    int compareToUpperBound =
                            this.zone.getUpperBound((byte) dimension)
                                    .getStringValue()
                                    .compareTo(
                                            constant.getConstant().asString());

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
                    System.out.println("logical and " + func.toString());
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
                    System.out.println("BOOL : " + bool1.toString() + " "
                            + bool2.toString());
                    if (func instanceof E_LogicalAnd) { // si op gauche faux
                                                        // renvoyer faux
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
            return null;
        }

    }

    private static class E_Bool extends ExprFunction0 {

        private boolean bool;

        public E_Bool(boolean bool) {
            super(new Boolean(bool).toString());
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
            // TODO Auto-generated method stub
            return "" + this.isBool();
        }

    }

    public FilterTransformer getTransformer() {
        return this.transformer;
    }

    public void setTransformer(FilterTransformer transformer) {
        this.transformer = transformer;
    }

}
