package org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates;

import java.io.Serializable;


/**
 * An element represents one component from a {@link Coordinate}.
 * 
 * @author lpellegr
 */
public abstract class Element implements Comparable<Element>, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Computes and returns a new {@link Element} with a value 
	 * being the middle of the current element and the specified element
	 * <code>elt</code>.
	 * 
	 * @param elt the element to compute with.
	 * 
	 * @return a new {@link Element} with a value 
	 * 		   being the middle of the current element 
	 * 		   the specified element <code>elt</code>.
	 */
	public abstract Element computeMiddle(Element elt);

	/**
	 * Returns a boolean indicating if the current element is
	 * between respectively the specified elements <code>e1</code>
	 * and <code>e2</code>.
	 * 
	 * @param e1 the first bound.
	 * 
	 * @param e2 the second bound.
	 * 
	 * @return <code>true</code> whether <code>e1<0 and this in [e1;e2[</code>
	 * 		   or  <code>e1 > e2 and this in [e2;e1[</code>, <code>false</code>
	 * 		   otherwise.
	 */
	public boolean isBetween(Element e1, Element e2) {
		if (e1.compareTo(e2) < 0) {
			return (this.compareTo(e1) >= 0) && (this.compareTo(e2) < 0);
		} else if (e1.compareTo(e2) > 0) {
			return (this.compareTo(e2) >= 0) && (this.compareTo(e1) < 0);
		}
		return false;
	}

	/**
	 * Computes and returns a new {@link Element} with a value 
	 * being the middle of the specified elements
	 * <code>e1</code> and <code>e2</code>.
	 * 
	 * @param <T> the type of {@link Element} to use.
	 * 
	 * @param e1 the lower bound.
	 * 
	 * @param e2 the upper bound.
	 * 
	 * @return a new {@link Element} with a value being the middle 
	 * 		   of the specified elements <code>e1</code> and
	 * 		   <code>e2</code>.
	 * 
	 * @see Element#computeMiddle(Element)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Element> T middle(T e1, T e2) {
		return (T) e1.computeMiddle(e2);
	}

	/**
	 * Returns the maximum among the specified coordinate elements.
	 * 
	 * @param elt1
	 *            first element.
	 * @param elt2
	 *            second element.
	 *            
	 * @return the maximum among the specified coordinate elements 
	 * 		   using {@link Element#compareTo(Element)}.
	 */
	public static Element max(Element elt1, Element elt2) {
		if (elt1.compareTo(elt2) > 0) {
			return elt1;
		} else {
			return elt2;
		}
	}

	/**
	 * Returns the minimum among the specified coordinate elements.
	 * 
	 * @param elt1
	 *            first element.
	 * @param elt2
	 *            second element.
	 *            
	 * @return the minimum among the specified coordinate elements 
	 * 		   using {@link Element#compareTo(Element)}.
	 */
	public static Element min(Element elt1, Element elt2) {
		if (elt1.compareTo(elt2) < 0) {
			return elt1;
		} else {
			return elt2;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		return this.getClass().equals(obj.getClass())
		&& this.compareTo((Element) obj) == 0;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract int compareTo(Element e);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract int hashCode();

	/**
	 * {@inheritDoc}
	 */
	public abstract String toString();
	
}
