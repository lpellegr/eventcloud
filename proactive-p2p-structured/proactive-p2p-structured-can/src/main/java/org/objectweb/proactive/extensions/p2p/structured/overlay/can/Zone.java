package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.core.util.converter.MakeDeepCopy;
import org.objectweb.proactive.extensions.p2p.structured.configuration.DefaultProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Element;

/**
 * An zone defines a space (rectangle) which is completely logical and managed
 * by an {@link AbstractCanOverlay}. A zone is composed of two coordinates: a
 * lower bound coordinate which corresponds to the lower left corner and a upper 
 * bound which corresponds to the upper right corner.
 * 
 * @author lpellegr
 */
public class Zone implements Serializable {

    private static final long serialVersionUID = 1L;

    private Coordinate upperBound;

    private Coordinate lowerBound;

    /**
     * Constructs a new zone by using the {@link DefaultProperties#CAN_LOWER_BOUND} and 
     * {@link DefaultProperties#CAN_UPPER_BOUND} as coordinate elements.
     */
    public Zone() {
        this.lowerBound = this.createCoordinate(DefaultProperties.CAN_LOWER_BOUND.getValue());
        this.upperBound = this.createCoordinate(DefaultProperties.CAN_UPPER_BOUND.getValue());
    }
    
    /**
     * Constructs a new zone with the specified lower and upper bounds.
     * 
     * @param lowerBound
     *            the minimum coordinates.
     * @param upperBound
     *            the maximum coordinates. 
     */
    public Zone(Coordinate lowerBound, Coordinate upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }
    
    /**
     * Creates a new coordinate where each coordinate elements have
     * the specified <code>value</code> and a type from
     * {@link DefaultProperties#CAN_COORDINATE_TYPE}.
     * 
     * @param value the default value for each coordinate element.
     * 
     * @return a new coordinate with elements of type {@link DefaultProperties#CAN_COORDINATE_TYPE}.
     */
    private Coordinate createCoordinate(String value) {
    	Element[] elts = new Element[DefaultProperties.CAN_NB_DIMENSIONS.getValue()];
    	for (int i=0; i<DefaultProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
    		elts[i] = this.createElement(value);
    	}
    	return new Coordinate(elts);
    }
    
