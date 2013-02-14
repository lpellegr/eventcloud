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
package fr.inria.eventcloud.multiactivities;

import java.util.Comparator;
import java.util.List;

import com.google.common.base.Objects;

/**
 * 
 * 
 * @author lpellegr
 */
public class RequestPriorityConstraint implements
        Comparator<RequestPriorityConstraint> {

    private final String methodName;

    private final List<Class<?>> parameterTypes;

    // priority level for methods without priority is 0
    private final int priorityLevel;

    public RequestPriorityConstraint(String methodName, int priorityLevel) {
        this(methodName, null, priorityLevel);
    }

    public RequestPriorityConstraint(String methodName,
            List<Class<?>> parameterTypes, int priorityLevel) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.priorityLevel = priorityLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(RequestPriorityConstraint rpc1,
                       RequestPriorityConstraint rpc2) {
        return rpc1.priorityLevel - rpc2.priorityLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        RequestPriorityConstraint other = (RequestPriorityConstraint) obj;
        if (this.methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        } else if (!this.methodName.equals(other.methodName)) {
            return false;
        }

        if (this.parameterTypes == null) {
            if (other.parameterTypes != null) {
                return false;
            }
        } else if (!this.parameterTypes.equals(other.parameterTypes)) {
            return false;
        }

        if (this.priorityLevel != other.priorityLevel) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(
                this.priorityLevel, this.methodName, this.parameterTypes);
    }

    public String getMethodName() {
        return this.methodName;
    }

    public List<Class<?>> getParameterTypes() {
        return this.parameterTypes;
    }

    public int getPriorityLevel() {
        return this.priorityLevel;
    }

}
