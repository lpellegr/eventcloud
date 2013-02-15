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
package org.objectweb.proactive.extensions.p2p.structured.providers;

import java.io.Serializable;

import javax.inject.Provider;

/**
 * This class is used to get an instance of type {@code T}. In addition to all
 * the advantages described in {@link Provider}, a provider is usefull to allow
 * parameters that are not serializable into the constructor of an active
 * object. Indeed, instead of passing a not serializable parameter, a provider
 * that knows how to construct this non-serializable object is used. Hence, the
 * provider instance will be serialized but not the object provided by the
 * provider.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the object type this provider provides.
 */
public abstract class SerializableProvider<T> implements Provider<T>,
        Serializable {

    private static final long serialVersionUID = 140L;

    /**
     * Creates a provider for the specified {@code clazz}. This method is a
     * convenient method to build a provider for the given {@code clazz} by
     * using its empty constructor.
     * 
     * @param clazz
     *            the instance of the class to encapsulate into the provider to
     *            create.
     * 
     * @return a provider for the specified {@code clazz}.
     * 
     * @throws IllegalArgumentException
     *             if the instantiation fails.
     */
    public static <T> SerializableProvider<T> create(final Class<T> clazz) {
        return new SerializableProvider<T>() {

            private static final long serialVersionUID = 140L;

            @Override
            public T get() {
                try {
                    return clazz.newInstance();
                } catch (InstantiationException e) {
                    throw new IllegalArgumentException(
                            "The specified class represents an abstract class, an interface, an array class, a primitive type, or void",
                            e);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(
                            "Class or empty constructor not accessible", e);
                }
            }
        };
    }

}
