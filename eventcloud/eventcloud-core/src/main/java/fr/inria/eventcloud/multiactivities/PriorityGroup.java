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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.objectweb.proactive.core.body.request.Request;

import com.google.common.base.Objects;

/**
 * Groups requests that have the same priority level.
 * 
 * @author lpellegr
 */
public class PriorityGroup implements Comparator<PriorityGroup> {

    private final int priorityLevel;

    private final List<Request> requests;

    // position of the request in the original request queue
    private final List<Integer> requestPositions;

    public PriorityGroup(int priorityLevel) {
        this.priorityLevel = priorityLevel;
        this.requests = new ArrayList<Request>();
        this.requestPositions = new ArrayList<Integer>();
    }

    public void add(Request request, int position) {
        this.requests.add(request);
        this.requestPositions.add(position);
    }

    public void clear() {
        this.requests.clear();
        this.requestPositions.clear();
    }

    public int getPriorityLevel() {
        return this.priorityLevel;
    }

    public List<Request> getRequests() {
        return this.requests;
    }

    public List<Integer> getRequestPositions() {
        return this.requestPositions;
    }

    public int size() {
        return this.requests.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(PriorityGroup pg1, PriorityGroup pg2) {
        return pg1.priorityLevel - pg2.priorityLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(
                this.priorityLevel, this.requestPositions, this.requests);
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
        PriorityGroup other = (PriorityGroup) obj;
        if (this.priorityLevel != other.priorityLevel) {
            return false;
        }
        if (this.requestPositions == null) {
            if (other.requestPositions != null) {
                return false;
            }
        } else if (!this.requestPositions.equals(other.requestPositions)) {
            return false;
        }
        if (this.requests == null) {
            if (other.requests != null) {
                return false;
            }
        } else if (!this.requests.equals(other.requests)) {
            return false;
        }
        return true;
    }

}
