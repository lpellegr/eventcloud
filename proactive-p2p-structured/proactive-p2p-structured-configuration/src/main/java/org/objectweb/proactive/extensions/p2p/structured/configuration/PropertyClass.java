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
package org.objectweb.proactive.extensions.p2p.structured.configuration;

/**
 * A {@link Class} property.
 * 
 * @author lpellegr
 */
public class PropertyClass extends Property<Class<?>> {

    private final String defaultClassName;

    private String className;

    public PropertyClass(String name, Class<?> defaultValue) {
        super(name, defaultValue);

        this.defaultClassName = defaultValue.getName();

        if (this.className == null) {
            this.className = this.defaultClassName;
        }
    }

    public PropertyClass(String name, String className) {
        super(name, null);

        this.defaultClassName = className;

        if (this.className == null) {
            this.className = this.defaultClassName;
        }
    }

    public PropertyClass(String name, Class<?> defaultValue,
            Validator<Class<?>> validator) {
        super(name, defaultValue, validator);

        this.defaultClassName = defaultValue.getName();

        if (this.className == null) {
            this.className = this.defaultClassName;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDefaultValue() {
        return transform(this.defaultClassName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getValue() {
        return transform(this.className);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(Class<?> value) {
        if (this.validator != null) {
            this.validator.checkValidity(this.name, value);
        }

        this.className = value.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAsString(String value) {
        this.className = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> parse(String className) {
        return transform(className);
    }

    private static Class<?> transform(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
