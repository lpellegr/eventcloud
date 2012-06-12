/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class which is common to all components in order to load some
 * configurations from data spaces in case that the components have been
 * deployed with the ProActive Scheduler.
 * 
 * @author bsauvan
 */
public abstract class AbstractComponent implements ComponentInitActive {

    private static final String INPUT_SPACE_PREFIX = "INPUT_SPACE";

    private static Logger log =
            LoggerFactory.getLogger(AbstractComponent.class);

    protected String log4jConfigurationProperty =
            "log4j.configuration.dataspace";

    protected String p2pConfigurationProperty =
            "proactive.p2p.structured.configuration";

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        this.loadLog4jConfigurationFromIS();

        this.loadP2PConfigurationFromIS();
    }

    private void loadLog4jConfigurationFromIS() {
        try {
            String log4jConfigurationPropertyValue =
                    System.getProperty(this.log4jConfigurationProperty);

            if (log4jConfigurationPropertyValue != null
                    && log4jConfigurationPropertyValue.startsWith(INPUT_SPACE_PREFIX)) {
                DataSpacesFileObject log4jConfigurationDSFile =
                        PADataSpaces.resolveDefaultInput(log4jConfigurationPropertyValue.substring(
                                INPUT_SPACE_PREFIX.length() + 1,
                                log4jConfigurationPropertyValue.length()));

                DOMConfigurator configurator = new DOMConfigurator();
                InputStream is =
                        log4jConfigurationDSFile.getContent().getInputStream();
                configurator.doConfigure(is, LogManager.getLoggerRepository());
                is.close();

                log.debug("Log4J configuration successfully loaded from input space");
            }
        } catch (SpaceNotFoundException snfe) {
            snfe.printStackTrace();
        } catch (FileSystemException fse) {
            fse.printStackTrace();
        } catch (NotConfiguredException nce) {
            nce.printStackTrace();
        } catch (ConfigurationException ce) {
            ce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void loadP2PConfigurationFromIS() {
        try {
            String p2pConfigurationPropertyValue =
                    System.getProperty(this.p2pConfigurationProperty);

            if (p2pConfigurationPropertyValue != null
                    && p2pConfigurationPropertyValue.startsWith(INPUT_SPACE_PREFIX)) {
                DataSpacesFileObject p2pConfigurationDSFile =
                        PADataSpaces.resolveDefaultInput(p2pConfigurationPropertyValue.substring(
                                INPUT_SPACE_PREFIX.length() + 1,
                                p2pConfigurationPropertyValue.length()));
                InputStream is =
                        p2pConfigurationDSFile.getContent().getInputStream();
                File p2pConfigurationFile = null;
                FileOutputStream fos = null;

                try {
                    p2pConfigurationFile =
                            File.createTempFile(
                                    "p2p-configuration-", ".properties");
                    fos = new FileOutputStream(p2pConfigurationFile);

                    IOUtils.copy(is, fos);

                    System.setProperty(
                            this.p2pConfigurationProperty,
                            p2pConfigurationFile.getCanonicalPath());

                    log.debug("P2P configuration successfully loaded from input space");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        } catch (SpaceNotFoundException snfe) {
            snfe.printStackTrace();
        } catch (FileSystemException fse) {
            fse.printStackTrace();
        } catch (NotConfiguredException nce) {
            nce.printStackTrace();
        } catch (ConfigurationException ce) {
            ce.printStackTrace();
        }
    }

}
