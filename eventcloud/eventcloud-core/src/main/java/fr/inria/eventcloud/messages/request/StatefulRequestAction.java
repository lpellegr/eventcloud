/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.messages.request;

/**
 * A stateful request action is a wrapper that contains the result of the action
 * which is executed with a {@link StatefulQuadruplePatternRequest}, but also
 * some other metrics like the time to execute the action, etc.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the action type result.
 */
public final class StatefulRequestAction<T> {

    public final long duration;

    public final T result;

    public StatefulRequestAction(long duration, T result) {
        super();
        this.duration = duration;
        this.result = result;
    }

}
