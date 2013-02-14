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
package fr.inria.eventcloud.benchmarks.radix10_conversion;

import com.beust.jcommander.IStringConverter;

/**
 * A simple converter to specify how run arguments have to be parsed.
 * 
 * @author lpellegr
 */
public class RunConverter implements IStringConverter<Run> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Run convert(String value) {
        String[] chunks = value.split(",");

        if (chunks.length == 2) {
            int nbQuadsToRead = parseInt(chunks[0]);
            int precision = parseInt(chunks[1]);

            return new Run(nbQuadsToRead, precision);
        } else {
            throw new IllegalArgumentException("Invalid argument: " + value);
        }
    }

    private static final int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
