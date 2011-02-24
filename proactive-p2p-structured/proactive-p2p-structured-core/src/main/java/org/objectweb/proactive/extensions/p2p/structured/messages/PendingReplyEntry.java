package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.messages.reply.AbstractReply;

/**
 * {@link PendingReplyEntry} is used for storing some information (e.g. the
 * number of replies expected, the number of replies received, etc.) about a
 * request which is being handled. These information are useful to create a
 * synchronization point.
 * <p>
 * Each time a new reply is received, it has to be merged with the previous one
 * before to set it as the new value associated to this entry.
 * 
 * @author lpellegr
 */
public class PendingReplyEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Status {
		/**
		 * This status means that the number of replies expected 
		 * is equals to the number of responses received.
		 */
		ALL_REPLIES_RECEIVED, 
		/**
		 * This status means that the number of replies received 
		 * is smaller than the number of responses expected.
		 */
		RECEIPT_IN_PROGRESS
	};

	private Status status = Status.RECEIPT_IN_PROGRESS;

	/**
	 * The reply corresponding to the last response which has been merged.
	 */
	private AbstractReply<?> reply;

	/**
	 * The maximum number of replies expected.
	 */
	private final int expectedRepliesCount;

	/**
	 * The current number of responses received.
	 */
	private int repliesCount = 0;

	/**
	 * Constructs a new entry with the specified 
	 * <code>expectedResponsesNumber</code>.
	 * 
	 * @param expectedRepliesCount
	 * 				the maximum number of responses expected.
	 */
	public PendingReplyEntry(int expectedRepliesCount) {
	    this.expectedRepliesCount = expectedRepliesCount;
	}
	
	/**
	 * Returns the current number of responses received.
	 * 
	 * @return the current number of responses received.
	 */
	public int getRepliesCount() {
		return this.repliesCount;
	}

	/**
	 * Returns the maximum number of responses expected.
	 * 
	 * @return the maximum number of responses expected.
	 */
	public int getExpectedRepliesCount() {
		return this.expectedRepliesCount;
	}

    /**
	 * Returns the current status of the entry.
	 * 
	 * @return the current status of the entry.
	 */
	public Status getStatus() {
		return this.status;
	}

	/**
	 * Returns the last message merged.
	 * 
	 * @return the last message merged.
	 */
	public AbstractReply<?> getResponse() {
		return this.reply;
	}

	public synchronized void incrementResponsesNumber(int increment) {
		this.repliesCount += increment;
		if (this.repliesCount == this.expectedRepliesCount) {
			this.status = Status.ALL_REPLIES_RECEIVED;
		}
	}

	/**
	 * Sets the specified message as response.
	 * 
	 * @param response 
	 * 			the new response to associate to this entry.
	 */
	public synchronized void setResponse(AbstractReply<?> response) {
		this.reply = response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[repliesCount=" 
			   + this.repliesCount + ", expectedRepliesCount=" 
			   + this.expectedRepliesCount + "]";
	}
	
}
