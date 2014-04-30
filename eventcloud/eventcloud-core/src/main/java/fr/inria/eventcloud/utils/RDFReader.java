/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.utils;

import java.io.InputStream;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotReader;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.datastore.QuadrupleIterator;

/**
 * Utility methods to read from RDF files or streams and convert information
 * into objects from the EventCloud API.
 * 
 * @author lpellegr
 */
public class RDFReader {

    public static List<Quadruple> read(String uri) {
        return read(uri, true, true);
    }

    public static List<Quadruple> read(String uri,
                                       final boolean checkQuadrupleType,
                                       final boolean parseQuadrupleMetaInformation) {
        final Builder<Quadruple> quadruples = ImmutableList.builder();

        read(uri, new Callback<Quadruple>() {

            @Override
            public void execute(Quadruple quadruple) {
                quadruples.add(quadruple);
            }

        }, checkQuadrupleType, parseQuadrupleMetaInformation);

        return quadruples.build();
    }

    public static List<Quadruple> read(InputStream is,
                                       SerializationFormat format) {
        return read(is, format, true, true);
    }

    public static List<Quadruple> read(InputStream is,
                                       SerializationFormat format,
                                       final boolean checkQuadrupleType,
                                       final boolean parseQuadrupleMetaInformation) {
        final Builder<Quadruple> quadruples = ImmutableList.builder();

        read(is, format, new Callback<Quadruple>() {

            @Override
            public void execute(Quadruple quadruple) {
                quadruples.add(quadruple);
            }

        }, checkQuadrupleType, parseQuadrupleMetaInformation);

        return quadruples.build();
    }

    public static void read(String uri, Callback<Quadruple> action) {
        read(uri, action, true, true);
    }

    public static void read(String uri, final Callback<Quadruple> action,
                            final boolean checkQuadrupleType,
                            final boolean checkQuadrupleMetaInformation) {
        StreamRDF sink = new StreamRDFBase() {
            @Override
            public void quad(Quad quad) {
                action.execute(JenaConverter.toQuadruple(
                        quad, checkQuadrupleType, checkQuadrupleMetaInformation));
            }
        };

        RDFDataMgr.parse(sink, uri);
    }

    public static void read(InputStream is, SerializationFormat format,
                            Callback<Quadruple> action) {
        read(is, format, action, true, true);
    }

    public static void read(InputStream is, SerializationFormat format,
                            final Callback<Quadruple> action,
                            final boolean checkQuadrupleType,
                            final boolean checkQuadrupleMetaInformation) {
        StreamRDF sink = new StreamRDFBase() {
            @Override
            public void quad(Quad quad) {
                action.execute(JenaConverter.toQuadruple(
                        quad, checkQuadrupleType, checkQuadrupleMetaInformation));
            }
        };

        RDFDataMgr.parse(sink, is, null, format.toJenaLang());
    }

    public static final QuadrupleIterator pipe(InputStream in,
                                               SerializationFormat format) {
        return pipe(in, format, true, true);
    }

    public static final QuadrupleIterator pipe(InputStream in,
                                               SerializationFormat format,
                                               boolean checkQuadrupleType,
                                               boolean parseQuadrupleMetaInformation) {
        return new QuadrupleIterator(
                RiotReader.createIteratorQuads(in, format.toJenaLang(), null),
                checkQuadrupleType, parseQuadrupleMetaInformation);
    }

}
