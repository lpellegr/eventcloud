package fr.inria.eventcloud.parsers;

import java.io.InputStream;

import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;

import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
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
        Sink<Quad> sink = new Sink<Quad>() {
            @Override
            public void send(final Quad quad) {
                action.execute(new Quadruple(
                        quad.getGraph(), quad.getSubject(),
                        quad.getPredicate(), quad.getObject()));
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

}
