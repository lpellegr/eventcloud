package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements;


/**
 * Embodies a double coordinate element.
 * 
 * @author lpellegr
 */
public class DoubleElement extends Element<Double> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new coordinate element with the specified <code>value</code>.
	 * 
	 * @param value
	 *            the String that will be parsed as a Double.
	 */
	public DoubleElement(String value) {
		super(Double.valueOf(value));
	}

	/**
	 * Constructs a new coordinate element with the specified <code>value</code>.
	 * 
	 * @param value
	 *            the Double value to set.
	 */
	public DoubleElement(Double value) {
		super(value);
	}

	/**
	 * Returns the value associated to this element.
	 * 
	 * @return the value associated to this element.
	 */
	public Double getValue() {
		return this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Element<Double> middle(Element<Double> elt) {
		return new DoubleElement(
				(this.value + ((DoubleElement) elt).getValue()) / 2.0);
	}

}
