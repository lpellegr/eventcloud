/**
 * Copyright (c) 2011 INRIA.
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
package fr.inria.eventcloud.api;

import java.io.Serializable;

/**
 * Uniquely identify a subscription that has been submitted on an Event-Cloud.
 * 
 * @author lpellegr
 */
public final class SubscriptionId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long value;

    public SubscriptionId(long value) {
        this.value = value;
    }

    /**
     * Parses the string argument as a SubscriptionId.
     * 
     * @param s
     *            a <code>String</code> containing the
     *            <code>SubscriptionId</code> representation to be parsed.
     * 
     * @return the <code>SubscriptionId</code> represented by the argument.
     */
    public static final SubscriptionId parseFrom(String s) {
        return new SubscriptionId(Long.parseLong(s));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SubscriptionId
                && this.value == ((SubscriptionId) obj).value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Long.valueOf(value).hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Long.toString(this.value);
    }

}
