package org.objectweb.proactive.extensions.p2p.structured.configuration;

import org.objectweb.proactive.core.ProActiveRuntimeException;

/**
 * An Integer property.
 * 
 * @author Laurent Pellegrino
 */
public class PropertyInteger extends Property {
	
	public PropertyInteger(String name) {
		super(name, PropertyType.INTEGER);
	}

	public PropertyInteger(String name, int defaultValue) {
		this(name);
		this.setDefaultValue(new Integer(defaultValue).toString());
	}

	/**
	 * Returns the value of this property.
	 * 
	 * @return the value of this property.
	 */
	public int getValue() {
		String str = super.getValueAsString();
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			throw new ProActiveRuntimeException(
					"Invalid value for property " + super.name
							+ " must be an integer", e);
		}
	}

	/**
	 * Updates the value of this property.
	 * 
	 * @param value
	 *            the new value.
	 */
	public void setValue(int value) {
		super.setValue(new Integer(value).toString());
	}

	@Override
	public boolean isValid(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
}
