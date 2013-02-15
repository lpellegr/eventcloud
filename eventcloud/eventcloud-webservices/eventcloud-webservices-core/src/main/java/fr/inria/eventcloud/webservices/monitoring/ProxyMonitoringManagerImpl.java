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
package fr.inria.eventcloud.webservices.monitoring;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.w3c.dom.Document;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import easybox.petalslink.com.esrawreport._1.EJaxbReportListType;
import easybox.petalslink.com.esrawreport._1.EJaxbReportTimeStampType;
import easybox.petalslink.com.esrawreport._1.EJaxbReportType;
import easybox.petalslink.com.esrawreport._1.ObjectFactory;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.monitoring.ProxyMonitoringActions;
import fr.inria.eventcloud.translators.wsn.WsnHelper;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * Concrete implementation for {@link ProxyMonitoringManager}.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class ProxyMonitoringManagerImpl extends AbstractComponent implements
        ProxyMonitoringActions, ProxyMonitoringManager, ComponentInitActive {

    /**
     * Name of the proxy monitoring manager non functional component.
     */
    public static final String COMPONENT_NAME = "ProxyMonitoringManager";

    /**
     * ADL name of the proxy monitoring manager non functional component.
     */
    public static final String PROXY_MONITORING_MANAGER_ADL =
            "fr.inria.eventcloud.webservices.monitoring.ProxyMonitoringManager";

    /**
     * Functional interface name of the proxy monitoring manager non functional
     * component.
     */
    public static final String MONITORING_SERVICES_ITF = "monitoring-services";

    private static final QName RAW_REPORT_QNAME = new QName(
            "http://www.petalslink.org/rawreport/1.0", "RawReportTopic", "rrt");

    private static final QName INTERFACE_QNAME = new QName(
            "http://www.petalslink.com/wsn/service/WsnProducer",
            "NotificationProducer", "np");

    public final LoadingCache<String, NotificationConsumer> notificationConsumerClients =
            CacheBuilder.newBuilder().softValues().maximumSize(10).build(
                    new CacheLoader<String, NotificationConsumer>() {
                        @Override
                        public NotificationConsumer load(String consumerEndpoint)
                                throws Exception {
                            return WsClientFactory.createWsClient(
                                    NotificationConsumer.class,
                                    consumerEndpoint);
                        }
                    });

    private Map<SubscriptionId, String> consumerEndpoints;

    private ExecutorService cachedThreadPool;

    /**
     * Empty constructor required by ProActive.
     */
    public ProxyMonitoringManagerImpl() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        this.configurationProperty = "eventcloud.configuration";
        this.propertiesClass = EventCloudProperties.class;
        super.initComponentActivity(body);

        this.consumerEndpoints = new HashMap<SubscriptionId, String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runComponentActivity(Body body) {
        new Service(body).fifoServing();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enableInputOutputMonitoring(SubscriptionId subscriptionId,
                                               String consumerEndpoint) {
        if (!this.consumerEndpoints.containsValue(consumerEndpoint)) {
            this.consumerEndpoints.put(subscriptionId, consumerEndpoint);
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean disableInputOutputMonitoring(SubscriptionId subscriptionId) {
        if (this.consumerEndpoints.containsKey(subscriptionId)) {
            this.consumerEndpoints.remove(subscriptionId);
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInputOutputMonitoringEnabled() {
        return this.consumerEndpoints.size() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendInputOutputMonitoringReport(final String source,
                                                final String destination,
                                                final long eventPublicationTimestamp) {
        for (final String consumerEndpoint : this.consumerEndpoints.values()) {
            this.getCachedThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        ProxyMonitoringManagerImpl.this.notificationConsumerClients.get(
                                consumerEndpoint)
                                .notify(
                                        createRawReport(
                                                consumerEndpoint, source,
                                                destination,
                                                eventPublicationTimestamp));
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private synchronized ExecutorService getCachedThreadPool() {
        if (this.cachedThreadPool == null) {
            this.cachedThreadPool = Executors.newCachedThreadPool();
        }

        return this.cachedThreadPool;
    }

    private static Notify createRawReport(String consumerEndpoint,
                                          String source, String destination,
                                          long eventPublicationTimestamp) {
        easybox.petalslink.com.esrawreport._1.ObjectFactory factory =
                new ObjectFactory();

        EJaxbReportType reportType = factory.createEJaxbReportType();
        reportType.setExchangeId(UUID.randomUUID().toString());
        reportType.setTimeStamp(EJaxbReportTimeStampType.T_1);

        try {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(eventPublicationTimestamp);
            reportType.setDateInGMT(DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(gc));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }

        reportType.setConsumerEndpointAddress(destination);
        reportType.setOperationName("http://com.petalslink.esstar/service/management/user/1.0/Notify");
        reportType.setInterfaceQName(INTERFACE_QNAME);
        reportType.setProviderEndpointAddress(source);

        // TODO the following field should contain the size in bytes of the
        // event that is notified to a subscriber. However, to compute this
        // value is costly and should be done only if this is really required
        reportType.setContentLength(-1);
        reportType.setDoesThisResponseIsAnException(false);
        reportType.setEndpointName("EventCloud");

        EJaxbReportListType reportTypeList =
                factory.createEJaxbReportListType();
        reportTypeList.getReport().add(reportType);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            Document doc = dbf.newDocumentBuilder().newDocument();

            JAXBContext.newInstance(
                    easybox.petalslink.com.esrawreport._1.ObjectFactory.class)
                    .createMarshaller()
                    .marshal(factory.createReportList(reportTypeList), doc);

            return WsnHelper.createNotifyMessage(
                    consumerEndpoint, RAW_REPORT_QNAME, source,
                    doc.getDocumentElement());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

}
