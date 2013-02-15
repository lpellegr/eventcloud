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
package org.objectweb.proactive.extensions.p2p.structured.configuration;

/**
 * Parent of any concrete property. It stores the property name, it associated
 * value and default value. In addition it can check whether the specified value
 * is correct or not according to a {@link Validator} if it is not {@code null}.
 * 
 * @author lpellegr
 */
public abstract class Property<T> {

    protected final String name;

    protected final T defaultValue;

    protected T value;

    protected final Validator<T> validator;

    protected Property(String name, T defaultValue) {
        this(name, defaultValue, null);
    }

    protected Property(String name, T defaultValue, Validator<T> validator) {
        if (validator != null) {
            validator.checkValidity(name, defaultValue);
        }

        this.name = name;
        this.defaultValue = defaultValue;
        this.validator = validator;

        String javaProperty = System.getProperty(this.name);

        if (javaProperty != null) {
            this.setValueAsString(javaProperty);
        } else {
            this.value = defaultValue;
        }
    }

    /**
     * Returns the key associated to this property.
     * 
     * @return the key associated to this property.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the default value of this property.
     * 
     * @return the default value of this property.
     */
    public T getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property.
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Sets the value of this property.
     * 
     * @param value
     *            new value of the property.
     */
    public void setValue(T value) {
        if (this.validator != null) {
            this.validator.checkValidity(this.name, value);
        }

        this.value = value;
    }

    /**
     * Sets the value of this property from the specified String value. Once the
     * string value has been parsed, the value has to be set by calling
     * {@link #setValue(Object)} to guarantee that the value validity is checked
     * if a validator is specified.
     * 
     * @param value
     *            new value of the property.
     */
    public void setValueAsString(String value) {
        this.setValue(this.parse(value));
    }

    protected abstract T parse(String value);

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.name + "=" + this.value;
    }

}
