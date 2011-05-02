package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.util.Pair;

/**
 * A ZoneView is a view associated to a {@link Zone}. It is composed of two
 * coordinates: a lower bound coordinate which corresponds to the lower left
 * corner and a upper bound which corresponds to the upper right corner.
 * 
 * @author lpellegr
 * 
 * @param <C>
 *            the {@link Coordinate} type.
 * @param <E>
 *            the {@link Element}s type contained by the coordinates.
 * @param <T>
 *            the value type associated to each element.
 */
public abstract class ZoneView<C extends Coordinate<E, T>, E extends Element<T>, T extends Comparable<T>>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final C lowerBound;

    protected final C upperBound;

    protected ZoneView(C lowerBound, C upperBound) {
        this.lowerBound = checkNotNull(lowerBound);
        this.upperBound = checkNotNull(upperBound);
    }

    /**
     * Checks if the specified {@code coordinate} is in the zone managed.
     * 
     * @param coordinate
     *            the coordinate to check.
     * 
     * @return {@code true} if the coordinate is in the zone managed,
     *         {@code false} otherwise.
     */
    public boolean contains(C coordinate) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            if (this.contains(dim, coordinate.getElement(dim)) != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Indicates if the view contains the specified {@code element} on the
     * specified {@code dimension}.
     * <p>
     * An element value set to {@code null} is supposed to be contained by any
     * view on any dimension. This semantic is used to construct systems in
     * which queries can reach several peers.
     * 
     * @param dimension
     *            the dimension.
     * 
     * @param element
     *            the coordinate to check.
     * 
     * @return {@code 0} if the specified coordinate is in the zone view,
     *         {@code -1} if the coordinate is taller than the lower bound of
     *         the zone view and {@code 1} if the coordinate is greater or equal
     *         to the upper bound of the zone view.
     */
    public byte contains(byte dimension, E element) {
        if (element == null) {
            return 0;
        }

        if (element.compareTo(this.upperBound.getElement(dimension)) >= 0) {
            return 1;
        } else if (element.compareTo(this.lowerBound.getElement(dimension)) < 0) {
            return -1;
        }

        return 0;
    }

    /**
     * Returns a boolean indication whether the given {@code zone} overlaps the
     * current zone along the given axis.
     * 
     * @param view
     *            the zone view to compare with.
     * 
     * @param dimension
     *            the dimension used to perform the check operation.
     * 
     * @return {@code true} if the specified zone overlaps the current zone,
     *         {@code false} otherwise.
     */
    public boolean overlaps(ZoneView<C, E, T> view, byte dimension) {
        E a = this.lowerBound.getElement(dimension);
        E b = this.upperBound.getElement(dimension);
        E c = view.getLowerBound(dimension);
        E d = view.getUpperBound(dimension);

        return (((a.compareTo(c) >= 0) && (a.compareTo(d) < 0))
                || ((b.compareTo(c) > 0) && (b.compareTo(d) <= 0))
                || ((c.compareTo(a) >= 0) && (c.compareTo(b) < 0)) || ((d.compareTo(a) > 0) && (d.compareTo(b) <= 0)));
    }

    /**
     * Returns a boolean indicating whether the given {@code view} overlaps the
     * current zone. The specified {@code view} must overlap on all dimensions.
     * 
     * @param view
     *            the zone view to compare with.
     * 
     * @return {@code true} if the specified zone view overlaps the current zone
     *         view on all dimensions, {@code false} otherwise.
     */
    public boolean overlaps(ZoneView<C, E, T> view) {
        for (byte i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            if (this.overlaps(view, i) == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether the given {@code view} abuts the current one in the given
     * {@code dimension} and {@code direction}.
     * 
     * @param view
     *            the zone to compare with.
     * 
     * @param dimension
     *            the dimension on which the check is performed.
     * 
     * @param direction
     *            indicates the direction on which the check is performed:
     *            {@code false} is the inferior direction and {@code true} is
     *            the superior direction.
     * 
     * @return a boolean indicating if the specified {@code view} abuts the
     *         current zone.
     */
    public boolean abuts(ZoneView<C, E, T> view, byte dimension,
                         boolean direction) {
        return (direction && (this.lowerBound.getElement(dimension).compareTo(
                view.getUpperBound(dimension)) == 0))
                || (!direction && (this.upperBound.getElement(dimension)
                        .compareTo(view.getLowerBound(dimension)) == 0));
    }

    /**
     * Returns the dimension on which the given {@code view} neighbors the
     * current one. The result is the dimension number or {@code -1} if the
     * specified view does not neighbor the current view.
     * <p>
     * In a d-dimensional space, two views are neighbors if their edges overlap
     * in exactly {@code d-1} dimensions and abut in exactly {@code 1}
     * dimension.
     * 
     * @param view
     *            the zone view to compare with.
     * 
     * @return the dimension on which the given {@code view} neighbors the
     *         current one.
     */
    public byte neighbors(ZoneView<C, E, T> view) {
        byte overlaps = 0;
        byte abuts = 0;
        byte abutsDimension = -1;

        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            if (this.overlaps(view, dimension)) {
                overlaps++;
            } else {
                if (this.abuts(view, dimension, true)
                        || this.abuts(view, dimension, false)) {
                    abutsDimension = dimension;
                    abuts++;
                } else {
                    return -1;
                }
            }
        }

        if ((abuts != 1)
                || (overlaps != P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue() - 1)) {
            return -1;
        } else {
            return abutsDimension;
        }
    }

    /**
     * Returns two views representing the original one split into two following
     * the specified {@code dimension}.
     * 
     * @param dimension
     *            the dimension to split on.
     * 
     * @return two new views standing for the original one split into two
     *         following the specified {@code dimension}.
     */
    @SuppressWarnings("unchecked")
    public Pair<ZoneView<C, E, T>> split(byte dimension) {
        Element<T> middle =
                Element.middle(
                        this.lowerBound.getElement(dimension),
                        this.upperBound.getElement(dimension));

        try {
            Coordinate<E, T> lowerBoundCopy = this.lowerBound.clone();
            Coordinate<E, T> upperBoundCopy = this.upperBound.clone();

            lowerBoundCopy.setElement(dimension, (E) middle);
            upperBoundCopy.setElement(dimension, (E) middle);

            return new Pair<ZoneView<C, E, T>>(this.createZoneView(
                    this.lowerBound, (C) upperBoundCopy), this.createZoneView(
                    (C) lowerBoundCopy, this.upperBound));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public ZoneView<C, E, T> merge(ZoneView<C, E, T> view) {
        byte d = this.neighbors(view);

        try {
            Coordinate<E, T> lowerBoundCopy = this.lowerBound.clone();
            Coordinate<E, T> upperBoundCopy = this.upperBound.clone();

            lowerBoundCopy.setElement(d, (E) Element.min(
                    this.lowerBound.getElement(d), view.getLowerBound(d)));
            upperBoundCopy.setElement(d, (E) Element.max(
                    this.upperBound.getElement(d), view.getUpperBound(d)));

            return this.createZoneView((C) lowerBoundCopy, (C) upperBoundCopy);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a new {@link ZoneView} with the specified {@code lowerBound} and
     * {@code upperBound} by using the same concrete class type as the current
     * one.
     * 
     * @param lowerBound
     *            the lower bound coordinate.
     * @param upperBound
     *            the upper bound coordinate.
     * 
     * @return a new {@link ZoneView}.
     */
    @SuppressWarnings("unchecked")
    private ZoneView<C, E, T> createZoneView(C lowerBound, C upperBound) {
        ZoneView<C, E, T> result = null;

        try {
            result =
                    this.getClass()
                            .getConstructor(
                                    lowerBound.getClass(),
                                    upperBound.getClass())
                            .newInstance(lowerBound, upperBound);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return result;
    }

    public C getUpperBound() {
        return this.upperBound;
    }

    public C getLowerBound() {
        return this.lowerBound;
    }

    public E getLowerBound(byte dimension) {
        return this.lowerBound.getElement(dimension);
    }

    public E getUpperBound(byte dimension) {
        return this.upperBound.getElement(dimension);
    }

    @Override
    public int hashCode() {
        return 31 * (31 + this.lowerBound.hashCode())
                + this.upperBound.hashCode();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        return obj instanceof ZoneView
                && this.lowerBound.equals(((ZoneView<C, E, T>) obj).lowerBound)
                && this.upperBound.equals(((ZoneView<C, E, T>) obj).upperBound);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(this.lowerBound);
        result.append(" to ");
        result.append(this.upperBound);

        return result.toString();
    }

}
