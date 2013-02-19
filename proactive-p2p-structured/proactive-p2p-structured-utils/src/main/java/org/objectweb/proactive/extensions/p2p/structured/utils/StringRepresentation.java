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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import java.util.Arrays;

import com.google.common.base.Function;

/**
 * Enumeration used to define different representations of a String.
 * 
 * @author lpellegr
 */
public enum StringRepresentation {

    CODE_POINTS("codepoints", new Function<String, String>() {
        @Override
        public String apply(String value) {
            return Arrays.toString(UnicodeUtils.toCodePointArray(value));
        }
    }),

    UTF_16("utf16", new Function<String, String>() {
        @Override
        public String apply(String value) {
            return UnicodeUtils.toStringUtf16(value);
        }
    }),

    UTF_32("utf32", new Function<String, String>() {
        @Override
        public String apply(String value) {
            return UnicodeUtils.toStringUtf32(value);
        }
    }),

    STRING("string", new Function<String, String>() {
        @Override
        public String apply(String value) {
            return value;
        }
    });

    public String name;

    public Function<String, String> transformer;

    private StringRepresentation(String name,
            Function<String, String> transformer) {
        this.name = name;
        this.transformer = transformer;
    }

    public String getName() {
        return this.name;
    }

    public String apply(String value) {
        return this.transformer.apply(value);
    }

    public static StringRepresentation getInstance(String name) {
        for (StringRepresentation representation : StringRepresentation.values()) {
            if (representation.name.equalsIgnoreCase(name)) {
                return representation;
            }
        }

        throw new IllegalArgumentException("Unknown string representation: "
                + name);
    }

}
