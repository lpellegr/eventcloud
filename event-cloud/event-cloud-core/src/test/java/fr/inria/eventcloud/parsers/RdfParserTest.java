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
package fr.inria.eventcloud.parsers;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.utils.Callback;

/**
 * Test cases associated to {@link RdfParser}.
 * 
 * @author lpellegr
 */
public class RdfParserTest {

    private int counter;

    private final Callback<Quadruple> callback;

    public RdfParserTest() {
        this.counter = 0;

        this.callback = new Callback<Quadruple>() {
            @Override
            public void execute(Quadruple quad) {
                counter++;
            }
        };
    }

    @Test
    public void parseNQuadsFileTest() {
        RdfParser.parse(
                RdfParser.class.getResourceAsStream("/example.nquads"),
                SerializationFormat.NQuads, this.callback);

        Assert.assertEquals(15, this.counter);
    }

    @Test
    public void parseTrigFileTest() {
        RdfParser.parse(
                RdfParser.class.getResourceAsStream("/example.trig"),
                SerializationFormat.TriG, this.callback);

        Assert.assertEquals(15, this.counter);
    }

    @After
    public void tearDown() {
        this.counter = 0;
    }

}
