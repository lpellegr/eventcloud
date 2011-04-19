package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

/**
 * A unicode zone view is a zone view maintaining coordinates as
 * {@link StringElement}s.
 * 
 * @author lpellegr
 */
public class UnicodeZoneView extends
        ZoneView<StringCoordinate, StringElement, String> {

    private static final long serialVersionUID = 1L;

    public UnicodeZoneView(StringCoordinate lowerBound,
            StringCoordinate upperBound) {
        super(lowerBound, upperBound);
    }

}
