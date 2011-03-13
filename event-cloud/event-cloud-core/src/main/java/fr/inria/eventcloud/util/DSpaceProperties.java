package fr.inria.eventcloud.util;

import java.io.File;

import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyInteger;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyString;
import org.objectweb.proactive.extensions.p2p.structured.util.SystemUtil;
import org.ontoware.rdf2go.model.node.URI;

/**
 * @author lpellegr
 */
public class DSpaceProperties {

	public static final PropertyString DSPACE_CONFIGURATION_FILE = 
	                                                new PropertyString("dspace.configuration");

	public static final PropertyString DSPACE_REPOSITORY_RESTORE_ID = 
	                                                new PropertyString("dspace.repository.restore.id");
	
	public static final PropertyInteger DSPACE_CONSISTENCY_TIMEOUT =
													new PropertyInteger("dspace.consistency.timeout", 500);
	
	private static final File defaultPathForConfigurationFiles;
	
	static {
		String prefix = System.getProperty("user.home") + "/";
		String suffix = "dspace";
		
		if (SystemUtil.isWindows()) {
			defaultPathForConfigurationFiles = new File(prefix + suffix);
		} else {
			defaultPathForConfigurationFiles = new File(prefix + "." + suffix);
		}
	}
	
	/**
	 * Returns the default path used for dspace configuration files.
	 * 
	 * @return the default path used for dspace configuration files.
	 */
	public static File getDefaultPathForConfigurationFiles() {
		return defaultPathForConfigurationFiles;		
	}
	
	public static final URI DEFAULT_CONTEXT = RDF2GoBuilder.createURI("http://www.inria.fr");
	
}
