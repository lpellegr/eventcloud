package fr.inria.eventcloud.overlay.can;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.ZoneException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;

/**
 * This class maintains two representation of a zone: one by extending the
 * {@link Zone} class and one from scratch. This first representation is used to
 * index semantic data by using the lexicographic order whereas the second one
 * is used for the maintenance of the overlay.
 * 
 * @author lpellegr
 */
public class DualLexicographicZone extends Zone {
	
	private static final long serialVersionUID = 1L;

	private double[][] intervals;
	
	private double[] center;
	
	public DualLexicographicZone() {
		super();
		this.init();
	}
	
	protected DualLexicographicZone(Coordinate lowerBound, Coordinate upperBound, double[][] intervals) {
		super(lowerBound, upperBound);
		this.intervals = intervals;
		this.computeCenter();
	}
	
	private void init() {
		this.intervals = new double[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()][2];
		for (int dim=0; dim<P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
			this.intervals[dim][0] = 0.0;
			this.intervals[dim][1] = 1.0;
		}
		this.computeCenter();
	}
	
	/**
	 * Returns the span of the zone.
	 * 
	 * @return the span of the zone.
	 */
    public double getArea() {
        double area = 1;

        // find the percentage of the space for each dimension
        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            area *= intervals[i][1] - intervals[i][0];
        }

        return area;
    }

    /**
     * Computes the center of the zone.
     */
    private void computeCenter() {
    	this.center = new double[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];
        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            this.center[i] = (intervals[i][0] + intervals[i][1]) / 2.0;
        }
    }
    
	/**
	 * Calculate the distance from the center of this zone to the given point
	 * 
	 * @param coordinates
	 * @return
	 */
	public double distance(double[] coordinates) {
		double distance = 0;
		double projection;

		for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
			projection = coordinates[i] - this.center[i];
			projection *= projection;
			distance += projection;
		}

		return Math.sqrt(distance);
	}
	
	@Override
	public DualLexicographicZone[] split(int dim) throws ZoneException {
		double[][] newIntervalsA = new double[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()][2];
		double[][] newIntervalsB = new double[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()][2];
		double middle = (this.intervals[dim][0] + this.intervals[dim][1]) / 2.0;

		for (int i=0; i<P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
			// sets the new coordinate on the dim which is split
			if (i == dim) {
				newIntervalsA[i][0] = this.intervals[i][0];
				newIntervalsA[i][1] = middle;
				newIntervalsB[i][0] = middle;
				newIntervalsB[i][1] = this.intervals[i][1];
			} else {
				newIntervalsA[i][0] = this.intervals[i][0];
				newIntervalsA[i][1] = this.intervals[i][1];
				newIntervalsB[i][0] = this.intervals[i][0];
				newIntervalsB[i][1] = this.intervals[i][1];
			}
		}
		
		Zone[] zones = super.split(dim);
	
		return new DualLexicographicZone[] {
			new DualLexicographicZone(
					zones[0].getLowerBound(), 
					zones[0].getUpperBound(), 
					newIntervalsA),
			new DualLexicographicZone(
					zones[1].getLowerBound(), 
					zones[1].getUpperBound(), 
					newIntervalsB)	
		};
	}

	public double[][] getIntervals() {
		return this.intervals;
	}
	
	public double[] getCenter() {
		return this.center;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append(" <=> (");
        for (int dim=0; dim<P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
        	buf.append(this.intervals[dim][0]);
        	if (dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()-1) {
        		buf.append(", ");
        	}
        }
        buf.append(") to (");
        for (int dim=0; dim<P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
        	buf.append(this.intervals[dim][1]);
        	if (dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()-1) {
        		buf.append(", ");
        	}
        }
        buf.append(")");
        return buf.toString();
	}
}