    /**
     * Creates a new {@link CoordinateValue} by using the concrete type
     * specified in {@link DefaultProperties#CAN_COORDINATE_TYPE}.
     * 
     * @param value the coordinate value value.
     * 
     * @return a new {@link CoordinateValue} according to the concrete type
     *         specified in {@link DefaultProperties#CAN_COORDINATE_TYPE}.
     */
    private Element createElement(String value) {
        try {
            return (Element) Class.forName(
                            DefaultProperties.CAN_COORDINATE_TYPE.getValueAsString())
                                .getConstructor(String.class).newInstance(value);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        throw new IllegalStateException("A new coordinate element cannot be created for value: " + value);
    }

    /**
     * Check if the specified <code>coordinate</code> is in the zone managed.
     * 
     * @param coordinate
     *            the coordinate to check.
     * 
     * @return <code>true</code> if the coordinate is in the zone managed,
     *         <code>false</code> otherwise.
     */
    public boolean contains(Coordinate coordinate) {
        for (int dim = 0; dim < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            if (this.contains(dim, coordinate.getElement(dim)) != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Indicates if the zone contains the specified coordinate on
     * the specified <code>dimension</code>.
     * <p>
     * A null coordinate is contained by all zones. This semantic 
     * is used to construct systems in which queries can reach 
     * several peers.
     * 
     * @param dimension
     *            the dimension.
     *            
     * @param coordinate
     *            the coordinate to check.
     * 
     * @return <code>0</code> if the specified coordinate is in the zone,
     *         <code>-1</code> if the coordinate is taller than the lower
     *         bound of the zone and <code>1</code> if the coordinate is
     *         greater or equal to the upper bound of the zone.
     */
    public short contains(int dimension, Element coordinate) {
        if (coordinate == null) {
            return 0;
        }
        
        if (coordinate.compareTo(this.upperBound.getElement(dimension)) >= 0) {
            return 1;
        } else if (coordinate.compareTo(this.lowerBound.getElement(dimension)) < 0) {
            return -1;
        }

        return 0;
    }
    
    /**
     * Returns a boolean indication whether the given <code>zone</code> overlaps 
     * the current zone along the given axis.
     *
     * @param zone 
     *              the zone to compare with.
     * 
     * @param dimension 
     *              the dimension on which the check is performed.
     * 
     * @return <code>true</code> if the specified zone overlap the current zone, 
     *         <code>false</code> otherwise.
     */
    public boolean overlaps(Zone zone, int dimension) {
        Element a = this.lowerBound.getElement(dimension);
        Element b = this.upperBound.getElement(dimension);
        Element c = zone.getLowerBound(dimension);
        Element d = zone.getUpperBound(dimension);

        return (((a.compareTo(c) >= 0) && (a.compareTo(d) < 0)) 
                    || ((b.compareTo(c) > 0) && (b.compareTo(d) <= 0))
                    || ((c.compareTo(a) >= 0) && (c.compareTo(b) < 0)) 
                    || ((d.compareTo(a) > 0) && (d.compareTo(b) <= 0)));
    }

    /**
     * Returns a boolean indicating whether the given <code>zone</code> overlaps 
     * the current zone. The specified <code>zone</code> must overlap on all 
     * dimensions.
     *
     * @param zone 
     *              the zone to compare with.
     *              
     * @return <code>true</code> if the specified zone overlap the current zone
     *         on all dimensions, <code>false</code> otherwise.
     */
    public boolean overlaps(Zone zone) {
        for (int i = 0; i < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            if (this.overlaps(zone, i) == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether the given <code>zone</code> abuts the current zone in 
     * the given direction and dimension.
     *
     * @param zone 
     *              the zone to compare with.
     * 
     * @param dimension 
     *              the dimension on which the check is performed.
     * 
     * @param low 
     *              indicates the direction on which the check is performed: 
     *              <code>false</code> is the inferior direction and 
     *              <code>true</code> is the superior direction.
     *              
     * @return a boolean indicating if the specified <code>zone</code> abuts 
     *         the current zone.
     */
    public boolean abuts(Zone zone, int dimension, boolean low) {
        return (low && 
                    (this.lowerBound.getElement(dimension).compareTo(
                            zone.getUpperBound(dimension)) == 0)) 
               || (!low && (this.upperBound.getElement(dimension).compareTo(
                                zone.getLowerBound(dimension)) == 0));
    }
    
    /**
     * Returns the dimension on which the given <code>zone</code> neighbors 
     * the current zone. The result is the dimension number or <code>-1</code> 
     * if the zone does not neighbor the current zone.
     * <p>
     * In a d-dimensional space, two zones are neighbors if their edges overlap 
     * in exactly <code>d-1</code> dimensions and abut in exactly <code>1</code> 
     * dimension.
     *
     * @param zone 
     *              the zone to compare with.
     * 
     * @return the dimension on which the given <code>zone</code> neighbors 
     *         the current zone.
     */
    public int neighbors(Zone zone) {
        int overlaps = 0;
        int abuts = 0;
        int abutsDimension = -1;

        for (int dimension = 0; dimension < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            if (this.overlaps(zone, dimension)) {
                overlaps++;
            } else {
                if (this.abuts(zone, dimension, true)
                        || this.abuts(zone, dimension, false)) {
                    abutsDimension = dimension;
                    abuts++;
                } else {
                    return -1;
                }
            }
        }

        if ((abuts != 1) || (overlaps != DefaultProperties.CAN_NB_DIMENSIONS.getValue() - 1)) {
            return -1;
        } else {
            return abutsDimension;
        }
    }

    /**
     * Merges the current zone with the specified <code>zone</code>.
     * 
     * @param zone
     *            the zone to merge with.
     *            
     * @return a new zone which is the merge between the current zone and the specified zone.
     * 
     * @throws ZoneException if zones to merge are not abuts.
     */
    public Zone merge(Zone zone) throws ZoneException {
        int abutsDimension = this.neighbors(zone);

        if (abutsDimension == -1) {
            throw new ZoneException("Zones to merge are not abuts.");
        }

        Coordinate lowerBound = null;
        Coordinate upperBound = null;
        
        try {
            lowerBound = (Coordinate) 
                MakeDeepCopy.WithObjectStream.makeDeepCopy(this.lowerBound);
            upperBound = (Coordinate) 
                MakeDeepCopy.WithObjectStream.makeDeepCopy(this.upperBound);
            
            lowerBound.setElement(abutsDimension,
            		(Element) MakeDeepCopy.WithObjectStream.makeDeepCopy(
            				Element.min(
            						this.getLowerBound(abutsDimension), 
            						zone.getLowerBound(abutsDimension))));
            upperBound.setElement(abutsDimension, 
            		(Element) MakeDeepCopy.WithObjectStream.makeDeepCopy(
            				Element.max(
            						this.getUpperBound(abutsDimension), 
            						zone.getUpperBound(abutsDimension))));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        

        return new Zone(lowerBound, upperBound);
    }

    /**
     * Returns two new zones representing the original one split into two,
     * following the specified <code>dimension</code>.
     * 
     * @param dimension
     *            the dimension to split into.
     *            
     * @return two new zones representing the original one split into two,
     *         following the specified <code>dimension</code>.
     *         
     * @throws ZoneException
     * 
     * @see Zone#split(int, Element)
     */
    public Zone[] split(int dimension) throws ZoneException {
        return this.split(
                    dimension, 
                    Element.middle(
                    		this.getLowerBound(dimension), 
                    		this.getUpperBound(dimension)));
    }

    /**
     * Returns two zones representing the original one split into two following
     * the specified <code>dimension</code> at the specified
     * <code>coordinateComponent</code>.
     * 
     * @param dimension
     *            the dimension.
     * @param coordinateComponent
     *            the coordinate component.
     * @return two zones representing the original one split into two following
     *         the specified dimension at the specified coordinate.
     * @throws ZoneException
     */
    public Zone[] split(int dimension, Element coordinateComponent) throws ZoneException {
        Coordinate upperBoundCopy = null;
        Coordinate lowerBoundCopy = null;

        try {
            upperBoundCopy = (Coordinate) 
                MakeDeepCopy.WithObjectStream.makeDeepCopy(this.getUpperBound());
            lowerBoundCopy = (Coordinate) 
                MakeDeepCopy.WithObjectStream.makeDeepCopy(this.getLowerBound());

            upperBoundCopy.setElement(dimension, 
            		(Element) MakeDeepCopy.WithObjectStream.makeDeepCopy(
            				coordinateComponent));
            lowerBoundCopy.setElement(dimension, 
            		(Element) MakeDeepCopy.WithObjectStream.makeDeepCopy(
            				coordinateComponent));
        
            return new Zone[] { 
                    new Zone(
                    		(Coordinate) MakeDeepCopy.WithObjectStream
                    						.makeDeepCopy(this.getLowerBound()), upperBoundCopy),
                    new Zone(lowerBoundCopy, 
                    		(Coordinate) MakeDeepCopy.WithObjectStream
                    						.makeDeepCopy(this.getUpperBound())) };
        } catch (IOException e) {
        	e.printStackTrace();
        } catch (ClassNotFoundException e) {
        	e.printStackTrace();
        }
        
        throw new ZoneException("An error occured while splitting the zone.");
    }

    /**
     * Returns the value from the upper bound associated to the 
     * specified <code>dimension</code>.
     * 
     * @param dimension
     *            the dimension on which the value is retrieved.
     *            
     * @return the value from the upper bound associated to the 
     *         specified <code>dimension</code>.
     */
    public Element getUpperBound(int dimension) {
        return this.upperBound.getElement(dimension);
    }

    /**
     * Returns the value from the lower bound associated to the 
     * specified <code>dimension</code>.
     * 
     * @param dimension
     *            the dimension on which the value is retrieved.
     *            
     * @return the value from the lower bound associated to the 
     *         specified <code>dimension</code>.
     */
    public Element getLowerBound(int dimension) {
        return this.lowerBound.getElement(dimension);
    }

    /**
     * Returns the bounds of the zone.
     * 
     * @return the bounds of the zone.
     */
    public Coordinate[] getBounds() {
        return new Coordinate[] { this.lowerBound, this.upperBound };
    }

    /**
     * Returns the upper bound from this zone.
     * 
     * @return the upper bound from this zone.
     */
    public Coordinate getUpperBound() {
        return this.upperBound;
    }

    /**
     * Returns the lower bound from this zone.
     * 
     * @return the lower bound from this zone.
     */
    public Coordinate getLowerBound() {
        return this.lowerBound;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
		int result = 1;
		result = 31 * result
				+ ((this.lowerBound == null) ? 0 : this.lowerBound.hashCode());
		result = 31 * result
				+ ((this.upperBound == null) ? 0 : this.upperBound.hashCode());
		return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Zone)) {
            return false;
        }

        Zone zone = (Zone) o;

        int i;
        int nbDim = this.upperBound.size();

        for (i = 0; i < nbDim; i++) {
            if (!this.getUpperBound(i).equals(zone.getUpperBound(i))
                    || !this.getLowerBound(i).equals(zone.getLowerBound(i))) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        buf.append(this.lowerBound);
        buf.append(") to (");
        buf.append(this.upperBound);
        buf.append(")");
        return buf.toString();
    }

}
