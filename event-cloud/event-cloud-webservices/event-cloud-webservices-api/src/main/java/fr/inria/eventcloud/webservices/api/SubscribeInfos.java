package fr.inria.eventcloud.webservices.api;

import java.io.Serializable;

/**
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class SubscribeInfos implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sparqlQuery;

    private String subscriberWsUrl;

    public SubscribeInfos(String sparqlQuery, String subscriberWsUrl) {
        super();
        this.sparqlQuery = sparqlQuery;
        this.subscriberWsUrl = subscriberWsUrl;
    }

    public String getSparqlQuery() {
        return this.sparqlQuery;
    }

    public void setSparqlQuery(String sparqlQuery) {
        this.sparqlQuery = sparqlQuery;
    }

    public String getSubscriberWsUrl() {
        return this.subscriberWsUrl;
    }

    public void setSubscriberWsUrl(String subscriberWsUrl) {
        this.subscriberWsUrl = subscriberWsUrl;
    }
}
