package fr.inria.eventcloud.messages.request.can;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Convenient class used to serialize the parameters at the creation of the
 * object and to deserialize them when they are used. This avoids to serialize
 * and to deserialize the parameters at each hop from a peer to an another.
 * 
 * @author lpellegr
 * 
 * @see AddStatementRequest
 * @see RemoveStatementRequest
 */
public abstract class StatementRequest extends ForwardRequest {

    private static final long serialVersionUID = 1L;

    private byte[] context;

    private byte[] statement;

    public StatementRequest(URI context, Statement stmt) {
        super(SemanticHelper.createCoordinateWithoutNullValues(stmt));

        try {
            this.context =
                    ObjectToByteConverter.ObjectStream.convert(checkNotNull(context));
            this.statement = ObjectToByteConverter.ObjectStream.convert(stmt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract public void onDestinationReached(StructuredOverlay overlay,
                                              URI context, Statement stmt);

    public Router<ForwardRequest, StringCoordinate> getRouter() {
        return new UnicastRequestRouter<ForwardRequest>() {
            @Override
            protected void onDestinationReached(StructuredOverlay overlay,
                                                ForwardRequest msg) {
                URI unserializedContext = null;
                Statement unserializedStatement = null;
                try {
                    unserializedContext =
                            (URI) ByteToObjectConverter.ObjectStream.convert(context);
                    unserializedStatement =
                            (Statement) ByteToObjectConverter.ObjectStream.convert(statement);

                    StatementRequest.this.onDestinationReached(
                            overlay, unserializedContext, unserializedStatement);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            };
        };
    }

}
