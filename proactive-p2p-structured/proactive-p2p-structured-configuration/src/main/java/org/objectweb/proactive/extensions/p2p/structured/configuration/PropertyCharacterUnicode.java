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
 * A {@link Character} property for representating a unicode character.
 * 
 * @author lpellegr
 */
public class PropertyCharacterUnicode extends Property<Integer> {

    public PropertyCharacterUnicode(String name, String defaultValue) {
        super(name, computeScalarValue(defaultValue));
    }

    public PropertyCharacterUnicode(String name, String defaultValue,
            Validator<Integer> validator) {
        super(name, computeScalarValue(defaultValue), validator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAsString(String value) {
        super.setValue(computeScalarValue(value));
    }

    public String getValueAsString() {
        return new String(Character.toChars(super.value));
    }

    private static int computeScalarValue(String unicodeCharacter) {
        if (!unicodeCharacter.isEmpty()) {
            int codePoint = unicodeCharacter.codePointAt(0);

            if (Character.isValidCodePoint(codePoint)) {
                if (unicodeCharacter.length() == 1
                        || (Character.isSupplementaryCodePoint(codePoint) && unicodeCharacter.length() == 2)) {
                    return codePoint;
                } else {
                    throw new IllegalArgumentException(
                            "Only one unicode character is allowed: "
                                    + unicodeCharacter);
                }
            } else {
                throw new IllegalArgumentException(
                        "Illegal unicode character specified: "
                                + unicodeCharacter);
            }
        } else {
            throw new IllegalArgumentException("Empty string is not allowed");
        }
    }

}
