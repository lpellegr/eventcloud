package fr.inria.eventcloud.overlay.can;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.UniversalBodyRemoteObjectAdapter;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.MergeOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;
import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.TriplePattern;
import org.ontoware.rdf2go.model.node.NodeOrVariable;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.ResourceOrVariable;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.UriOrVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.exceptions.NotActiveObjectException;
import fr.inria.eventcloud.exceptions.NotOnSameRuntimeException;
import fr.inria.eventcloud.kernel.SemanticSpaceOverlayKernel;
import fr.inria.eventcloud.overlay.SemanticQueryManager;
import fr.inria.eventcloud.overlay.SemanticStructuredOverlay;
import fr.inria.eventcloud.rdf2go.wrappers.ClosableIterableWrapper;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * {@link SemanticSpaceCanOverlay} is a concrete implementation of
 * {@link AbstractCANOverlay} for a semantic CAN network.
 * 
 * @author lpellegr
 */
public class SemanticSpaceCanOverlay extends AbstractCANOverlay implements
		SemanticStructuredOverlay {
	
	private static final long serialVersionUID = 1L;

	private static final transient Logger logger = 
			LoggerFactory.getLogger(SemanticSpaceCanOverlay.class);

	private URI spaceURI;

	private SemanticSpaceOverlayKernel localKernelReference;

	private SemanticSpaceOverlayKernel remoteKernelReference;

	/**
	 * Constructs a new overlay by using the specified <code>spaceURI</code> and
	 * <code>remoteKernelReference</code>.
	 * 
	 * @param spaceURI
	 *            the space to which the overlay belongs to.
	 * @param remoteKernelReference
	 *            the remote reference to the kernel in order to have access to
	 *            a datastore.
	 */
	public SemanticSpaceCanOverlay(URI spaceURI,
			SemanticSpaceOverlayKernel remoteKernelReference) {
		super(new SemanticQueryManager());
		this.spaceURI = spaceURI;
		this.remoteKernelReference = remoteKernelReference;
	}

	/**
	 * Returns the unique identifier from the body of a specified active object.
	 * 
	 * @param activeObject
	 *            the active to inspect in order to get the identifier.
	 * @return the unique identifier from the body of a specified active object.
	 * @throws NotActiveObjectException
	 *             if the specified <code>activeObject</code> isn't an active
	 *             object.
	 */
	private UniqueID getBodyID(Object activeObject)
			throws NotActiveObjectException {
		if (!(activeObject instanceof StubObject)) {
			throw new NotActiveObjectException();
		}

		UniversalBody body = 
			((UniversalBodyProxy) ((StubObject) activeObject).getProxy()).getBody();
		if (body instanceof UniversalBodyRemoteObjectAdapter) {
			return ((UniversalBodyRemoteObjectAdapter) body).getID();
		} else {
			return ((AbstractBody) body).getID();
		}
	}

	/**
	 * Returns the reified object from a specified {@link Body}.
	 * 
	 * @param body
	 *            the {@link Body} to use in order to retrieve the reified
	 *            object.
	 * @return the reified object (java object in the current JVM associated to
	 *         the specified body and so to the active object associated to this
	 *         body) from a specified {@link Body}.
	 */
	private Object getReifiedObject(Body body) {
		if (body instanceof UniversalBodyRemoteObjectAdapter) {
			// proactive powaaaaa :]
			return ((AbstractBody) ((UniversalBody) ((UniversalBodyRemoteObjectAdapter) body).getTarget())).getReifiedObject();
		} else {
			return ((AbstractBody) body).getReifiedObject();
		}
	}

	/**
	 * Sets the local kernel reference.
	 * 
	 * @throws NotActiveObjectException
	 *             if the specified <code>remoteKernelReference</code> is not an
	 *             active object.
	 * 
	 * @throws NotOnSameRuntimeException
	 *             if the reified object from the remote active object
	 *             <code>remoteKernelReference</code> is not on the same JVM as
	 *             the current overlay.
	 */
	private void setLocalKernelReference() throws NotActiveObjectException,
			NotOnSameRuntimeException {
		if (this.localKernelReference != null) {
			return;
		}

		LocalBodyStore bodyStore = LocalBodyStore.getInstance();
		Body body;

		if ((body = bodyStore.getLocalBody(
						this.getBodyID(
								this.remoteKernelReference))) == null) {
			throw new NotOnSameRuntimeException();
		} else {
		    this.localKernelReference = 
		        (SemanticSpaceOverlayKernel) this.getReifiedObject(body);
		}
	}

	public void initActivity(Body body) {
		super.initActivity(body);
		try {
			this.setLocalKernelReference();
		} catch (NotActiveObjectException e) {
		    logger.error("Cannot setup local kernel reference because the kernel is not an active object.", e);
		} catch (NotOnSameRuntimeException e) {
		    logger.error("Cannot setup local kernel reference because the kernel and the peer are not on the same JVM.", e);
		}
		if (logger.isDebugEnabled()) {
		    logger.debug("Local kernel reference setup.");
		}
	}

	protected Object getDataIn(Zone zone) {
		ClosableIterableWrapper result = new ClosableIterableWrapper(
				SemanticHelper
						.generateClosableIterable(new HashSet<Statement>()));

		Set<Statement> statementsToTransfert = new HashSet<Statement>();
		try {
			if (this.localKernelReference.hasStatements(this.spaceURI)) {
				result = new ClosableIterableWrapper(
						this.localKernelReference.sparqlConstruct(
								this.spaceURI,
								"CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }"));

				ClosableIterator<Statement> it = result.toRDF2Go().iterator();
				Statement stmt;
				String subject;
				String predicate;
				String object;

				while (it.hasNext()) {
					stmt = it.next();

					subject = SemanticHelper.parseTripleForLoadBalancing(stmt
							.getSubject().toString());
					predicate = SemanticHelper.parseTripleForLoadBalancing(stmt
							.getPredicate().toString());
					object = SemanticHelper.parseTripleForLoadBalancing(stmt
							.getObject().toString());

					// Yeah, manual filtering is really ugly!
					if (subject.compareTo(zone
							.getLowerBound(0).toString()) >= 0
							&& subject.compareTo(zone
									.getUpperBound(0).toString()) < 0
							&& predicate.compareTo(zone
									.getLowerBound(1).toString()) >= 0
							&& predicate.compareTo(zone
									.getUpperBound(1).toString()) < 0
							&& object.compareTo(zone
									.getLowerBound(2).toString()) >= 0
							&& object.compareTo(zone
									.getUpperBound(2).toString()) < 0) {
					    if (logger.isDebugEnabled()) {
					        logger.debug("Statement {} will be transfered", stmt);
					    }
						statementsToTransfert.add(stmt);
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("{} data have been retrieved for transfert",
							result.getData().size());
				}
			}
		} catch (SemanticSpaceException e) {
			e.printStackTrace();
		}

		return new ClosableIterableWrapper(
				SemanticHelper.generateClosableIterable(statementsToTransfert));
	}

	protected void removeDataIn(Zone zone) {
		ClosableIterableWrapper result = new ClosableIterableWrapper(
				SemanticHelper
						.generateClosableIterable(new HashSet<Statement>()));

		try {
			if (this.localKernelReference.hasStatements(this.spaceURI)) {
				result = new ClosableIterableWrapper(
						this.localKernelReference.sparqlConstruct(
								this.spaceURI,
								"CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }"));

				ClosableIterator<Statement> it = result.toRDF2Go().iterator();
				Statement stmt;
				String subject;
				String predicate;
				String object;

				while (it.hasNext()) {
					stmt = it.next();

					subject = SemanticHelper.parseTripleForLoadBalancing(stmt
							.getSubject().toString());
					predicate = SemanticHelper.parseTripleForLoadBalancing(stmt
							.getPredicate().toString());
					object = SemanticHelper.parseTripleForLoadBalancing(stmt
							.getObject().toString());

					// Yeah, manual filtering is really ugly!
					if (subject.compareTo(zone
							.getLowerBound(0).toString()) >= 0
							&& subject.compareTo(zone
									.getUpperBound(0).toString()) < 0
							&& predicate.compareTo(zone
									.getLowerBound(1).toString()) >= 0
							&& predicate.compareTo(zone
									.getUpperBound(1).toString()) < 0
							&& object.compareTo(zone
									.getLowerBound(2).toString()) >= 0
							&& object.compareTo(zone
									.getUpperBound(2).toString()) < 0) {
					    if (logger.isDebugEnabled()) {
					        logger.debug("Statement {} will be transfered", stmt);
					    }
						this.localKernelReference.removeStatement(this.spaceURI, stmt);
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("{} data have been retrieved for transfert",
							result.getData().size());
				}
			}
		} catch (SemanticSpaceException e) {
			e.printStackTrace();
		}
	}
	
	protected void affectDataReceived(Object dataReceived) {
		try {
			this.localKernelReference.addAll(
					this.spaceURI,
					((ClosableIterableWrapper) dataReceived).toRDF2Go().iterator());
		} catch (SemanticSpaceException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	protected void mergeDataReceived(MergeOperation msg) {
		try {
			this.localKernelReference.addAll(this.spaceURI,
					((ClosableIterable<Statement>) msg.getDataToReallocate())
							.iterator());
		} catch (SemanticSpaceException e) {
			e.printStackTrace();
		}
	}

	protected ClosableIterable<Statement> retrieveAllData() {
		try {
			return this.localKernelReference.sparqlConstruct(this.spaceURI,
					"CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <" + this.spaceURI
							+ "> { ?s ?s ?o } . }");
		} catch (SemanticSpaceException e) {
			e.printStackTrace();
		}
		return null;
	}

	public synchronized SemanticSpaceOverlayKernel getLocalSemanticSpaceOverlayKernel() {
		if (this.localKernelReference == null) {
			try {
				this.setLocalKernelReference();
			} catch (NotActiveObjectException e) {
				e.printStackTrace();
			} catch (NotOnSameRuntimeException e) {
				e.printStackTrace();
			}
		}
		
		return this.localKernelReference;
	}

	public SemanticSpaceOverlayKernel getRemoteSemanticSpaceOverlayKernel() {
		return this.remoteKernelReference;
	}

	public URI getSpaceURI() {
		return this.spaceURI;
	}

	public void setSpaceURI(URI spaceURI) {
		this.spaceURI = spaceURI;
	}

	/*
	 * Implementation of SemanticSpaceOperations interface
	 */

	public void addAll(URI space, Iterator<? extends Statement> other)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void addStatement(URI space, Statement statement)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void addStatement(URI space, Resource subject, URI predicate,
			String literal) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void addStatement(URI space, Resource subject, URI predicate,
			String literal, String languageTag) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void addStatement(URI space, Resource subject, URI predicate,
			String literal, URI datatypeURI) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void addStatement(URI space, String subjectURIString, URI predicate,
			String literal) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void addStatement(URI space, String subjectURIString, URI predicate,
			String literal, String languageTag) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void addStatement(URI space, String subjectURIString, URI predicate,
			String literal, URI datatypeURI) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public boolean contains(URI space, Statement s)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean contains(URI space, ResourceOrVariable subject,
			UriOrVariable predicate, NodeOrVariable object)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean contains(URI space, ResourceOrVariable subject,
			UriOrVariable predicate, String plainLiteral)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub
		return false;
	}

	public ClosableIterator<Statement> findStatements(URI space,
			TriplePattern triplepattern) throws SemanticSpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	public void joinSpace(URI space) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void leaveSpace(URI space) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public Set<URI> listJoinedSpaces() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<URI> listSpaces() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeAll(URI space) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void removeAll(URI space, Iterator<? extends Statement> statements)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void removeStatement(URI space, Statement statement)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void removeStatement(URI space, Resource subject, URI predicate,
			String literal) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void removeStatement(URI space, Resource subject, URI predicate,
			String literal, String languageTag) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void removeStatement(URI space, Resource subject, URI predicate,
			String literal, URI datatypeURI) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void removeStatement(URI space, String subjectURIString,
			URI predicate, String literal) throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void removeStatement(URI space, String subjectURIString,
			URI predicate, String literal, String languageTag)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void removeStatement(URI space, String subjectURIString,
			URI predicate, String literal, URI datatypeURI)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void removeStatements(URI space, ResourceOrVariable subject,
			UriOrVariable predicate, NodeOrVariable object)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public void removeStatements(URI space, TriplePattern triplePattern)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub

	}

	public boolean sparqlAsk(URI space, String query)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub
		return false;
	}

	public ClosableIterable<Statement> sparqlConstruct(URI space, String query)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	public ClosableIterable<Statement> sparqlDescribe(URI space, String query)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub
		return null;
	}

	public QueryResultTable sparqlSelect(URI space, String queryString)
			throws SemanticSpaceException {
		// TODO Auto-generated method stub
		return null;
	}

}
