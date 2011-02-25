package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.messages.reply.Reply;

/**
 * {@link ReplyEntry} is used for storing some information (e.g. the
 * number of replies expected, the number of replies received, etc.) about a
 * request which is being handled. These information are useful to create a
 * synchronization point.
 * <p>
 * Each time a new reply is received, it has to be merged with the previous one
 * before to set it as the new value associated to this entry.
 * 
 * @author lpellegr
 */
public class ReplyEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Status {
		/**
		 * This status means that the number of replies expected 
		 * is equals to the number of replies received.
		 */
		FINAL_REPLY_RECEIVED, 
		/**
		 * This status means that the number of replies received 
		 * is smaller than the number of replies expected.
		 */
		RECEIPT_IN_PROGRESS
	};

	private Status status = Status.RECEIPT_IN_PROGRESS;

	/**
	 * The reply corresponding to the last reply which has been merged.
	 */
	private Reply<?> reply;

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
	public ReplyEntry(int expectedRepliesCount) {
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
	public Reply<?> getReply() {
		return this.reply;
	}

	public synchronized void incrementRepliesCount(int increment) {
		this.repliesCount += increment;
		if (this.repliesCount == this.expectedRepliesCount) {
			this.status = Status.FINAL_REPLY_RECEIVED;
		}
	}

	/**
	 * Sets the specified message as response.
	 * 
	 * @param response 
	 * 			the new response to associate to this entry.
	 */
	public synchronized void setResponse(Reply<?> response) {
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
