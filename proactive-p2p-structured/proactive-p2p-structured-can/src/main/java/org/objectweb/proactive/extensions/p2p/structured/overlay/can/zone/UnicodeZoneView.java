package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

/**
 * 
 * @author lpellegr
 */
public class UnicodeZoneView extends ZoneView<StringCoordinate, StringElement, String> {

	private static final long serialVersionUID = 1L;
	
	public UnicodeZoneView(StringCoordinate lowerBound, StringCoordinate upperBound) {
		super(lowerBound, upperBound);
	}
	
}
