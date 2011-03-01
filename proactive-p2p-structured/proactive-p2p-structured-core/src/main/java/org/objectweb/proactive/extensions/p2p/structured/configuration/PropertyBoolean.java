package org.objectweb.proactive.extensions.p2p.structured.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Boolean property.
 * 
 * @author lpellegr
 *
 * @version $Id: PropertyBoolean.java 5079 2010-10-11 11:17:01Z plaurent $
 */
public class PropertyBoolean extends Property {
    
	static final private Logger logger = LoggerFactory.getLogger(PropertyBoolean.class);

    public static final String TRUE = "true";
    
    public static final String FALSE = "false";
    
    public PropertyBoolean(String name) {
        super(name, PropertyType.BOOLEAN);
    }
    
    public PropertyBoolean(String name, boolean defaultValue) {
        this(name);
        this.setDefaultValue(new Boolean(defaultValue).toString());
    }

    /**
	 * Returns the value of this property.
	 * 
	 * @return the value of this property.
	 */
    public boolean getValue() {
        String str = super.getValueAsString();
        return Boolean.parseBoolean(str);
    }

	/**
	 * Updates the value of this property.
	 * 
	 * @param value
	 *            the new value.
	 */
    public void setValue(boolean value) {
        super.setValue(new Boolean(value).toString());
    }

	/**
	 * Indicates if this property is true.
	 * 
	 * This method can only be called with boolean property. Otherwise an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * If the value is illegal for a boolean property, then false is returned
	 * and a warning is printed.
	 * 
	 * @return <code>true</code> if the property is set to true.
	 */
    public boolean isTrue() {
        String val = super.getValueAsString();
        if (TRUE.equals(val)) {
            return true;
        }
        if (FALSE.equals(val)) {
            return false;
        }

        logger.warn(this.name + " is a boolean property but its value is nor " + TRUE + " nor " + FALSE +
            " " + "(" + val + "). ");
        return false;
    }

    @Override
    public boolean isValid(String value) {
        if (TRUE.equals(value) || FALSE.equals(value))
            return true;

        return false;
    }

}
