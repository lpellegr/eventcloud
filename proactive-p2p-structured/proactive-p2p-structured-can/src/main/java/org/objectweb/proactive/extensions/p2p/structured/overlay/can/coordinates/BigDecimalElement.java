package org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates;

import java.math.BigDecimal;

/**
 * Represents a BigDecimal coordinate element.
 * 
 * @author Laurent Pellegrino
 */
public class BigDecimalElement extends Element {

	private static final long serialVersionUID = 1L;

	/**
	 * The value associated to this coordinate element.
	 */
	private final BigDecimal value;
	
	/**
	 * Constructs a new coordinate element with the specified
	 * <code>value</code>.
	 * 
	 * @param value the String that will be parsed as BigDecimal.
	 */
	public BigDecimalElement(String value) {
		this.value = new BigDecimal(value);
	}
	
	/**
	 * Constructs a new coordinate element with the specified
	 * <code>value</code>.
	 * 
	 * @param value the BigDecimal value to set.
	 */
	public BigDecimalElement(BigDecimal value) {
		this.value = value;
	}
	
	/**
	 * Returns the value associated to this element.
	 * 
	 * @return the value associated to this element.
	 */
	public BigDecimal getValue() {
		return this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Element computeMiddle(Element e) {
		return new BigDecimalElement(
				this.value.add(
						((BigDecimalElement) e).getValue())
						.divide(BigDecimal.valueOf(2)));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Element elt) {
		return this.value.compareTo(
					((BigDecimalElement) elt).getValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.value.toPlainString();
	}

}
