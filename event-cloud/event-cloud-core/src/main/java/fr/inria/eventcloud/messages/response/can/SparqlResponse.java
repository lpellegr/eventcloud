package fr.inria.eventcloud.messages.response.can;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastResponseRouter;

import fr.inria.eventcloud.messages.request.can.SparqlRequest;
import fr.inria.eventcloud.overlay.SparqlRequestResponseManager;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;

/**
 * @author lpellegr
 */
public abstract class SparqlResponse extends AnycastResponse {

    private static final long serialVersionUID = 1L;

    private List<Byte[]> serializedResults;
    
    public SparqlResponse(SparqlRequest query) {
        super(query);
        this.serializedResults = new ArrayList<Byte[]>();
    }

	/**
	 * Returns the filter time in ms (i.e. the time wasted to filter the results
	 * returned by the sub-queries). A value equals to 0 means the initial query
	 * has not been decomposed.
	 * 
	 * @return the filter time in ms (i.e. the time wasted to filter the results
	 *         returned by the sub-queries). A value equals to 0 means the
	 *         initial query has not been decomposed.
	 */
	public long getFilterTime() {
		return 0;
	}

	/**
	 * Returns a value representing the time spent in querying the datastores.
	 * 
	 * @return a value representing the time spent in querying the datastores.
	 */
	public long getQueryDatastoreTime() {
		return 0;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void merge(AnycastResponse subResponse) {
		for (Byte[] bytes : ((SparqlResponse) subResponse).getSerializedResults()) {
			this.serializedResults.add(bytes);	
		}
	}
	
	public List<Byte[]> getSerializedResults() {
		return this.serializedResults;
	}

	public synchronized List<ClosableIterableWrapper> getDeserializedResults() {
		List<ClosableIterableWrapper> deserializedResults = new ArrayList<ClosableIterableWrapper>();
		for (Byte[] value : this.serializedResults) {
			try {
				deserializedResults
						.add((ClosableIterableWrapper) ByteToObjectConverter.ObjectStream
								.convert(toPrimitive(value)));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return deserializedResults;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Router<? extends RequestResponseMessage<StringCoordinate>, StringCoordinate> getRouter() {
		return new AnycastResponseRouter<AnycastResponse>() {
			@Override
			public void makeDecision(StructuredOverlay overlay, AnycastResponse response) {
				Future<ClosableIterableWrapper> result = 
					((SparqlRequestResponseManager) overlay.getRequestResponseManager())
							.getPendingRequestsResult().remove(response.getId());
				// result != null <-> we are on a peer validating the constraints 
				if (result != null) {
					// ensures that the query datastore has terminated
					// before to send back the request 
					try {
						((SparqlResponse) response).getSerializedResults().add(
								toObject(ObjectToByteConverter.ObjectStream.convert(result.get())));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				
				super.makeDecision(overlay, response);
			}
		};
	}

	/**
	 * Converts an array of primitive bytes to objects.
	 * 
	 * This method returns {@code null} for a {@code null} input array.
	 * 
	 * @param byteArray
	 *            a byte array.
	 * 
	 * @return a {@code Byte} array, {@code null} if null array input.
	 */
	public static Byte[] toObject(byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}
		
		Byte[] box = new Byte[byteArray.length];
		for (int i = 0; i < box.length; i++) {
			box[i] = byteArray[i];
		}
		return box;
	}

	/**
	 * Converts an array of object Bytes to primitives handling {@code null}.
	 * 
	 * This method returns {@code null} for a {@code null} input array.
	 * 
	 * @param byteArray
	 *            a Byte array, may be null.
	 *            
	 * @return a byte array, null if null array input.
	 */
	public static byte[] toPrimitive(Byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}
		
		byte[] unbox = new byte[byteArray.length];
		for (int i = 0; i <unbox.length; i++) {
			unbox[i] = byteArray[i];
		}
		return unbox;
	}

}
