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
package fr.inria.eventcloud.datastore;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

import fr.inria.eventcloud.pubsub.PublishSubscribeConstants;

/**
 * A VariableDatatype is a Jena {@link RDFDatatype} that is used to store a
 * {@link Node_Variable} as a RDF literal. Once the variable is inserted into
 * the datastore with the tag {@link VariableDatatype}, when you retrieve a
 * result that contain this type of typed literal, you can execute
 * {@link Node#getLiteralValue()} to get directly the {@link Node_Variable}
 * object without parsing it.
 * 
 * @author lpellegr
 */
public class VariableDatatype extends BaseDatatype {

    private static final RDFDatatype instance = new VariableDatatype();

    private VariableDatatype() {
        super(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String unparse(Object value) {
        return ((Node_Variable) value).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        return Node.createVariable(lexicalForm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        return value1.getDatatype() == value2.getDatatype()
                && value1.getValue().equals(value2.getValue());
    }

    public static RDFDatatype getInstance() {
        return instance;
    }

}
