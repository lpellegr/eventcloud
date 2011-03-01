package fr.inria.eventcloud.reasoner.visitor;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import fr.inria.eventcloud.api.messages.request.SparqlQuery;
import fr.inria.eventcloud.messages.request.can.SemanticRequest;
import fr.inria.eventcloud.messages.request.can.TriplePatternRequest;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.reasoner.RangeQueryCondition;
import fr.inria.eventcloud.reasoner.RangeQueryOperator;

/**
 * Try to identify Sparql query type by using Sesame Visitor pattern.
 * 
 * @author lpellegr
 */
public class QueryIdentifierVisitor extends QueryModelVisitorBase<RuntimeException> {

    private boolean containsFilter = false;

    private boolean containsOrder = false;

    private boolean constainsEqualsComparator = false;

    private boolean containsNotEqualsComparator = false;

    private int numberOfCompareGT = 0;

    private int numberOfCompareGE = 0;
    
    private int numberOfCompareLT = 0;
    
    private int numberOfCompareLE = 0;
    
    private int numberOfAnd = 0;

    private int numberOfOr = 0;

    private List<String> constantValues = new ArrayList<String>();
    
    private List<StatementPattern> statementPatterns = new ArrayList<StatementPattern>();

    private List<RangeQueryCondition> rangeQueryConditions = new ArrayList<RangeQueryCondition>();

    private List<String> bindingNames = new ArrayList<String>();

    public void meet(Filter node) throws RuntimeException {
        this.containsFilter = true;
        super.meet(node);
    }

    public void meet(Order node) throws RuntimeException {
        this.containsOrder = true;
        super.meet(node);
    }

    public void meet(And node) throws RuntimeException {
        this.numberOfAnd++;
        super.meet(node);
    }

    public void meet(Or node) throws RuntimeException {
        this.numberOfOr++;
        super.meet(node);
    }

    public void meet(Compare node) throws RuntimeException {
        if (!this.constainsEqualsComparator) {
            if (node.getOperator() == CompareOp.EQ) {
                this.constainsEqualsComparator = true;
            }
        }

        if (!this.containsNotEqualsComparator) {
            if (node.getOperator() == CompareOp.NE) {
                this.containsNotEqualsComparator = true;
            }
        }

        if (node.getOperator() == CompareOp.LT || 
        	node.getOperator() == CompareOp.LE || 
        	node.getOperator() == CompareOp.GT || 
        	node.getOperator() == CompareOp.GE) {
            
        	String bindingName = ((Var) ((Str) node.getLeftArg()).getArg()).getName();
        	int bindingNameIndex = 
        		this.bindingNames.indexOf(bindingName);

        	switch (node.getOperator()) {
        	case LT:
        		this.numberOfCompareLT++;
        		this.rangeQueryConditions.add(
        				new RangeQueryCondition(
        						bindingNameIndex, 
        						bindingName, 
        						RangeQueryOperator.LESS, 
        						this.createSemanticCoordinateValueFrom(
        								((ValueConstant) node.getRightArg()).getValue().stringValue())));
        		break;
        	case GE:
        		this.numberOfCompareGE++;
        		this.rangeQueryConditions.add(
        				new RangeQueryCondition(
        						bindingNameIndex, 
        						bindingName, 
        						RangeQueryOperator.GREATER_EQUALS, 
        						this.createSemanticCoordinateValueFrom(
        								((ValueConstant) node.getRightArg()).getValue().stringValue())));
        		break;
        	case LE:
        		this.numberOfCompareLE++;
        		break;
        	case GT:
        		this.numberOfCompareGT++;
        		break;
        	}
        }

        super.meet(node);
    }

    public void meet(StatementPattern node) throws RuntimeException {
    	this.statementPatterns.add(node);
    	
    	int index = 0;
    	for (String bindingName : node.getBindingNames()) {
			this.bindingNames.add(index, bindingName);
			index++;
    	}
    }
    
    public void meet(ValueConstant node) throws RuntimeException {
        this.constantValues.add(node.getValue().stringValue());
        super.meet(node);
    }

