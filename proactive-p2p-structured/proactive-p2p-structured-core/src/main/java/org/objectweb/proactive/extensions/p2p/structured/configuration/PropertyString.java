package org.objectweb.proactive.extensions.p2p.structured.configuration;

/**
 * A String property.
 * 
 * @author Laurent Pellegrino
 */
public class PropertyString extends Property {
	
	public PropertyString(String name) {
		super(name, PropertyType.STRING);
	}

	public PropertyString(String name, String defaultValue) {
		this(name);
		this.setDefaultValue(defaultValue);
	}

	/**
	 * Returns the value of this property.
	 * 
	 * @return the value of this property.
	 */
	public String getValue() {
		return super.getValueAsString();
	}

	/**
	 * Updates the value of this property.
	 * 
	 * @param value
	 *            the new value.
	 */
	public void setValue(String value) {
		super.setValue(value);
	}

	@Override
	public boolean isValid(String value) {
		return value != null;
	}
	
}
