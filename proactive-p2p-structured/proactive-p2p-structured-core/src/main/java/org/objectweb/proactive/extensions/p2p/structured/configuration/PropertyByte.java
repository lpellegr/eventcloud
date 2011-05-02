package org.objectweb.proactive.extensions.p2p.structured.configuration;

import org.objectweb.proactive.core.ProActiveRuntimeException;

/**
 * A Byte property.
 * 
 * @author lpellegr
 */
public class PropertyByte extends Property {

    public PropertyByte(String name) {
        super(name, PropertyType.BYTE);
    }

    public PropertyByte(String name, byte defaultValue) {
        this(name);
        this.setDefaultValue(Byte.valueOf(defaultValue).toString());
    }

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property.
     */
    public byte getValue() {
        String str = super.getValueAsString();
        try {
            return Byte.parseByte(str);
        } catch (NumberFormatException e) {
            throw new ProActiveRuntimeException("Invalid value for property "
                    + super.name + " must be a byte", e);
        }
    }

    /**
     * Updates the value of this property.
     * 
     * @param value
     *            the new value.
     */
    public void setValue(byte value) {
        super.setValue(Byte.valueOf(value).toString());
    }

    @Override
    public boolean isValid(String value) {
        try {
            Byte.parseByte(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
