package org.objectweb.proactive.extensions.p2p.structured.overlay;

import org.objectweb.proactive.extensions.p2p.structured.messages.PendingReplyEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.AbstractReply;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.MergeOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;

/**
 * 
 * @author lpellegr
 */
public class BasicCANOverlay extends AbstractCanOverlay {

	private static final long serialVersionUID = 1L;

	public BasicCANOverlay() {
		super(new RequestReplyManager() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public PendingReplyEntry mergeResponseReceived(AbstractReply<?> msg) {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
	
	@Override
	protected void affectDataReceived(Object dataReceived) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void mergeDataReceived(MergeOperation msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Object retrieveAllData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object getDataIn(Zone zone) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void removeDataIn(Zone zone) {
		// TODO Auto-generated method stub
		
	}

}