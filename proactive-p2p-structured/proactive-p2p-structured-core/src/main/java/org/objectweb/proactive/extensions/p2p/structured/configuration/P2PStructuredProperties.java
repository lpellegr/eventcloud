package org.objectweb.proactive.extensions.p2p.structured.configuration;

/**
 * Defines properties for the structured p2p module.
 * 
 * @author lpellegr
 */
public class P2PStructuredProperties {

	public static final PropertyString CAN_COORDINATE_TYPE =
                            new PropertyString(
                            		"can.coordinate.element.type", 
                            		"org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.StringElement");
	
	public static final PropertyInteger CAN_NB_DIMENSIONS =
							new PropertyInteger("can.nb.dimensions", 3);
	
	public static final PropertyString CAN_LOWER_BOUND =
							new PropertyString("can.lower.bound", "0");
	
	public static final PropertyString CAN_UPPER_BOUND = 
							new PropertyString("can.upper.bound", "{");
	
	public static final PropertyInteger CAN_REFRESH_TASK_START =
	                        new PropertyInteger("can.task.refresh.start", 0);
	
	public static final PropertyInteger CAN_REFRESH_TASK_INTERVAL =
                            new PropertyInteger("can.task.refresh.interval", 500);
	
	/**
	 * Indicates which representation to use for coordinates when they are printed
	 * on the standard output. The values can be <code>alpha</code> for a standard
	 * String representation or <code>codepoints</code> in order to display the
	 * code points values.
	 */
	public static final PropertyString CAN_COORDINATE_DISPLAY =
							new PropertyString("can.coordinate.display", "alpha");
	
    public static final PropertyString CHORD_ID_REPRESENTATION =
        					new PropertyString("chord.id.representation", "hexadecimal");

    public static final PropertyInteger CHORD_ID_DISPLAYED_BYTES = 
							new PropertyInteger("chord.id.displayed.bytes", 4);

    public static final PropertyInteger CHORD_NB_TOLERANT_FAILURES =
							new PropertyInteger("chord.nb.tolerant.failures", 0);

    public static final PropertyInteger CHORD_CHECK_PREDECESSOR_TASK_START = 
							new PropertyInteger("chord.check.predecessor.task.start", 6000);

    public static final PropertyInteger CHORD_CHECK_PREDECESSOR_TASK_INTERVAL =
							new PropertyInteger("chord.check.predecessor.task.interval", 12000);

    public static final PropertyInteger CHORD_FIX_FINGER_TASK_START = 
							new PropertyInteger("chord.fix.finger.task.start", 0);

    public static final PropertyInteger CHORD_FIX_FINGER_TASK_INTERVAL = 
							new PropertyInteger("chord.fix.finger.task.interval", 12000);

    public static final PropertyInteger CHORD_STABILIZE_TASK_START =
							new PropertyInteger("chord.stabilize.task.start", 12000);

    public static final PropertyInteger CHORD_STABILIZE_TASK_INTERVAL =
							new PropertyInteger("chord.stabilize.task.interval", 12000);
	
	public static final PropertyDouble TRACKER_STORAGE_PROBABILITY = 
							new PropertyDouble("tracker.storage.probability", 1.0);
	
}
