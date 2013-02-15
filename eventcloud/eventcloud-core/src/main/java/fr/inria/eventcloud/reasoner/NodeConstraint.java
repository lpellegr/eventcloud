package fr.inria.eventcloud.reasoner;

import com.hp.hpl.jena.graph.Node;

public class NodeConstraint {

    private Node node;

    private int dimension;

    private String constraint;

    private String constraintConstant;

    private boolean isVarFirstArg;

    public NodeConstraint(Node node, int dimension, String constraint,
            String constraintConstant, boolean isVarFirstArg) {
        this.setNode(node);
        this.setDimension(dimension);
        this.setConstraint(constraint);
        this.setConstraintConstant(constraintConstant);
        this.setVarFirstArg(isVarFirstArg);
    }

    public Node getNode() {
        return this.node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public String getConstraint() {
        return this.constraint;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public String getConstraintConstant() {
        return this.constraintConstant;
    }

    public void setConstraintConstant(String constraintConstant) {
        this.constraintConstant = constraintConstant;
    }

    public int getDimension() {
        return this.dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public boolean isVarFirstArg() {
        return this.isVarFirstArg;
    }

    public void setVarFirstArg(boolean isVarFirstArg) {
        this.isVarFirstArg = isVarFirstArg;
    }

}
