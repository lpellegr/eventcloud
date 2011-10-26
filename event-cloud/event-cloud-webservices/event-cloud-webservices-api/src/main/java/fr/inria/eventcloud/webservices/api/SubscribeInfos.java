/**
 * Copyright (c) 2011 INRIA.
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
