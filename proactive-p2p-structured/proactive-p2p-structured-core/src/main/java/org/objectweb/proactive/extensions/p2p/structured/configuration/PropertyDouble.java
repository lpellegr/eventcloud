package org.objectweb.proactive.extensions.p2p.structured.configuration;

import org.objectweb.proactive.core.ProActiveRuntimeException;

/**
 * A Double property.
 * 
 * @author lpellegr
 */
public class PropertyDouble extends Property {

    public PropertyDouble(String name) {
        super(name, PropertyType.DOUBLE);
    }

    public PropertyDouble(String name, double defaultValue) {
        this(name);
        this.setDefaultValue(new Double(defaultValue).toString());
    }

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property.
     */
    public double getValue() {
        String str = super.getValueAsString();
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new ProActiveRuntimeException("Invalid value for property "
                    + super.name + " must be a double", e);
        }
    }

    /**
     * Updates the value of this property.
     * 
     * @param value
     *            the new value.
     */
    public void setValue(double value) {
        super.setValue(new Double(value).toString());
    }

    @Override
    public boolean isValid(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
