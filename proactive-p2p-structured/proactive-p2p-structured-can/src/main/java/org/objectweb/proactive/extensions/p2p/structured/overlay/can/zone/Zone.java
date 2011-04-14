package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import static org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory.createDoubleCoordinate;
import static org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory.createStringCoordinate;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.DoubleCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.DoubleElement;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.util.Pair;


/**
 * A zone defines a space (rectangle) which is completely logical and managed by
 * an {@link AbstractCanOverlay}. The coordinates of this rectangle are maintained 
 * in a {@link ZoneView}. By default a zone contain two views. The first one is a 
 * {@link NumericZoneView} which uses respectively {@code 0.0} and {@code 1.0} as
 * the lower and upper bound. The second is an {@link UnicodeZoneView} which uses
 * respectively {@link P2PStructuredProperties#CAN_LOWER_BOUND} and 
 * {@link P2PStructuredProperties#CAN_UPPER_BOUND} as the lower and upper bound.
 * <p>
 * The former is used to compute a distance or an area whereas the latter is used 
 * in order to index the data by using a lexicographic order.
 * <p>
 * <strong>By default all operations are delegated to the {@link UnicodeZoneView}.</strong>
 * 
 * @author lpellegr
 */
public class Zone implements Serializable {

	private static final long serialVersionUID = 1L;

	private final UnicodeZoneView unicodeView;
	
	private final NumericZoneView numView;
	
	/**
	 * Creates a new Zone with the specified {@code unicodeView} and {@code numView}.
	 * 
	 * @param unicodeView the {@link UnicodeZoneView} to set.
	 * @param numView the {@link NumericZoneView} to set.
	 */
	public Zone(UnicodeZoneView unicodeView, NumericZoneView numView) {
	    this.unicodeView = unicodeView;
	    this.numView = numView;
	}
	
	/**
	 * Constructs a new zone by initializing the views with 
	 * their default values.
	 */
	public Zone() {
	    this(new UnicodeZoneView(
                createStringCoordinate(P2PStructuredProperties.CAN_LOWER_BOUND.getValue()), 
                createStringCoordinate(P2PStructuredProperties.CAN_UPPER_BOUND.getValue())),
             new NumericZoneView(
                createDoubleCoordinate(0.0),
                createDoubleCoordinate(1.0)));
	}

	public Pair<Zone> split(int dimension) {
	    Pair<ZoneView<StringCoordinate, StringElement, String>> newUnicodeViews = this.unicodeView.split(dimension);
	    Pair<ZoneView<DoubleCoordinate, DoubleElement, Double>> newNumViews = this.numView.split(dimension);
	    
	    return new Pair<Zone>(
	                new Zone(
	                        (UnicodeZoneView) newUnicodeViews.getFirst(), 
	                        (NumericZoneView) newNumViews.getFirst()),
	                new Zone(
	                        (UnicodeZoneView) newUnicodeViews.getSecond(), 
	                        (NumericZoneView) newNumViews.getSecond()));
	}

	public Zone merge(Zone zone) {
	    return new Zone((UnicodeZoneView) 
	                        this.unicodeView.merge(zone.getUnicodeView()),
	                    (NumericZoneView) 
	                        this.numView.merge(zone.getNumericView()));
	}
	
    public UnicodeZoneView getUnicodeView() {
        return this.unicodeView;
    }

    public NumericZoneView getNumericView() {
        return this.numView;
    }

    public short contains(int dimension, StringElement element) {
        return this.unicodeView.contains(dimension, element);
    }
    
    public boolean contains(StringCoordinate coordinate) {
        return this.unicodeView.contains(coordinate);
    }

    public boolean overlaps(ZoneView<StringCoordinate, StringElement, String> view, int dimension) {
        return this.unicodeView.overlaps(view, dimension);
    }

    public boolean overlaps(ZoneView<StringCoordinate, StringElement, String> view) {
        return this.unicodeView.overlaps(view);
    }

    public boolean abuts(ZoneView<StringCoordinate, StringElement, String> view, int dimension, boolean direction) {
        return this.unicodeView.abuts(view, dimension, direction);
    }

    public int neighbors(Zone zone) {
        return this.unicodeView.neighbors(zone.getUnicodeView());
    }

    public StringCoordinate getUpperBound() {
        return this.unicodeView.getUpperBound();
    }

    public StringCoordinate getLowerBound() {
        return this.unicodeView.getLowerBound();
    }

    public StringElement getLowerBound(int dimension) {
        return this.unicodeView.getLowerBound(dimension);
    }

    public StringElement getUpperBound(int dimension) {
        return this.unicodeView.getUpperBound(dimension);
    }

    public String toString() {
        return this.unicodeView.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Zone
                && this.unicodeView.equals(((Zone) obj).getUnicodeView())
                    && this.numView.equals(((Zone) obj).getNumericView());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31 * (31 + this.numView.hashCode()) + this.unicodeView.hashCode();
    }

}
