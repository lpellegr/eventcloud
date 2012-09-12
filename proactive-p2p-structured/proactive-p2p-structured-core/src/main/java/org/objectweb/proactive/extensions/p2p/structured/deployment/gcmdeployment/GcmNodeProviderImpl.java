package org.objectweb.proactive.extensions.p2p.structured.deployment.gcmdeployment;

import java.io.File;
import java.io.Serializable;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

public class GcmNodeProviderImpl implements NodeProvider, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private GCMApplication gcmad;
    private File pathToGCMADescriptor = null;

    public GcmNodeProviderImpl(String path) {
        this.pathToGCMADescriptor = new File(path);
        this.init();
    }

    @Override
    public void init() {
        try {
            this.gcmad =
                    PAGCMDeployment.loadApplicationDescriptor(this.pathToGCMADescriptor);
            this.gcmad.startDeployment();
            this.gcmad.waitReady();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void terminate() {
        this.gcmad.kill();
    }

    @Override
    public Node getANode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GCMVirtualNode getGcmVirtualNode(String vnName) {
        return this.gcmad.getVirtualNode(vnName);
    }

}
