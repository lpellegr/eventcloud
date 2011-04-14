package fr.inria.eventcloud.messages.response.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastResponseRouter;

import fr.inria.eventcloud.messages.request.can.ShutdownRequest;

/**
 * Response associated to {@link ShutdownRequest}. 
 * 
 * @author lpellegr
 */
public class ShutdownResponse extends AnycastResponse {

	public ShutdownResponse(AnycastRequest request) {
		super(request);
	}

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Router<? extends RequestResponseMessage<StringCoordinate>, StringCoordinate> getRouter() {
		return new AnycastResponseRouter<AnycastResponse>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void merge(AnycastResponse subResponse) {
		// no result, no action		
	}

}
