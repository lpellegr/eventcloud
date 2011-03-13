package org.objectweb.proactive.extensions.p2p.structured.initializers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.RouterStore;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * @author lpellegr
 * 
 */
public class RouterStoreTest {

	@Test
	public void test() {	
		RouterA routerA = new RouterA();
		RouterB routerB = new RouterB();
		
		ConstraintsValidatorA validatorA = new ConstraintsValidatorA("A");
		ConstraintsValidatorB validatorB = new ConstraintsValidatorB("B");
		
		RequestA requestA = new RequestA(validatorA);
		RequestB requestB = new RequestB(validatorB);
		
		RouterStore store = RouterStore.getInstance();
		store.store(requestA.getClass(), validatorA.getClass(), routerA);

		assertTrue(store.contains(requestA.getClass(), validatorA.getClass()));
		assertEquals(routerA.hashCode(), store.get(requestA.getClass(), validatorA.getClass()).hashCode());
		assertFalse(store.contains(requestA.getClass(), validatorB.getClass()));
		
		store.store(requestB.getClass(), validatorB.getClass(), routerB);
		assertTrue(store.contains(requestA.getClass(), validatorA.getClass()));
		assertEquals(routerB.hashCode(), store.get(requestB.getClass(), validatorB.getClass()).hashCode());
		assertFalse(store.contains(requestA.getClass(), validatorB.getClass()));
	}

	/*
	 * Some classes examples to run the tests.
	 */
	
	class RouterA extends Router<SuperRequest, String> {

		@Override
		public void makeDecision(StructuredOverlay overlay,
				SuperRequest msg) {
		}

		@Override
		protected void doHandle(StructuredOverlay overlay,
				SuperRequest msg) {
		}

		@Override
		protected void doRoute(StructuredOverlay overlay,
				SuperRequest msg) {
		}
		
	}
	
	class RouterB extends Router<SuperRequest, String> {

		@Override
		public void makeDecision(StructuredOverlay overlay,
				SuperRequest msg) {
		}

		@Override
		protected void doHandle(StructuredOverlay overlay,
				SuperRequest msg) {
		}

		@Override
		protected void doRoute(StructuredOverlay overlay,
				SuperRequest msg) {
		}
		
	}
	
	class ConstraintsValidatorA extends ConstraintsValidator<String> {

		private static final long serialVersionUID = 1L;

		public ConstraintsValidatorA(String key) {
			super(key);
		}

		@Override
		public boolean validatesKeyConstraints(StructuredOverlay overlay) {
			return false;
		}
		
	}
	
	class ConstraintsValidatorB extends ConstraintsValidator<String> {

		private static final long serialVersionUID = 1L;

		public ConstraintsValidatorB(String key) {
			super(key);
		}

		@Override
		public boolean validatesKeyConstraints(StructuredOverlay overlay) {
			return false;
		}
		
	}
	
	abstract class SuperRequest extends Request<String> {

		private static final long serialVersionUID = 1L;

		public SuperRequest(ConstraintsValidator<String> validator) {
			super(validator);
		}
		@Override
		public Router<SuperRequest, String> getRouter() {
			return null;
		}

		@Override
		public Response<String> createResponse() {
			return null;
		}
		
	}
	
	class RequestA extends SuperRequest {

		private static final long serialVersionUID = 1L;

		public RequestA(ConstraintsValidator<String> validator) {
			super(validator);
		}
		
	}
	
	class RequestB extends SuperRequest {

		private static final long serialVersionUID = 1L;

		public RequestB(ConstraintsValidator<String> validator) {
			super(validator);
		}
		
	}
	
}
