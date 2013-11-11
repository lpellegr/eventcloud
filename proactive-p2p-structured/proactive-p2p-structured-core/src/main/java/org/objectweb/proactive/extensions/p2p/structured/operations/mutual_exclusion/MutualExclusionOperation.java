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
package org.objectweb.proactive.extensions.p2p.structured.operations.mutual_exclusion;

import org.objectweb.proactive.extensions.p2p.structured.operations.RunnableOperation;

/**
 * Mutual exclusion operation. It overrides some compatibility methods to avoid
 * some boilerplate code.
 * 
 * @author lpellegr
 */
public abstract class MutualExclusionOperation extends RunnableOperation {

    private static final long serialVersionUID = 160L;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithRouting() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatible(RunnableOperation other) {
        return other instanceof MutualExclusionOperation;
    }

}
