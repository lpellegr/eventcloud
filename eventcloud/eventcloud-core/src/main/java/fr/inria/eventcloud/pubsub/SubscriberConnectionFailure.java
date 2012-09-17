/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.pubsub;

/**
 * Contains some information about connection failures with a subscriber.
 * 
 * @author lpellegr
 */
public class SubscriberConnectionFailure {

    private long lastContact;

    private int nbAttempts;

    public SubscriberConnectionFailure() {
        this.lastContact = System.currentTimeMillis();
        this.nbAttempts = 0;
    }

    public int getNbAttempts() {
        return this.nbAttempts;
    }

    public void incNbAttempts() {
        this.nbAttempts++;
    }

    public long getLastContact() {
        return this.lastContact;
    }

}
