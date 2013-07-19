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
package org.objectweb.proactive.extensions.p2p.structured;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.cxf.helpers.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apfloat.ApfloatContext;
import org.apfloat.spi.BuilderFactory;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.core.component.body.ComponentRunActive;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Abstract class which is common to all components in order to load some
 * configurations from data spaces in case that the components have been
 * deployed with the ProActive Scheduler.
 * 
 * @author bsauvan
 */
public abstract class AbstractComponent implements ComponentInitActive,
        ComponentRunActive {

    public static final String INPUT_SPACE_PREFIX = "INPUT_SPACE";

    private static Logger log =
            LoggerFactory.getLogger(AbstractComponent.class);

    protected String logbackConfigurationProperty =
            "logback.configuration.dataspace";

    protected String log4jConfigurationProperty =
            "log4j.configuration.dataspace";

    protected String configurationProperty =
            "proactive.p2p.structured.configuration";

    protected Class<?> propertiesClass = P2PStructuredProperties.class;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        // FIXME: to avoid some deadlock with components the method
        // setImmediateServices has to be handled in immediate services. This
        // configuration should be done in the ProActive source code.
        body.setImmediateService("setImmediateServices", false);
        body.setImmediateService("setAttributes", false);
        // body.setImmediateService("unExposeComponentAsWebService", false);

        this.loadLogbackConfigurationFromIS();
        this.loadLog4jConfigurationFromIS();
        this.loadConfigurationFromIS();

        try {
            this.propertiesClass.getDeclaredMethod("loadConfiguration").invoke(
                    null);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        } catch (InvocationTargetException ite) {
            throw new IllegalStateException(ite);
        } catch (NoSuchMethodException nsme) {
            throw new IllegalStateException(nsme);
        }

        try {
            // sets the default builder factory for the Apfloat library
            ApfloatContext.getContext()
                    .setBuilderFactory(
                            (BuilderFactory) P2PStructuredProperties.APFLOAT_DEFAULT_BUILDER_FACTORY.getValue()
                                    .newInstance());
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        } catch (InstantiationException ie) {
            throw new IllegalStateException(ie);
        }
    }

    private void loadLogbackConfigurationFromIS() {
        try {
            String logbackConfigurationPropertyValue =
                    System.getProperty(this.logbackConfigurationProperty);

            if (logbackConfigurationPropertyValue != null
                    && logbackConfigurationPropertyValue.startsWith(INPUT_SPACE_PREFIX)) {
                DataSpacesFileObject logbackConfigurationDSFile =
                        PADataSpaces.resolveDefaultInput(logbackConfigurationPropertyValue.substring(
                                INPUT_SPACE_PREFIX.length() + 1,
                                logbackConfigurationPropertyValue.length()));
                InputStream logbackConfigurationIs =
                        logbackConfigurationDSFile.getContent()
                                .getInputStream();

                try {
                    LoggerContext context =
                            (LoggerContext) LoggerFactory.getILoggerFactory();
                    JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(context);
                    context.reset();
                    configurator.doConfigure(logbackConfigurationIs);
                } catch (ClassCastException cce) {
                    log.warn(
                            "Unable to load Logback configuration from input space because there is conflict with an another implementation of SLF4J",
                            cce);
                } catch (JoranException je) {
                    throw new IllegalStateException(je);
                } finally {
                    logbackConfigurationIs.close();
                }

                log.debug("Logback configuration successfully loaded from input space");
            }
        } catch (SpaceNotFoundException snfe) {
            throw new IllegalStateException(snfe);
        } catch (FileSystemException fse) {
            throw new IllegalStateException(fse);
        } catch (NotConfiguredException nce) {
            throw new IllegalStateException(nce);
        } catch (ConfigurationException ce) {
            throw new IllegalStateException(ce);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
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
                InputStream log4jConfigurationIs =
                        log4jConfigurationDSFile.getContent().getInputStream();

                if (log4jConfigurationPropertyValue.endsWith(".xml")) {
                    // XML configuration file
                    DOMConfigurator configurator = new DOMConfigurator();
                    configurator.doConfigure(
                            log4jConfigurationIs,
                            LogManager.getLoggerRepository());
                } else {
                    // Properties configuration file
                    PropertyConfigurator.configure(log4jConfigurationIs);
                }

                log4jConfigurationIs.close();

                log.debug("Log4J configuration successfully loaded from input space");
            }
        } catch (SpaceNotFoundException snfe) {
            throw new IllegalStateException(snfe);
        } catch (FileSystemException fse) {
            throw new IllegalStateException(fse);
        } catch (NotConfiguredException nce) {
            throw new IllegalStateException(nce);
        } catch (ConfigurationException ce) {
            throw new IllegalStateException(ce);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void loadConfigurationFromIS() {
        try {
            String p2pConfigurationPropertyValue =
                    System.getProperty(this.configurationProperty);

            if (p2pConfigurationPropertyValue != null
                    && p2pConfigurationPropertyValue.startsWith(INPUT_SPACE_PREFIX)) {
                DataSpacesFileObject p2pConfigurationDSFile =
                        PADataSpaces.resolveDefaultInput(p2pConfigurationPropertyValue.substring(
                                INPUT_SPACE_PREFIX.length() + 1,
                                p2pConfigurationPropertyValue.length()));
                InputStream is =
                        p2pConfigurationDSFile.getContent().getInputStream();
                File p2pConfigurationFile =
                        File.createTempFile("p2p-configuration-", ".properties");
                FileOutputStream fos =
                        new FileOutputStream(p2pConfigurationFile);

                IOUtils.copy(is, fos);

                is.close();
                fos.close();

                System.setProperty(
                        this.configurationProperty,
                        p2pConfigurationFile.getCanonicalPath());

                log.debug("P2P configuration successfully loaded from input space");
            }
        } catch (SpaceNotFoundException snfe) {
            throw new IllegalStateException(snfe);
        } catch (FileSystemException fse) {
            throw new IllegalStateException(fse);
        } catch (NotConfiguredException nce) {
            throw new IllegalStateException(nce);
        } catch (ConfigurationException ce) {
            throw new IllegalStateException(ce);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

}
