package fr.inria.eventcloud.reasoner;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Element;


/**
 * @author lpellegr
 */
public class RangeQueryCondition {

	private static final long serialVersionUID = 1L;

	private int index;

    private String var;

    private RangeQueryOperator operator;

    private Element coordinate;

    public RangeQueryCondition(int index, String var, RangeQueryOperator operator, Element coordinate) {
        super();
        this.var = var;
        this.operator = operator;
        this.coordinate = coordinate;
    }

    public int getIndex() {
        return this.index;
    }

    /**
     * @return the var
     */
    public String getVar() {
        return this.var;
    }

    /**
     * @return the operator
     */
    public RangeQueryOperator getOperator() {
        return this.operator;
    }

    /**
     * @return the constant
     */
    public Element getCoordinate() {
        return this.coordinate;
    }

    public String toString() {
        return "?" + this.var + " " + this.operator + " " + this.coordinate;
    }

}
