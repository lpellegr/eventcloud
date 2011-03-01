package fr.inria.eventcloud.reasoner;

/**
 * @author lpellegr
 */
public enum RangeQueryOperator {
	AND("&&"), 
	OR("||"), 
	EQUALS("=="), 
	GREATER(">"), 
	GREATER_EQUALS(">="), 
	LESS("<"), 
	LESS_EQUALS("<="), 
	NOT_EQUALS("!=");
	
	private final String value;
	
	RangeQueryOperator(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
	
    public static RangeQueryOperator getType(String operator) {
        if (operator.equals("&&")) {
            return AND;
        } else if (operator.equals("||")) {
            return OR;
        } else if (operator.equals("==")) {
            return EQUALS;
        } else if (operator.equals(">")) {
            return GREATER;
        } else if (operator.equals(">=")) {
            return GREATER_EQUALS;
        } else if (operator.equals("<")) {
            return LESS;
        } else if (operator.equals("<=")) {
            return LESS_EQUALS;
        } else if (operator.equals("!=")) {
            return NOT_EQUALS;
        }
        return null;
    }

}
