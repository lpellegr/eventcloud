package fr.inria.eventcloud.reasoner;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy;
import com.hp.hpl.jena.sparql.expr.ExprTransformer;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class Test {

    public static void main(String[] args) {
        Expr expr =
                ExprUtils.parse("((?s > 10 ) ||  (?o < \"http://namespace.org/test\" && ?j < 20) )");

        System.out.println("INPUT => " + ExprUtils.fmtSPARQL(expr));

        Expr newExpr = ExprTransformer.transform(new ExprTransformCopy() {
            @Override
            public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {
                System.out.println("ExprTransformBase.transform(ExprFunction2, Expr, Expr)");
                if (expr1 == null) {
                    return new E_Null();
                }

                if (expr2 == null) {
                    return new E_Null();
                }

                if (expr1 instanceof E_Null) {
                    return expr2;
                }

                if (expr2 instanceof E_Null) {
                    return expr1;
                }

                return super.transform(func, expr1, expr2);
            }

            @Override
            public Expr transform(ExprVar exprVar) {
                System.out.println("ExprTransformBase.transform(ExprVar)");
                // supprime les conditions qui portent sur la variable o
                if (exprVar.getVarName().equals("o")) {
                    return NodeValue.nvNothing;
                }

                return exprVar;
            }
        }, expr);

        System.out.println("OUTPUT => " + ExprUtils.fmtSPARQL(newExpr));
    }

    private static class E_Null extends ExprFunction0 {
        private static final String symbol = "null";

        public E_Null() {
            super(symbol);
        }

        @Override
        public NodeValue eval(FunctionEnv env) {
            return NodeValue.nvNothing;
        }

        @Override
        public Expr copy() {
            return new E_Null();
        }
    }

}
