package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

/**
 * 
 * 
 * @author lpellegr
 */
public class StringCoordinate extends Coordinate<StringElement, String> {

    private static final long serialVersionUID = 1L;

    public StringCoordinate(StringElement... elts) {
        super(elts);
    }

}
