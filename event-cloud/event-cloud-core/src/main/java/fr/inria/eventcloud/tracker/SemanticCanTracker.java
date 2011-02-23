package fr.inria.eventcloud.tracker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.can.LookupReply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.openrdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.tracker.SemanticFrequencies.Type;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * A specific implementation of a tracker for a semantic content addressable
 * network. It takes into account the frequency of triples in order to load
 * balance the join operation.
 * 
 * @author lpellegr
 */
public class SemanticCanTracker extends Tracker {

	private static final long serialVersionUID = 1L;

	private static final transient Logger logger = LoggerFactory.getLogger(SemanticCanTracker.class);
	
	// The frequency of prefix of data inserted in the network
	private Map<Character, SemanticFrequencies> frequencies;

	private long occurencesTotalCount = 0;

	public SemanticCanTracker() {
        super();
        this.initialize();
    }

    public SemanticCanTracker(String associatedNetworkName) {
        super(OverlayType.CAN, associatedNetworkName);
        this.initialize();
    }

    public SemanticCanTracker(String associatedNetworkName, String trackerName) {
        super(OverlayType.CAN, associatedNetworkName, trackerName);
        this.initialize();
    }
	
    public String dump() {
    	StringBuffer buf = new StringBuffer();
    	
    	for (Entry<Character, SemanticFrequencies> entry : this.frequencies.entrySet()) {
    		buf.append(entry.getKey());
    		buf.append(" ");
    		buf.append(this.computeFrequency(entry.getValue().getSubjectCharacterFrequency().getOccurencesCount()));
    		buf.append(" ");
    		buf.append(this.computeFrequency(entry.getValue().getPredicateCharacterFrequency().getOccurencesCount()));
    		buf.append(" ");
    		buf.append(this.computeFrequency(entry.getValue().getObjectCharacterFrequency().getOccurencesCount()));
    		buf.append("\n");
    	}
    	return buf.toString();
    }
    
    private void initialize() {
    	this.frequencies = new HashMap<Character, SemanticFrequencies>();

		List<Character> legalPrefixes = new ArrayList<Character>();
		legalPrefixes.addAll(this
				.getIntermediateCharacters(new CharactersInterval('0', '9')));
		legalPrefixes.addAll(this
				.getIntermediateCharacters(new CharactersInterval('A', 'Z')));
		legalPrefixes.addAll(this
				.getIntermediateCharacters(new CharactersInterval('a', 'z')));

		for (Character c : legalPrefixes) {
			this.frequencies.put(c, new SemanticFrequencies());
		}
    }
    
