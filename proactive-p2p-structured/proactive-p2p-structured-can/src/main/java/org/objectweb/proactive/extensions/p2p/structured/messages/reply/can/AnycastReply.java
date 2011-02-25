package org.objectweb.proactive.extensions.p2p.structured.messages.reply.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingList;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.Reply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;

/**
 * @author lpellegr
 */
public abstract class AnycastReply extends Reply<Coordinate> {

    private static final long serialVersionUID = 1L;

    private AnycastRoutingList anycastRoutingList = new AnycastRoutingList();

//    protected final List<ObjectAsByteArray> dataRetrieved = new ArrayList<ObjectAsByteArray>();

//    /**
//     * Field used to store data which have been merged when response is returned
//     * at the sender of the query. It is used to avoid to perform several times
//     * the merge operation if it is necessary to access multiple times to the
//     * data merged. Because the merge is performed only on the receiver of the
//     * query, this field is marked as transient to avoid to serialize it.
//     */
//    protected transient T dataMerged;

//    public AnycastReply(
//            AnycastReply<?> response,
//            Coordinate keyToReach) {
//        super(response, keyToReach);
//        this.anycastRoutingList = response.getAnycastRoutingList();
//    }

    public AnycastReply(
            AnycastRequest query,
            Coordinate keyToReach) {
        super(query, keyToReach);
        this.anycastRoutingList = query.getAnycastRoutingList();
    }

//    /**
//     * Query the local data store in order to retrieved data which needs to be
//     * routed back.
//     * 
//     * @param overlay
//     *            the {@link StructuredOverlay} on which the call to the
//     *            datastore is performed.
//     * @return the data retrieved.
//     */
//    public abstract T queryDataStore(StructuredOverlay overlay);
//
//    /**
//     * Query the datastore and adds the data returned to the list of data
//     * retrieved.
//     * 
//     * @param overlay
//     *            the {@link StructuredOverlay} on which the call to the
//     *            datastore is performed.
//     */
//    public void queryDataStoreAndStoreData(StructuredOverlay overlay) {
//        this.storeData(this.queryDataStore(overlay));
//    }

//    /**
//     * Added the specified data to the list of data retrieved by the current
//     * response.
//     * 
//     * @param data
//     *            the data to add to the list of data retrieved.
//     */
//    public void storeData(Object data) {
//        try {
//            this.dataRetrieved.add(
//                    new ObjectAsByteArray(
//                            ObjectToByteConverter.ObjectStream.convert(data)));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    /**
//     * Removes all the elements in the list of data retrieved.
//     */
//    public void clearData() {
//    	this.dataRetrieved.clear();
//    }
    
//    /**
//     * Adds all the specified data to the list of data retrieved.
//     * 
//     * @param data
//     *            the data to add to the list of data retrieved.
//     */
//    public void addAll(List<ObjectAsByteArray> data) {
//        this.dataRetrieved.addAll(data);
//    }

//    /**
//     * Merges two data.
//     * 
//     * @param data1
//     *            the first data.
//     * @param data2
//     *            the second data.
//     * @return data1 with data2 merged into.
//     */
//    public abstract T merge(T data1, T data2);

//    /**
//     * Merges the data retrieved and returns them with concrete type.
//     * 
//     * @return the data retrieved merged.
//     */
//    public synchronized T mergeAndGetDataRetrieved() {
//        if (this.dataMerged == null) {
//            Iterator<ObjectAsByteArray> it = this.dataRetrieved.iterator();
//            if (it.hasNext()) {
//                this.dataMerged = this.convertToConcreteObject(it.next());
//            }
//            
//            while (it.hasNext()) {
//                this.dataMerged = this.merge(
//                        this.dataMerged, 
//                        this.convertToConcreteObject(it.next()));
//            }
//            
//            return this.dataMerged;
//        }
//
//        return this.dataMerged;
//    }
    
//    /**
//     * Converts an {@link ObjectAsByteArray} to its concrete type.
//     * 
//     * @param object the object to convert.
//     * 
//     * @return The object converted to the concrete type T.
//     */
//    @SuppressWarnings("unchecked")
//    private T convertToConcreteObject(ObjectAsByteArray object) {
//        T result = null;
//        try {
//             result = (T) ByteToObjectConverter.ObjectStream.convert(object.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        
//        return result;
//    }
    
//    /**
//     * Returns the data which have been merged or <code>null</code> if the merge
//     * operation hasn't been performed.
//     * 
//     * @return the data which have been merged or <code>null</code> if the merge
//     *         operation hasn't been performed.
//     */
//    public T getDataMerged() {
//        return this.dataMerged;
//    }
//    
    /**
     * Returns the {@link AnycastRoutingList} containing the
     * {@link AnycastRoutingEntry} to use in order to route the response.
     * 
     * @return the {@link AnycastRoutingList} containing the
     *         {@link AnycastRoutingEntry} to use in order to route the
     *         response.
     */
    public AnycastRoutingList getAnycastRoutingList() {
        return this.anycastRoutingList;
    }

	/**
	 * Merges the specified {@link AnycastReply} with the current one. When the
	 * merge operation is terminated, the specified reply is discarded.
	 * 
	 * @param subreply
	 *            the subreply to merge into the current one.
	 */
    public abstract void merge(AnycastReply subreply);
    
//    /**
//     * Returns the data retrieved.
//     * 
//     * @return the data retrieved.
//     */
//    public List<ObjectAsByteArray> getDataRetrieved() {
//        return this.dataRetrieved;
//    }

}
