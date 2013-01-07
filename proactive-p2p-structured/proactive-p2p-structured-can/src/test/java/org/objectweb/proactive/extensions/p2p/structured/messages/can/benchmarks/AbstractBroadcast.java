package org.objectweb.proactive.extensions.p2p.structured.messages.can.benchmarks;

import java.io.IOException;

import org.objectweb.proactive.extensions.p2p.structured.deployment.CanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.StringCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;

public abstract class AbstractBroadcast extends JunitByClassCanNetworkDeployer {

	protected int nbPeers;
	protected String logDirectory;
	
	protected Proxy proxy;
	
	public AbstractBroadcast(int nbPeers, String logDirectory) {
		super(
				new CanDeploymentDescriptor<StringElement>(
						new SerializableProvider<StringCanOverlay>() {
							private static final long serialVersionUID = 130L;
							@Override
							public StringCanOverlay get() {
								return new StringCanOverlay();
							}
						}).setInjectionConstraintsProvider(InjectionConstraintsProvider.newFractalInjectionConstraintsProvider()),
				1, nbPeers);
		this.nbPeers = nbPeers;
		this.logDirectory = logDirectory;
	}
	
	
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	public void initialize() {
		super.setUp();
		this.proxy = Proxies.newProxy(super.getRandomTracker());
	}
	
	public void terminate() {
		super.tearDown();
		try {
			this.proxy.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