	public Peer findPeerToJoin() {
		double f = ProActiveRandom.nextDouble();
		
		Character subjectCharacter = this.findCharacterByFrequency(f, Type.SUBJECT);
		Character predicateCharacter = this.findCharacterByFrequency(f, Type.PREDICATE);
		Character objectCharacter = this.findCharacterByFrequency(f, Type.OBJECT);
		
		if (subjectCharacter == null || predicateCharacter == null || objectCharacter == null) {
			logger.debug("findPeerToJoin return a peer elected randomly (no frequency found).");
			return this.getRandomPeer();
		}
		
		StringElement subjectElt = subjectCharacter == null ? null : new StringElement(subjectCharacter.toString());
		StringElement predicateElt = predicateCharacter == null ? null : new StringElement(predicateCharacter.toString());
		StringElement objectElt = objectCharacter == null ? null : new StringElement(objectCharacter.toString());
		
		logger.debug("findPeerToJoin return a peer based on the frequency found.");
		return ((LookupReply)
				PAFuture.getFutureValue(
						this.getRandomPeer().send(
								new LookupRequest(
										new Coordinate(
												subjectElt, predicateElt, objectElt))))).getPeerFound();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Peer getLandmarkPeerToJoin() {
		return this.findPeerToJoin();
	}

	private Character findCharacterByFrequency(double f, Type type) {
		double sum = 0;
		CharacterFrequency cf = null;
		
		for (Entry<Character, SemanticFrequencies> entry : this.frequencies.entrySet()) {
			switch (type) {
			case SUBJECT:
				cf = entry.getValue().getSubjectCharacterFrequency();
				break;
			case PREDICATE:
				cf = entry.getValue().getPredicateCharacterFrequency();
				break;
			case OBJECT:
				cf = entry.getValue().getObjectCharacterFrequency();
				break;
			}
			
			if (cf.getOccurencesCount() > 0
					&& f  > sum 
						&& f <= sum + this.computeFrequency(cf.getOccurencesCount())) {
					return entry.getKey(); 
			}

			sum += this.computeFrequency(cf.getOccurencesCount());
		}
	
		return null;
	} 

	public void updateFrequency(Statement stmt) {
		this.frequencies.get(
				SemanticHelper.parseTripleForLoadBalancing(
						stmt.getSubject().toString()).charAt(0))
							.getSubjectCharacterFrequency()
								.incrementOccurencesCount();
		
		this.frequencies.get(
				SemanticHelper.parseTripleForLoadBalancing(
						stmt.getPredicate().toString()).charAt(0))
							.getPredicateCharacterFrequency()
								.incrementOccurencesCount();
		
		this.frequencies.get(
				SemanticHelper.parseTripleForLoadBalancing(
						stmt.getObject().toString()).charAt(0))
							.getObjectCharacterFrequency()
								.incrementOccurencesCount();

		this.occurencesTotalCount++;
	}

	private double computeFrequency(long occurences) {
		if (occurences == 0 || this.occurencesTotalCount == 0) {
			return 0;
		}

		return (double) occurences / this.occurencesTotalCount;
	}

	private List<Character> getIntermediateCharacters(
			CharactersInterval interval) {
		int lowerIndex = (int) interval.getLowerBound();
		int upperIndex = (int) interval.getUpperBound();
		int size = upperIndex - lowerIndex + 1;

		List<Character> result = new ArrayList<Character>(size);
		for (int i = lowerIndex; i < lowerIndex + size; i++) {
			result.add((char) i);
		}

		return result;
	}

	public static void main(String[] args) {
	}
}

class SemanticFrequencies implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {
		SUBJECT,
		PREDICATE,
		OBJECT
	}
	
	private CharacterFrequency subjectCharacterFrequency;

	private CharacterFrequency predicateCharacterFrequency;

	private CharacterFrequency objectCharacterFrequency;

	public SemanticFrequencies() {
		this.subjectCharacterFrequency = new CharacterFrequency(0);
		this.predicateCharacterFrequency = new CharacterFrequency(0);
		this.objectCharacterFrequency = new CharacterFrequency(0);
	}

	public SemanticFrequencies(CharacterFrequency subjectCharacterFrequency,
			CharacterFrequency predicateCharacterFrequency,
			CharacterFrequency objectCharacterFrequency) {
		super();
		this.subjectCharacterFrequency = subjectCharacterFrequency;
		this.predicateCharacterFrequency = predicateCharacterFrequency;
		this.objectCharacterFrequency = objectCharacterFrequency;
	}

	public CharacterFrequency getSubjectCharacterFrequency() {
		return this.subjectCharacterFrequency;
	}

	public CharacterFrequency getPredicateCharacterFrequency() {
		return this.predicateCharacterFrequency;
	}

	public CharacterFrequency getObjectCharacterFrequency() {
		return this.objectCharacterFrequency;
	}

}

class CharacterFrequency implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long occurencesCount;

	public CharacterFrequency(long occurencesCount) {
		super();
		this.occurencesCount = occurencesCount;
	}

	public long getOccurencesCount() {
		return this.occurencesCount;
	}

	public void incrementOccurencesCount() {
		this.occurencesCount++;
	}
	
	@Override
	public String toString() {
		return "" + this.occurencesCount;
	}

}

class CharactersInterval {

	private final char lowerBound;

	private final char upperBound;

	public CharactersInterval(char lowerBound, char upperBound) {
		super();
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public char getLowerBound() {
		return this.lowerBound;
	}

	public char getUpperBound() {
		return this.upperBound;
	}

}
