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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.utils.Callback;

/**
 * Test cases associated to {@link RdfParser} and {@link RdfSerialier}.
 * 
 * @author lpellegr
 * @author ialshaba
 */
public class RdfParserTest {

    private static int counter = 0;

    @Test
    public void parseNQuadsFileTest() {
        final List<Quadruple> quads = new ArrayList<Quadruple>();
        RdfParser.parse(
                RdfParser.class.getResourceAsStream("/example.nquads"),
                SerializationFormat.NQuads, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        counter++;
                    }
                });

        // parser = RiotReader. in, null, sink);

        Assert.assertEquals(15, counter);
        tearDown();
        ByteArrayOutputStream outS = new ByteArrayOutputStream();
        // BufferedOutputStream bOut = new BufferedOutputStream(out)
        RdfSerializer.nQuadsWriter(outS, new ArrayList<Quadruple>(quads));
        ByteArrayInputStream ins = new ByteArrayInputStream(outS.toByteArray());
        RdfParser.parse(
                ins, SerializationFormat.NQuads, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        counter++;
                    }
                });

    }

    @Test
    public void parseTrigFileTest() {
        final List<Quadruple> quads = new ArrayList<Quadruple>();
        // OutputStream out = null;
        RdfParser.parse(
                RdfParser.class.getResourceAsStream("/example.trig"),
                SerializationFormat.TriG, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        /* low level test
                        System.out.println("quad "+ counter+ " graph \t " +quad.getGraph().toString());
                        System.out.println("quad "+ counter+ " subject \t " +quad.getSubject().toString());
                        System.out.println("quad "+ counter+ " predicate \t " +quad.getPredicate().toString());
                        System.out.println("quad "+ counter+ " object \t " +quad.getObject().toString());
                        */
                        counter++;
                        quads.add(quad);
                    }
                });

        Assert.assertEquals(15, counter);
        tearDown();
        ByteArrayOutputStream outS = new ByteArrayOutputStream();
        // BufferedOutputStream bOut = new BufferedOutputStream(out)

        RdfSerializer.triGWriter(outS, new ArrayList<Quadruple>(quads));
        try {
            outS.writeTo(System.out);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ByteArrayInputStream ins = new ByteArrayInputStream(outS.toByteArray());
        RdfParser.parse(
                ins, SerializationFormat.TriG, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        counter++;
                        quads.add(quad);
                    }

                });
        Assert.assertEquals(15, counter);
    }

    @Test
    public void parseNQuadsWriteTriGTest() {
        final List<Quadruple> quads = new ArrayList<Quadruple>();
        RdfParser.parse(
                RdfParser.class.getResourceAsStream("/example.nquads"),
                SerializationFormat.NQuads, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        counter++;
                        quads.add(quad);
                    }
                });

        Assert.assertEquals(15, counter);
        tearDown();

        ByteArrayOutputStream outS = new ByteArrayOutputStream();

        RdfSerializer.triGWriter(outS, quads);
        try {
            outS.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayInputStream ins = new ByteArrayInputStream(outS.toByteArray());
        quads.clear();
        RdfParser.parse(
                ins, SerializationFormat.TriG, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        counter++;
                        quads.add(quad);
                    }
                });

        Assert.assertEquals(15, counter);

    }

    @After
    public void tearDown() {
        counter = 0;

    }

}
