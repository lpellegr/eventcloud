package fr.inria.eventcloud.overlay.can;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.MergeOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.config.EventCloudProperties;
import fr.inria.eventcloud.datastore.OwlimDatastore;
import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.overlay.SemanticStructuredOverlay;
import fr.inria.eventcloud.overlay.SparqlRequestResponseManager;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * This class is a concrete implementation of {@link AbstractCanOverlay} for a
 * semantic CAN network. In particular, it overrides methods that are necessary
 * to transfer data when peers join or leave.
 * 
 * @author lpellegr
 */
public class SemanticCanOverlay extends AbstractCanOverlay implements SemanticStructuredOverlay {

	private static final long serialVersionUID = 1L;

	private static final transient Logger logger = 
			LoggerFactory.getLogger(SemanticCanOverlay.class);

	protected transient SemanticDatastore datastore;

	/**
	 * Constructs a new overlay.
	 */
	public SemanticCanOverlay() {
		super(new SparqlRequestResponseManager());
	}
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public void initActivity(Body body) {
		this.datastore = new OwlimDatastore(true);
		this.datastore.open();
		super.initActivity(body);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endActivity(Body body) {
		this.datastore.close();
	}

	@Override
	protected Object getDataIn(Zone zone) {
		Set<Statement> statementsToTransfert = new HashSet<Statement>();
		ClosableIterable<Statement> result = this.datastore.sparqlConstruct(
				EventCloudProperties.DEFAULT_CONTEXT,
				"CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");

		ClosableIterator<Statement> it = result.iterator();
		Statement stmt;
		String subject;
		String predicate;
		String object;

		while (it.hasNext()) {
			stmt = it.next();

			subject = SemanticHelper.parseTripleElement(
						stmt.getSubject().toString());
			predicate = SemanticHelper.parseTripleElement(
						stmt.getPredicate().toString());
			object = SemanticHelper.parseTripleElement(
						stmt.getObject().toString());

			// Yeah, manual filtering is really ugly!
			if (subject.compareTo(zone.getLowerBound(0).toString()) >= 0
					&& subject.compareTo(zone.getUpperBound(0).toString()) < 0
					&& predicate.compareTo(zone.getLowerBound(1).toString()) >= 0
					&& predicate.compareTo(zone.getUpperBound(1).toString()) < 0
					&& object.compareTo(zone.getLowerBound(2).toString()) >= 0
					&& object.compareTo(zone.getUpperBound(2).toString()) < 0) {

				statementsToTransfert.add(stmt);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Data have been retrieved for transfert");
		}

		return new ClosableIterableWrapper(
				SemanticHelper.generateClosableIterable(statementsToTransfert));
	}

	@Override
	protected void removeDataIn(Zone zone) {
		ClosableIterable<Statement> result = this.datastore.sparqlConstruct(
				EventCloudProperties.DEFAULT_CONTEXT,
				"CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");

		ClosableIterator<Statement> it = result.iterator();
		Statement stmt;
		String subject;
		String predicate;
		String object;

		while (it.hasNext()) {
			stmt = it.next();

			subject = SemanticHelper.parseTripleElement(
						stmt.getSubject().toString());
			predicate = SemanticHelper.parseTripleElement(
						stmt.getPredicate().toString());
			object = SemanticHelper.parseTripleElement(
						stmt.getObject().toString());

			if (subject.compareTo(zone.getLowerBound(0).toString()) >= 0
					&& subject.compareTo(zone.getUpperBound(0).toString()) < 0
					&& predicate.compareTo(zone.getLowerBound(1).toString()) >= 0
					&& predicate.compareTo(zone.getUpperBound(1).toString()) < 0
					&& object.compareTo(zone.getLowerBound(2).toString()) >= 0
					&& object.compareTo(zone.getUpperBound(2).toString()) < 0) {
				this.datastore.removeStatement(EventCloudProperties.DEFAULT_CONTEXT, stmt);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Data have been removed due to transfert");
		}
	}

	@Override
	protected void affectDataReceived(Object dataReceived) {
		ClosableIterator<Statement> data = 
			((ClosableIterableWrapper) dataReceived).toRDF2Go().iterator();
		
		this.datastore.addAll(EventCloudProperties.DEFAULT_CONTEXT, data);
	}

	@SuppressWarnings("unchecked")
	protected void mergeDataReceived(MergeOperation msg) {
		ClosableIterator<Statement> data = 
			((ClosableIterable<Statement>) msg.getDataToReallocate()).iterator();
		
		this.datastore.addAll(EventCloudProperties.DEFAULT_CONTEXT, data);
	}

	@Override
	protected ClosableIterable<Statement> retrieveAllData() {
		return this.datastore.sparqlConstruct(
					EventCloudProperties.DEFAULT_CONTEXT,
					"CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
	}

	public SemanticDatastore getDatastore() {
		return this.datastore;
	}

}
