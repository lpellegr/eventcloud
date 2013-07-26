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
package fr.inria.eventcloud.benchmarks.pubsub;

import java.io.Serializable;

/**
 * 
 * 
 * @author lpellegr
 */
public class StorageTimes implements Serializable {

    private static final long serialVersionUID = 160L;

    private long publicationsEndTime;

    private long subscriptionsEndTime;

    public StorageTimes(long publicationsEndTime, long subscriptionsEndTime) {
        super();
        this.publicationsEndTime = publicationsEndTime;
        this.subscriptionsEndTime = subscriptionsEndTime;
    }

    public long getPublicationsEndTime() {
        return this.publicationsEndTime;
    }

    public long getSubscriptionsEndTime() {
        return this.subscriptionsEndTime;
    }

    public void setPublicationsEndTime(long publicationsEndTime) {
        this.publicationsEndTime = publicationsEndTime;
    }

    public void setSubscriptionsEndTime(long subscriptionsEndTime) {
        this.subscriptionsEndTime = subscriptionsEndTime;
    }

}
