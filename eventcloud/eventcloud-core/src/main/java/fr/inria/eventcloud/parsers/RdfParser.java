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
package fr.inria.eventcloud.parsers;

import java.io.InputStream;

import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;

import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.utils.Callback;

/**
 * Defines an RDF parser which knows how to parse different format such that
 * TriG or NQuads.
 * 
 * @author lpellegr
 */
public class RdfParser {

    /**
     * Parses the given input stream by using the specified
     * {@link SerializationFormat}. Each time a {@link Quadruple} is read, the
     * specified {@link Callback} is applied to this {@link Quadruple}.
     * 
     * @param in
     *            the input stream where the data are consumed.
     * 
     * @param format
     *            the format which is expected when the data are read from the
     *            input stream.
     * 
     * @param action
     *            the callback action to perform each time a quadruple is
     *            parsed.
     */
    public static final void parse(InputStream in, SerializationFormat format,
                                   final Callback<Quadruple> action) {
        parse(in, format, action, true);
    }

    /**
     * Parses the given input stream by using the specified
     * {@link SerializationFormat}. Each time a {@link Quadruple} is read, the
     * specified {@link Callback} is applied to this {@link Quadruple}.
     * 
     * @param in
     *            the input stream where the data are consumed.
     * 
     * @param format
     *            the format which is expected when the data are read from the
     *            input stream.
     * 
     * @param action
     *            the callback action to perform each time a quadruple is
     *            parsed.
     * 
     * @param checkQuadrupleSyntax
     *            indicates whether the syntax of each quadruple which is
     *            reconstructed has to be checked (e.g. to throw an exception if
     *            a Blank Node is detected because this kind of RDF term is not
     *            supported).
     */
    public static final void parse(InputStream in, SerializationFormat format,
                                   final Callback<Quadruple> action,
                                   final boolean checkQuadrupleSyntax) {
        Sink<Quad> sink = new Sink<Quad>() {
            @Override
            public void send(final Quad quad) {
                action.execute(new Quadruple(
                        quad.getGraph(), quad.getSubject(),
                        quad.getPredicate(), quad.getObject(),
                        checkQuadrupleSyntax, false));
            }

            @Override
            public void close() {
            }

            @Override
            public void flush() {
            }

        };

        LangRIOT parser;

        switch (format) {
            case TriG:
                parser = RiotReader.createParserTriG(in, null, sink);
                break;
            case NQuads:
                parser = RiotReader.createParserNQuads(in, sink);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknow SerializationFormat: " + format);
        }

        parser.parse();
        sink.close();
    }

    public static final QuadrupleIterator parse(InputStream in,
                                                SerializationFormat format) {
        return parse(in, format, true);
    }

    public static final QuadrupleIterator parse(InputStream in,
                                                SerializationFormat format,
                                                boolean checkQuadrupleSyntax) {
        return new QuadrupleIterator(RiotReader.createIteratorQuads(
                in, format.toJenaLang(), null), checkQuadrupleSyntax, false);
    }

}
