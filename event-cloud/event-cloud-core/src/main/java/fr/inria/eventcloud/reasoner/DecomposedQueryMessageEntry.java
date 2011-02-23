package fr.inria.eventcloud.reasoner;


/**
 * @author lpellegr
 */
public class DecomposedQueryMessageEntry {

    public enum Status {
        ALL_RESPONSES_RECEIVED, RECEIPT_IN_PROGRESS
    };

    private Status status = Status.RECEIPT_IN_PROGRESS;

    private ParsedSparqlQuery decomposedSparqlQuery;

    private int numberOfResponseWaiting = 0;

    private int numberOfResponseReceived = 0;

    public DecomposedQueryMessageEntry(ParsedSparqlQuery decomposedSparqlQuery) {
        super();
        this.decomposedSparqlQuery = decomposedSparqlQuery;
        this.numberOfResponseWaiting = decomposedSparqlQuery.getSubQueries().size();
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * @return the response
     */
    public ParsedSparqlQuery getDecomposedSparqlQuery() {
        return this.decomposedSparqlQuery;
    }

    /**
     * @return the numberOfResponseWaiting
     */
    public int getNumberOfResponseWaiting() {
        return this.numberOfResponseWaiting;
    }

    /**
     * @return the numberOfResponseReceived
     */
    public int getNumberOfResponseReceived() {
        return this.numberOfResponseReceived;
    }

    public void incrementNbResponseReceived(int increment) {
        this.numberOfResponseReceived++;
        if (this.numberOfResponseReceived == this.numberOfResponseWaiting) {
            this.status = Status.ALL_RESPONSES_RECEIVED;
        }
    }

}
