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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A basic implementation of an Observable for the Observer/Observable pattern
 * by using a {@link ConcurrentHashMap}.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the type of the observers.
 */
public class Observable<T> {

    private final ConcurrentHashMap<T, Long> observers;

    public Observable() {
        this.observers = new ConcurrentHashMap<T, Long>();
    }

    /**
     * Registers a new observer.
     * 
     * @param observer
     *            the listener that want to observe.
     */
    public void register(T observer) {
        this.observers.putIfAbsent(observer, System.currentTimeMillis());
    }

    /**
     * Unregisters the specified observer. Passing an observer which is not
     * registered has not effect.
     * 
     * @param observer
     *            the observer to remove.
     */
    public void unregister(T observer) {
        this.observers.remove(observer);
    }

    /**
     * Returns the registration time associated to the specified observer.
     * 
     * @param observer
     *            the observer to use.
     * 
     * @return the registration time associated to the specified observer.
     */
    public long getRegistrationTime(T observer) {
        Long result = this.observers.get(observer);
        if (result == null) {
            return -1;
        }

        return result;
    }

    /**
     * Returns the observers which have been registered.
     * 
     * @return the observers which have been registered.
     */
    public Set<T> getObservers() {
        return this.observers.keySet();
    }

    /**
     * Notify all the observers with the specified action.
     * 
     * @param action
     *            the action to execute on each observer which has been
     *            notified?
     */
    public void notify(NotificationAction<T> action) {
        for (T observer : this.observers.keySet()) {
            action.execute(observer);
        }
    }

    /**
     * Provides a closure for wrapping the action to execute for a notification.
     */
    public static abstract class NotificationAction<T> {

        public abstract void execute(T observer);

    }

}
