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
package fr.inria.eventcloud.datastore;

import com.hp.hpl.jena.sparql.core.Var;

/**
 * Caches static variable allocations.
 * 
 * @author lpellegr
 */
public final class Vars {

    public static final Var GRAPH = Var.alloc("g");

    // sId
    public static final Var SUBSCRIPTION_ID = Var.alloc("a");

    // sSrc
    public static final Var SUBSCRIPTION_SOURCE = Var.alloc("b");

    // ssId
    public static final Var SUBSUBSCRIPTION_ID = Var.alloc("c");

    // ssSrc
    public static final Var SUBSUBSCRIPTION_SOURCE = Var.alloc("d");

    // ssGraph
    public static final Var SUBSUBSCRIPTION_GRAPH = Var.alloc("e");

    // ssSubject
    public static final Var SUBSUBSCRIPTION_SUBJECT = Var.alloc("f");

    // ssPredicate
    public static final Var SUBSUBSCRIPTION_PREDICATE = Var.alloc("h");

    // ssObject
    public static final Var SUBSUBSCRIPTION_OBJECT = Var.alloc("i");

    private Vars() {

    }

}
