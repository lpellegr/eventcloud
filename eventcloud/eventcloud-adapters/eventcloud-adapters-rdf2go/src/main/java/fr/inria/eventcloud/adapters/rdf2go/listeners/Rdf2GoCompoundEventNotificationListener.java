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
package fr.inria.eventcloud.adapters.rdf2go.listeners;

import org.ontoware.rdf2go.impl.jena.ModelFactoryImpl;
import org.ontoware.rdf2go.impl.jena.TypeConversion;
import org.ontoware.rdf2go.model.Model;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;

/**
 * This class adapts {@link CompoundEventNotificationListener} to receive
 * notification as a {@link Model}, which is an RDF2Go object.
 * 
 * @author lpellegr
 */
public abstract class Rdf2GoCompoundEventNotificationListener extends
        fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener
        implements Rdf2goNotificationListener<Model> {

    private static final long serialVersionUID = 151L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, CompoundEvent solution) {
        Model model =
                new ModelFactoryImpl().createModel(TypeConversion.toRDF2Go(
                        solution.getGraph()).asURI());

        model.open();

        for (Quadruple q : solution) {
            model.addStatement(TypeConversion.toRDF2Go(q.getSubject())
                    .asResource(), TypeConversion.toRDF2Go(q.getPredicate())
                    .asURI(), TypeConversion.toRDF2Go(q.getObject()));
        }

        this.handle(id, model);
    }

}
