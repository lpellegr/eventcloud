package org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;

/**
 * Represents a set of elements (i.e. a set of values for each component 
 * of the coordinate) used to determine the position of a point in a {@link Zone}.
 * 
 * @author lpellegr
 * 
 * @see Coordinate
 */
public class Coordinate implements Comparable<Coordinate>, Iterable<Element>, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The set of elements composing the coordinate.
	 */
	private final Element[] values;
    
	/**
	 * Constructs a new coordinate with the specified 
	 * <code>elements</code> as coordinate elements.
	 * 
	 * @param elements the elements composing the coordinate.
	 */
    public Coordinate(Element... elements) {
        this.values = elements;
    }
    
    /**
     * Returns the {@link Element} at the given <code>index</code>.
     * 
     * @param index the index of the element to return 
     * 		  (i.e. the coordinate value on the given dimension).
     * 
     * @return the {@link Element} at the given <code>index</code>.
     */
    public Element getElement(int index) {
        return this.values[index];
    }

    /**
     * Returns the elements composing the coordinate.
     * 
     * @return the elements composing the coordinate.
     */
    public Element[] getElements() {
        return this.values;
    }

    /**
     * Returns the number of coordinate elements contained by this coordinate.
     * 
     * @return the number of coordinate elements contained by this coordinate.
     */
    public int size() {
    	return this.values.length;
    }
    
    /**
     * Sets the specified index to the given value <code>elt</code>.
     * 
     * @param index the element index to edit (i.e. the dimension).
     * 
     * @param elt the new element to set.
     */
    public void setElement(int index, Element elt) {
    	this.values[index] = elt;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        
        for (int i=0; i<this.values.length; i++) {
            result.append(this.values[i]);
            if (i != this.values.length - 1) {
                result.append(",");
            }
        }
        
        return result.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Element> iterator() {
    	return Arrays.asList(this.values).iterator();
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public int compareTo(Coordinate c) {
		if (this.size() != c.size()) {
			return -1;
		}
		
		for (int i=0; i<this.size(); i++) {
			if (this.values[i].compareTo(c.getElement(i)) != 0) {
				return -1;
			}
		}
		
		return 0;
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.values);
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Coordinate
				&& this.compareTo((Coordinate) obj) == 0;
	}
    
}
