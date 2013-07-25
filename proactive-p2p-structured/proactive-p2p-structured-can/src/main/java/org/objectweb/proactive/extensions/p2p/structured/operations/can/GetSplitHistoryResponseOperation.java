/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.SplitEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * Response associated to {@link GetSplitHistoryOperation}.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class GetSplitHistoryResponseOperation<E extends Element> implements
        ResponseOperation {

    private static final long serialVersionUID = 151L;

    private final List<SplitEntry> splitHistory;

    public GetSplitHistoryResponseOperation(List<SplitEntry> splitHistory) {
        super();
        this.splitHistory = splitHistory;
    }

    public List<SplitEntry> getSplitHistory() {
        return this.splitHistory;
    }

}
