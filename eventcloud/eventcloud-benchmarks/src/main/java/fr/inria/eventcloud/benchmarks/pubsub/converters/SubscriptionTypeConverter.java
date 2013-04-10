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
package fr.inria.eventcloud.benchmarks.pubsub.converters;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;

import fr.inria.eventcloud.benchmarks.pubsub.SubscriptionType;

/**
 * Simple rewriting type converter for {@link JCommander}.
 * 
 * @author lpellegr
 */
public class SubscriptionTypeConverter implements
        IStringConverter<SubscriptionType> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionType convert(String value) {
        if (value.equalsIgnoreCase("path-query-fixed-predicate")) {
            return SubscriptionType.PATH_QUERY_FIXED_PREDICATE;
        } else if (value.equalsIgnoreCase("path-query-free-predicate")) {
            return SubscriptionType.PATH_QUERY_FREE_PREDICATE;
        } else if (value.equalsIgnoreCase("accept-all")) {
            return SubscriptionType.ACCEPT_ALL;
        }

        throw new IllegalArgumentException("Unknow subscription type: " + value);
    }

}
