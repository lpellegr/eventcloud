package fr.inria.eventcloud.utils;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtil;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;

import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;

/**
 * 
 * @author lpellegr
 */
public class InputStreamReaderHelper {

    public static final void read(InputStream in, SerializationFormat format,
                                  final QuadrupleAction action) {
        // TODO define the number of threads to use and if the thread pool has
        // to be shared between all the methods?
        final ExecutorService threadPool =
                Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());
        
        Sink<Quad> sink = new Sink<Quad>() {
            @Override
            public void send(final Quad quad) {
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        action.execute(new Quadruple(
                                quad.getGraph(), quad.getSubject(),
                                quad.getPredicate(), quad.getObject()));
                    }
                });
            }

            @Override
            public void close() {
                threadPool.shutdown();
            }

            @Override
            public void flush() {
                
            }

        };

        LangRIOT parser;

        switch (format) {
            case TriG:
                // TODO define baseURI
                parser = RiotReader.createParserTriG(in, "", sink);
                break;
            case NQuads:
                parser = RiotReader.createParserNQuads(in, sink);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknow SerializationFormat: " + format);
        }

        parser.parse();
        
    }

    public static abstract class QuadrupleAction {

        public abstract void execute(Quadruple quad);

    }

}