    public boolean constantValuesStartAllBy(String prefix) {
        for (String constantValue : this.constantValues) {
            if (!constantValue.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    public void printInformation() {
        System.out.println("containsFilter=" + this.containsFilter);
        System.out.println("containsOrder=" + this.containsOrder);
        System.out.println("containsEqualsComparator=" + this.constainsEqualsComparator);
        System.out.println("containsNotEqualsComparator=" + this.containsNotEqualsComparator);

        System.out.println("BindingNames:");
        for (String name : this.bindingNames) {
        	System.out.println(name);
        }
        
        System.out.println("RangeQuery conditions:");
        for (RangeQueryCondition condition : this.rangeQueryConditions) {
        	System.out.println(condition);
        }
        
        System.out.println("numberOfAnd=" + this.numberOfAnd);
        System.out.println("numberOfOr=" + this.numberOfOr);
    }

    public SemanticRequest processIdentificationAndCreateQueryMessage(SparqlQuery query) {
    	if (this.statementPatterns.size() > 1) {
    		throw new IllegalStateException("query to identify is supposed to be already decomposed if necessary");
    	}
    	
//    	if (this.containsFilter && 
//    		!this.containsOrder && 
//    		this.statementPatterns.size() == 1 && 
//    		(this.numberOfAnd + this.numberOfOr <= 5) && 
//    		this.numberOfCompareGT == 0 &&
//    		this.numberOfCompareLE == 0 &&
//    		!this.constainsEqualsComparator && 
//    		!this.containsNotEqualsComparator) {
//        	
//            return new RangeQueryMessage(query, this.rangeQueryConditions);
//    	} else {
    	
    	// TODO implement range queries...
    	
            return new TriplePatternRequest(query, new Coordinate(
            		this.createSemanticCoordinateValueFrom(
            				toString(this.statementPatterns.get(0).getSubjectVar())),
            		this.createSemanticCoordinateValueFrom(
            				toString(this.statementPatterns.get(0).getPredicateVar())),
            		this.createSemanticCoordinateValueFrom(
            				toString(this.statementPatterns.get(0).getObjectVar()))));
//        }
    }

	private static String toString(Var var) {
		if (var.getName().startsWith("-anon-")) {
			// Blank node
			return "_:" + var.getName().replaceAll("-", "");
		} else if (var.getValue() == null) {
			// Variable
			return null;
		} else if (var.getValue().toString().startsWith("\"")) {
			// Literal
			return var.getValue().toString();
		} else {
			// IRI
			return var.getValue().stringValue();
		}
	}

    private SemanticElement createSemanticCoordinateValueFrom(String value) {
    	if (value == null) {
    		return null;
    	} else {
    		return new SemanticElement(value);
    	}
    }

	public boolean containsFilter() {
		return this.containsFilter;
	}

	public boolean containsOrder() {
		return this.containsOrder;
	}

	public boolean constainsEqualsComparator() {
		return this.constainsEqualsComparator;
	}

	public boolean containsNotEqualsComparator() {
		return this.containsNotEqualsComparator;
	}

	public int getNumberOfCompareGT() {
		return this.numberOfCompareGT;
	}

	public int getNumberOfCompareGE() {
		return this.numberOfCompareGE;
	}

	public int getNumberOfCompareLT() {
		return this.numberOfCompareLT;
	}

	public int getNumberOfCompareLE() {
		return this.numberOfCompareLE;
	}

	public int getNumberOfAnd() {
		return this.numberOfAnd;
	}

	public int getNumberOfOr() {
		return this.numberOfOr;
	}

	public List<String> getConstantValues() {
		return this.constantValues;
	}

	public List<StatementPattern> getStatementPatterns() {
		return this.statementPatterns;
	}

	public List<RangeQueryCondition> getRangeQueryConditions() {
		return this.rangeQueryConditions;
	}

	public List<String> getBindingNames() {
		return this.bindingNames;
	}
  
}
