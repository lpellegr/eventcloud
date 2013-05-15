/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.tracker;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerImpl;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * A specific implementation of a tracker for a semantic content addressable
 * network. It takes into account the frequency of quadruples in order to load
 * balance the join operation.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class SemanticTrackerImpl extends TrackerImpl implements SemanticTracker {

    private static final long serialVersionUID = 150L;

    /**
     * ADL name of the semantic tracker component.
     */
    public static final String SEMANTIC_TRACKER_ADL =
            "fr.inria.eventcloud.tracker.SemanticTracker";

    /**
     * Empty constructor required by ProActive.
     */
    public SemanticTrackerImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        this.configurationProperty = "eventcloud.configuration";
        this.propertiesClass = EventCloudProperties.class;
        super.initComponentActivity(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticPeer getRandomSemanticPeer() {
        return (SemanticPeer) super.getRandomPeer();
    }

    // private static final long serialVersionUID = 150L;
    //
    // private static final Logger logger =
    // LoggerFactory.getLogger(SemanticTracker.class);
    //
    // // The frequency of data inserted in the network
    // private Map<Character, SemanticFrequencies> frequencies;
    //
    // private long occurencesTotalCount = 0;
    //
    // public SemanticTracker() {
    // super();
    // this.initialize();
    // }
    //
    // public SemanticTracker(String associatedNetworkName) {
    // super(associatedNetworkName);
    // this.initialize();
    // }
    //
    // public SemanticPeer getRandomPeer() {
    // return (SemanticPeer) super.getRandomPeer();
    // }
    //
    // public String dump() {
    // StringBuffer buf = new StringBuffer();
    //
    // for (Entry<Character, SemanticFrequencies> entry :
    // this.frequencies.entrySet()) {
    // buf.append(entry.getKey());
    // buf.append(" ");
    // buf.append(this.computeFrequency(entry.getValue()
    // .getSubjectCharacterFrequency()
    // .getOccurencesCount()));
    // buf.append(" ");
    // buf.append(this.computeFrequency(entry.getValue()
    // .getPredicateCharacterFrequency()
    // .getOccurencesCount()));
    // buf.append(" ");
    // buf.append(this.computeFrequency(entry.getValue()
    // .getObjectCharacterFrequency()
    // .getOccurencesCount()));
    // buf.append("\n");
    // }
    // return buf.toString();
    // }
    //
    // private void initialize() {
    // this.frequencies = new HashMap<Character, SemanticFrequencies>();
    //
    // List<Character> legalPrefixes = new ArrayList<Character>();
    // legalPrefixes.addAll(this.getIntermediateCharacters(new
    // CharactersInterval(
    // '0', '9')));
    // legalPrefixes.addAll(this.getIntermediateCharacters(new
    // CharactersInterval(
    // 'A', 'Z')));
    // legalPrefixes.addAll(this.getIntermediateCharacters(new
    // CharactersInterval(
    // 'a', 'z')));
    //
    // for (Character c : legalPrefixes) {
    // this.frequencies.put(c, new SemanticFrequencies());
    // }
    // }
    //
    // public Peer findPeerToJoin() {
    // double f = ProActiveRandom.nextDouble();
    //
    // Character graphCharacter =
    // this.findCharacterByFrequency(f, SemanticFrequencies.Type.GRAPH);
    //
    // Character subjectCharacter =
    // this.findCharacterByFrequency(
    // f, SemanticFrequencies.Type.SUBJECT);
    // Character predicateCharacter =
    // this.findCharacterByFrequency(
    // f, SemanticFrequencies.Type.PREDICATE);
    // Character objectCharacter =
    // this.findCharacterByFrequency(
    // f, SemanticFrequencies.Type.OBJECT);
    //
    // if (graphCharacter == null || subjectCharacter == null
    // || predicateCharacter == null || objectCharacter == null) {
    // logger.debug("findPeerToJoin return a peer elected randomly (no frequency found).");
    // return this.getRandomPeer();
    // }
    //
    // StringElement subjectElt =
    // new StringElement(subjectCharacter.toString());
    // StringElement predicateElt =
    // new StringElement(predicateCharacter.toString());
    // StringElement objectElt = new StringElement(objectCharacter.toString());
    //
    // logger.debug("findPeerToJoin return a peer based on the frequency found.");
    // try {
    // return ((LookupResponse) PAFuture.getFutureValue(this.getRandomPeer()
    // .send(
    // new LookupRequest(new StringCoordinate(
    // subjectElt, predicateElt, objectElt))))).getPeerFound();
    // } catch (DispatchException e) {
    // e.printStackTrace();
    // return null;
    // }
    // }
    //
    // private Character findCharacterByFrequency(double f,
    // SemanticFrequencies.Type type) {
    // double sum = 0;
    // CharacterFrequency cf = null;
    //
    // for (Entry<Character, SemanticFrequencies> entry :
    // this.frequencies.entrySet()) {
    // switch (type) {
    // case SUBJECT:
    // cf = entry.getValue().getSubjectCharacterFrequency();
    // break;
    // case PREDICATE:
    // cf = entry.getValue().getPredicateCharacterFrequency();
    // break;
    // case OBJECT:
    // cf = entry.getValue().getObjectCharacterFrequency();
    // break;
    // }
    //
    // if (cf.getOccurencesCount() > 0
    // && f > sum
    // && f <= sum
    // + this.computeFrequency(cf.getOccurencesCount())) {
    // return entry.getKey();
    // }
    //
    // sum += this.computeFrequency(cf.getOccurencesCount());
    // }
    //
    // return null;
    // }
    //
    // public void updateFrequency(Quadruple quad) {
    // this.frequencies.get(
    // SemanticHelper.parseElement(quad.getGraph().toString()).charAt(
    // 0))
    // .getGraphCharacterFrequency()
    // .incrementOccurencesCount();
    //
    // this.frequencies.get(
    // SemanticHelper.parseElement(quad.getSubject().toString())
    // .charAt(0))
    // .getSubjectCharacterFrequency()
    // .incrementOccurencesCount();
    //
    // this.frequencies.get(
    // SemanticHelper.parseElement(quad.getPredicate().toString())
    // .charAt(0))
    // .getPredicateCharacterFrequency()
    // .incrementOccurencesCount();
    //
    // this.frequencies.get(
    // SemanticHelper.parseElement(quad.getObject().toString())
    // .charAt(0))
    // .getObjectCharacterFrequency()
    // .incrementOccurencesCount();
    //
    // this.occurencesTotalCount++;
    // }
    //
    // private double computeFrequency(long occurences) {
    // if (occurences == 0 || this.occurencesTotalCount == 0) {
    // return 0;
    // }
    //
    // return (double) occurences / this.occurencesTotalCount;
    // }
    //
    // private List<Character> getIntermediateCharacters(CharactersInterval
    // interval) {
    // int lowerIndex = interval.getLowerBound();
    // int upperIndex = interval.getUpperBound();
    // int size = upperIndex - lowerIndex + 1;
    //
    // List<Character> result = new ArrayList<Character>(size);
    // for (int i = lowerIndex; i < lowerIndex + size; i++) {
    // result.add((char) i);
    // }
    //
    // return result;
    // }
    //
    // public static class CharactersInterval {
    //
    // private final char lowerBound;
    //
    // private final char upperBound;
    //
    // public CharactersInterval(char lowerBound, char upperBound) {
    // super();
    // this.lowerBound = lowerBound;
    // this.upperBound = upperBound;
    // }
    //
    // public char getLowerBound() {
    // return this.lowerBound;
    // }
    //
    // public char getUpperBound() {
    // return this.upperBound;
    // }
    //
    // }
    //
    // public static class CharacterFrequency implements Serializable {
    //
    // private static final long serialVersionUID = 150L;
    //
    // private long occurencesCount;
    //
    // public CharacterFrequency(long occurencesCount) {
    // super();
    // this.occurencesCount = occurencesCount;
    // }
    //
    // public long getOccurencesCount() {
    // return this.occurencesCount;
    // }
    //
    // public void incrementOccurencesCount() {
    // this.occurencesCount++;
    // }
    //
    // @Override
    // public String toString() {
    // return Long.toString(this.occurencesCount);
    // }
    //
    // }
    //
    // public static class SemanticFrequencies implements Serializable {
    //
    // private static final long serialVersionUID = 150L;
    //
    // public enum Type {
    // GRAPH, SUBJECT, PREDICATE, OBJECT,
    // }
    //
    // private CharacterFrequency graphCharacterFrequency;
    //
    // private CharacterFrequency subjectCharacterFrequency;
    //
    // private CharacterFrequency predicateCharacterFrequency;
    //
    // private CharacterFrequency objectCharacterFrequency;
    //
    // public SemanticFrequencies() {
    // this.graphCharacterFrequency = new CharacterFrequency(0);
    // this.subjectCharacterFrequency = new CharacterFrequency(0);
    // this.predicateCharacterFrequency = new CharacterFrequency(0);
    // this.objectCharacterFrequency = new CharacterFrequency(0);
    // }
    //
    // public SemanticFrequencies(CharacterFrequency graphCharacterFrequency,
    // CharacterFrequency subjectCharacterFrequency,
    // CharacterFrequency predicateCharacterFrequency,
    // CharacterFrequency objectCharacterFrequency) {
    // this.graphCharacterFrequency = graphCharacterFrequency;
    // this.subjectCharacterFrequency = subjectCharacterFrequency;
    // this.predicateCharacterFrequency = predicateCharacterFrequency;
    // this.objectCharacterFrequency = objectCharacterFrequency;
    // }
    //
    // public CharacterFrequency getGraphCharacterFrequency() {
    // return this.graphCharacterFrequency;
    // }
    //
    // public CharacterFrequency getSubjectCharacterFrequency() {
    // return this.subjectCharacterFrequency;
    // }
    //
    // public CharacterFrequency getPredicateCharacterFrequency() {
    // return this.predicateCharacterFrequency;
    // }
    //
    // public CharacterFrequency getObjectCharacterFrequency() {
    // return this.objectCharacterFrequency;
    // }
    //
    // }

}
