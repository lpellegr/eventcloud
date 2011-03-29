package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;

import fr.inria.eventcloud.messages.response.can.ShutdownResponse;
import fr.inria.eventcloud.overlay.can.SemanticCanOverlay;

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
	public Router<? extends RequestResponseMessage<Coordinate>, Coordinate> getRouter() {
		return new AnycastRequestRouter<AnycastRequest>() {
			@Override
			public void onPeerValidatingKeyConstraints(
							AbstractCanOverlay overlay, AnycastRequest request) {
				((SemanticCanOverlay) overlay).getDatastore().close();
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
