package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;

import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.messages.response.can.ShutdownResponse;

/**
 * Request used to shutdown the datastores associated to each peer over the
 * overlay.
 * 
 * @author lpellegr
 */
public class ShutdownRequest extends AnycastRequest {

    private static final long serialVersionUID = 1L;

    public ShutdownRequest() {
        super(new DefaultAnycastConstraintsValidator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<? extends RequestResponseMessage<StringCoordinate>, StringCoordinate> getRouter() {
        return new AnycastRequestRouter<AnycastRequest>() {
            @Override
            public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                       AnycastRequest request) {
                ((SemanticDatastore) overlay.getDatastore()).close(true);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnycastResponse createResponse() {
        return new ShutdownResponse(this);
    }

}
