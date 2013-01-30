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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.AnycastConstraintsValidator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy;
import com.hp.hpl.jena.sparql.expr.ExprTransformer;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.util.ExprUtils;

import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.overlay.can.SemanticZone;
import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * This class is the default validator for {@link AnycastRequestRouter}. This
 * validator assumes that all coordinate elements set to {@code null} match the
 * constraints.
 * 
 * @author lpellegr
 */
public final class AtomicQueryConstraintsValidator<E extends StringElement>
        extends AnycastConstraintsValidator<E> {

    private static final long serialVersionUID = 1L;

    private AtomicQuery atomicQuery;

    private FilterTransformer transformer;

    /**
     * Creates a new {@code AtomicQueryConstraintsValidator} with the specified
     * {@code key} to reach.
     * 
     * @param key
     *            the key to reach.
     */
    // renvoie un tab de 4 elements E > null si var ou node any sinon un new E
    // de la string
    // crée un coordonée a partir de ce tab à passer en param du super
    public AtomicQueryConstraintsValidator(AtomicQuery query) {
        super(
                checkNotNull((Coordinate<E>) (AtomicQueryConstraintsValidator.replaceVariablesByNull(query))));
        this.atomicQuery = query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final boolean validatesKeyConstraints(StructuredOverlay overlay) {
        return this.validatesKeyConstraints(((CanOverlay<E>) overlay).getZone());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean validatesKeyConstraints(Zone<E> zone) {
        this.transformer = new FilterTransformer(zone, this.atomicQuery);
        // check fixed parts
        for (byte i = 0; i < super.key.getValue().size(); i++) {
            // if coordinate is null we skip the test
            if (super.key.getValue().getElement(i) != null) {
                // the specified overlay does not contains the key
            if (zone.contains(i, super.key.getValue().getElement(i)) != 0) {             
                return false;
                }
            }
        }
        
        if (this.atomicQuery.getFilterConstraints() != null)
        {
            System.out.println("AtomicQueryConstraintsValidator il y a contrainte de filtre");
        for (int i = 0; i < this.atomicQuery.getFilterConstraints().size(); i++) {
            ExprTransformer.transform(
                    this.getTransformer(),
                    this.atomicQuery.getFilterConstraints().get(i));
        }
        System.out.println("je retourne " + this.getTransformer().finalDecision);
        return this.getTransformer().finalDecision;
        }
        else
        {
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

    private class FilterTransformer extends ExprTransformCopy implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private AtomicQuery atomicQuery;

        private Zone<E> zone;

        private boolean finalDecision;

        public FilterTransformer(Zone<E> zone, AtomicQuery atomicQuery) {
            super();
            this.zone = zone;
            this.atomicQuery = atomicQuery;
            this.finalDecision = false;
        }

        @Override
        public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {
            System.out.println("func = " + ExprUtils.fmtSPARQL(func));
            System.out.println("func instanceof = " + func.getClass().getSimpleName());
            
            if (func != null) {
                if (!(func instanceof E_LogicalAnd || func instanceof E_LogicalOr)) {
                    Expr variable;
                    String constant;
                    
                    if (func.getArg1().isVariable()) {
                        variable = expr1;
                    } else {
                        variable = expr2;
                    }

                    if (func.getArg1().isConstant()) {
                        constant =
                                SemanticElement.removePrefix(Node.createURI(expr1.getConstant()
                                        .asString()));
                    } else {
                        constant =
                                SemanticElement.removePrefix(Node.createURI(expr2.getConstant()
                                        .asString()));
                    }

                    int dimension =
                            this.atomicQuery.getVarIndex(variable.getVarName());

                    int compareToLowerBound =
                            this.zone.getLowerBound((byte) dimension)
                                    .getValue()
                                    .compareTo(constant);

                    int compareToUpperBound =
                            this.zone.getUpperBound((byte) dimension)
                                    .getValue()
                                    .compareTo(constant);
                    
                    System.out.println("dimension = " + dimension);
                    System.out.println("borne inf = " + this.zone.getLowerBound((byte) dimension)
                            .toString());
                    System.out.println("borne sup = " + this.zone.getUpperBound((byte) dimension)
                            .toString());

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
                            System.out.println("AtomicQueryConstraintsValidator.FilterTransformer.transform()");
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
                    System.out.println("arg1 = " + func.getArg1().getVarName() + " ET arg2 = " +
                            func.getArg2().getVarName());
                    // or try/catch ClassCastException on (ExprFunction2) func.getArg1();
                    if (func.getArg1().getVarName().equals(func.getArg2().getVarName()))
                    {
                        return new E_Bool(false);
                    }
                    System.out.println("classe = "+ func.getClass().getSimpleName() + "func = " + ExprUtils.fmtSPARQL(func));
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
            return null;
        }

    }

    private class E_Bool extends ExprFunction0 {

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

    private static Coordinate<SemanticElement> replaceVariablesByNull(AtomicQuery query) {
        SemanticElement[] tab = new SemanticElement[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];
        
        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++)
        {
            if (query.toArray()[i].isVariable() || query.toArray()[i].matches(Node.ANY))
            {
                query.toArray()[i] = null;
            }
            else
            {
                tab[i] = new SemanticElement(query.toArray()[i]);
            }
        }
        
        return new Coordinate<SemanticElement>(tab);
    }

}
