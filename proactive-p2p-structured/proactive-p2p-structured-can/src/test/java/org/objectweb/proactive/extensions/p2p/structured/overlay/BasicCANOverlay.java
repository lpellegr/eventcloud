package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.util.Set;

import org.objectweb.proactive.Service;
import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;
import org.objectweb.proactive.extensions.p2p.structured.api.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PostProcessException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PreProcessException;
import org.objectweb.proactive.extensions.p2p.structured.messages.PendingReplyEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.AbstractReply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.AbstractRequest;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.MergeOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.QueryManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;

/**
 * 
 * @author lpellegr
 */
public class BasicCANOverlay extends AbstractCanOverlay {

	private static final long serialVersionUID = 1L;

	public BasicCANOverlay() {
		super(new QueryManager() {

			private static final long serialVersionUID = 1L;

			protected AbstractRequest<?> preProcess(Request query)
					throws PreProcessException {
				return null;
			}

			protected Reply postProcess(
					AbstractReply<?> response)
					throws PostProcessException {
				return null;
			}

			@Override
			public PendingReplyEntry mergeResponseReceived(
					AbstractReply<?> msg) {
				return null;
			}

		});
	}

	protected void affectDataReceived(Object dataReceived) {
		// TODO Auto-generated method stub

	}

	protected void mergeDataReceived(MergeOperation msg) {
		// TODO Auto-generated method stub

	}

	public void processRequest(Service service, Request request) {
		// TODO Auto-generated method stub

	}

	protected Set<? extends Object> retrieveAllData() {
		// TODO Auto-generated method stub
		return null;
	}

	protected Object getDataIn(Zone zone) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void removeDataIn(Zone zone) {
		// TODO Auto-generated method stub
	}

}