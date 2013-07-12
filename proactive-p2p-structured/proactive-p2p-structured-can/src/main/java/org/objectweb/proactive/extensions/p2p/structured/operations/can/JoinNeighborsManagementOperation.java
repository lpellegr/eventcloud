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

import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;

/**
 * Abstract class defining compatibility for neighbors management operations.
 * 
 * @author lpellegr
 */
public abstract class JoinNeighborsManagementOperation extends
        CallableOperation {

    private static final long serialVersionUID = 150L;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithLeave() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithRouting() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatible(CallableOperation other) {
        return other instanceof JoinIntroduceOperation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isJoinOperation() {
        return true;
    }

}
