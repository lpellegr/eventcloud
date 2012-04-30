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
package fr.inria.eventcloud.parsers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.utils.Callback;

/**
 * Test cases associated to {@link RdfParser} and {@link RdfSerializer}.
 * 
 * @author lpellegr
 * @author ialshaba
 */
public class RdfParserTest {

    private int counter = 0;

    @Test
    public void parseNQuadsFileTest() {
        final List<Quadruple> quads = new ArrayList<Quadruple>();

        RdfParser.parse(
                RdfParser.class.getResourceAsStream("/example.nquads"),
                SerializationFormat.NQuads, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        quads.add(quad);
                        RdfParserTest.this.counter++;
                    }
                });

        Assert.assertEquals(15, this.counter);

        this.counter = 0;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        RdfSerializer.nQuadsWriter(bos, quads);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        RdfParser.parse(
                bis, SerializationFormat.NQuads, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        RdfParserTest.this.counter++;
                    }
                });

        Assert.assertEquals(15, this.counter);
    }

    @Test
    public void parseTrigFileTest() {
        final List<Quadruple> quads = new ArrayList<Quadruple>();

        RdfParser.parse(
                RdfParser.class.getResourceAsStream("/example.trig"),
                SerializationFormat.TriG, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        quads.add(quad);
                        RdfParserTest.this.counter++;
                    }
                });

        Assert.assertEquals(15, this.counter);

        this.counter = 0;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        RdfSerializer.triGWriter(bos, quads);

        ByteArrayInputStream ins = new ByteArrayInputStream(bos.toByteArray());
        RdfParser.parse(
                ins, SerializationFormat.TriG, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        RdfParserTest.this.counter++;
                    }
                });

        Assert.assertEquals(15, this.counter);
    }

    @Test
    public void parseNQuadsWriteTriGTest() {
        final List<Quadruple> quads = new ArrayList<Quadruple>();
        RdfParser.parse(
                RdfParser.class.getResourceAsStream("/example.nquads"),
                SerializationFormat.NQuads, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        RdfParserTest.this.counter++;
                        quads.add(quad);
                    }
                });

        Assert.assertEquals(15, this.counter);

        this.counter = 0;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        RdfSerializer.triGWriter(bos, quads);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        RdfParser.parse(
                bis, SerializationFormat.TriG, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        RdfParserTest.this.counter++;
                    }
                });

        Assert.assertEquals(15, this.counter);
    }

}
