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
package fr.inria.eventcloud.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.openjena.riot.out.NodeFmtLib;
import org.openjena.riot.tokens.TokenizerFactory;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.resultset.TSVOutput;

import fr.inria.eventcloud.protobuf.SparqlResultsProtos;
import fr.inria.eventcloud.protobuf.SparqlResultsProtos.Binding.Builder;
import fr.inria.eventcloud.protobuf.SparqlResultsProtos.Binding.SequenceBinding;

/**
 * A SparqlResultSerializer provides several methods to serialize and to
 * deserialize the results that are returned by Jena when you execute a SPARQL
 * query (e.g. a {@link Model} or a {@link ResultSet}).
 * 
 * @author lpellegr
 */
public final class SparqlResultSerializer {

    private SparqlResultSerializer() {

    }

    /**
     * Serializes the specified {@code binding} into the given output stream. By
     * default the output is not compressed.
     * 
     * @param out
     *            the output stream to write in.
     * @param binding
     *            the {@link Binding} to serialize.
     */
    public static void serialize(OutputStream out, Binding binding) {
        serialize(out, binding, false);
    }

    /**
     * Serializes the specified {@code binding} into the given output stream.
     * The compression is enabled or not according to the {@code gzipped}
     * parameter.
     * 
     * @param out
     *            the output stream to write in.
     * @param binding
     *            the {@link Binding} to serialize.
     * @param gzipped
     *            if set to {@code true} the output is gzipped.
     */
    public static void serialize(OutputStream out, Binding binding,
                                 boolean gzipped) {

        Builder bindingBuilder = SparqlResultsProtos.Binding.newBuilder();

        if (gzipped) {
            try {
                out = new GZIPOutputStream(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (binding.getParent() != null) {
            injectSequenceBindings(bindingBuilder, binding.getParent(), true);
        }
        injectSequenceBindings(bindingBuilder, binding, false);

        try {
            bindingBuilder.build().writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (gzipped) {
            try {
                ((GZIPOutputStream) out).finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void injectSequenceBindings(Builder builder,
                                               Binding binding, boolean parent) {
        List<Var> vars = extractConcreteVars(binding);

        for (Var var : vars) {
            SequenceBinding.Builder sequenceBinding =
                    SparqlResultsProtos.Binding.SequenceBinding.newBuilder();
            sequenceBinding.setVarName(var.getName());
            sequenceBinding.setValue(NodeFmtLib.serialize(binding.get(var)));
            if (parent) {
                builder.addParentBinding(sequenceBinding);
            } else {
                builder.addBinding(sequenceBinding);
            }
        }
    }

    private static List<Var> extractConcreteVars(Binding binding) {
        Iterator<Var> varsIterator = binding.vars();
        Set<Var> parentVars = new HashSet<Var>();
        List<Var> result = new ArrayList<Var>();

        if (binding.getParent() != null) {
            Iterator<Var> it = binding.getParent().vars();
            while (it.hasNext()) {
                parentVars.add(it.next());
            }
        }

        while (varsIterator.hasNext()) {
            Var var = varsIterator.next();
            if (!parentVars.contains(var)) {
                result.add(var);
            }
        }

        return result;
    }

    /**
     * Serializes the specified {@code resultSet} into the given output stream
     * by using the TSV format. By default the output is not compressed.
     * 
     * @param out
     *            the output stream to write in.
     * @param resultSet
     *            the {@link ResultSet} to serialize.
     */
    public static void serialize(OutputStream out, ResultSet resultSet) {
        serialize(out, resultSet, false);
    }

    /**
     * Serializes the specified {@code resultSet} into the given output stream
     * by using the TSV format. The compression is enabled or not according to
     * the {@code gzipped} parameter.
     * 
     * @param out
     *            the output stream to write in.
     * @param resultSet
     *            the {@link ResultSet} to serialize.
     * @param gzipped
     *            if set to {@code true} the output is gzipped.
     */
    public static void serialize(OutputStream out, ResultSet resultSet,
                                 boolean gzipped) {
        if (gzipped) {
            try {
                out = new GZIPOutputStream(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new TSVOutput().format(out, resultSet);

        if (gzipped) {
            try {
                ((GZIPOutputStream) out).finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Serializes the specified {@code model} by using the TURTLE syntax. By
     * default the output is not compressed.
     * 
     * @param out
     *            the output stream to write in.
     * @param model
     *            the {@link Model} to serialize.
     */
    public static void serialize(OutputStream out, Model model) {
        serialize(out, model, false);
    }

    /**
     * Serializes the specified {@code model} into the given output stream by
     * using the TURTLE syntax. By default the output is not compressed.
     * 
     * @param out
     *            the output stream to write in.
     * @param model
     *            the {@link Model} to serialize.
     * @param gzipped
     *            if set to {@code true} the output is gzipped.
     */
    public static void serialize(OutputStream out, Model model, boolean gzipped) {
        if (gzipped) {
            try {
                out = new GZIPOutputStream(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        model.write(out, "TURTLE", null);

        if (gzipped) {
            try {
                ((GZIPOutputStream) out).finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deserializes a {@link Binding} by reading bytes from the specified input
     * stream. It assumes the output is not compressed.
     * 
     * @param in
     *            the input stream to read from.
     * 
     * @return the {@link Binding} read from the input stream.
     */
    public static Binding deserializeBinding(InputStream in) {
        return deserializeBinding(in, false);
    }

    /**
     * Deserializes a {@link Binding} by reading bytes from the specified input
     * stream. It will try to ungzip the byte buffer according to the
     * {@code gzipped} parameter.
     * 
     * @param in
     *            the input stream to read from.
     * 
     * @return the {@link Binding} read from the input stream.
     */
    public static Binding deserializeBinding(InputStream in, boolean gzipped) {
        if (gzipped) {
            try {
                in = new GZIPInputStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SparqlResultsProtos.Binding bindingProto = null;
        try {
            bindingProto = SparqlResultsProtos.Binding.parseFrom(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Binding parentBinding = null;
        if (bindingProto.getParentBindingCount() > 0) {
            parentBinding =
                    createBinding(bindingProto.getParentBindingList(), null);
        }

        return createBinding(bindingProto.getBindingList(), parentBinding);
    }

    private static final Binding createBinding(List<SequenceBinding> sbl,
                                               Binding parent) {
        Binding binding = BindingFactory.create(parent);

        for (SequenceBinding sb : sbl) {
            binding.add(
                    Var.alloc(sb.getVarName()),
                    TokenizerFactory.makeTokenizerString(sb.getValue())
                            .next()
                            .asNode());
        }

        return binding;
    }

    /**
     * Deserializes a {@link Model} by reading bytes from the specified input
     * stream. It assumes the output is not compressed.
     * 
     * @param in
     *            the input stream to read from.
     * 
     * @return the {@link Model} read from the input stream.
     */
    public static Model deserializeModel(InputStream in) {
        return deserializeModel(in, false);
    }

    /**
     * Deserializes a {@link Model} by reading bytes from the specified input
     * stream. It will try to ungzip the byte buffer according to the
     * {@code gzipped} parameter.
     * 
     * @param in
     *            the input stream to read from.
     * 
     * @return the {@link Model} read from the input stream.
     */
    public static Model deserializeModel(InputStream in, boolean gzipped) {
        Model model = ModelFactory.createDefaultModel();

        if (gzipped) {
            try {
                in = new GZIPInputStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        model.read(in, null, "TURTLE");

        return model;
    }

    /**
     * Deserializes a {@link ResultSet} by reading bytes from the specified
     * input stream. It assumes the output is not compressed.
     * 
     * @param in
     *            the input stream to read from.
     * 
     * @return the {@link ResultSet} read from the input stream.
     */
    public static ResultSet deserializeResultSet(InputStream in) {
        return deserializeResultSet(in, false);
    }

    /**
     * Deserializes a {@link ResultSet} by reading bytes from the specified
     * input stream. It will try to ungzip the byte buffer according to the
     * {@code gzipped} parameter.
     * 
     * @param in
     *            the input stream to read from.
     * 
     * @return the {@link ResultSet} read from the input stream.
     */
    public static ResultSet deserializeResultSet(InputStream in, boolean gzipped) {
        if (gzipped) {
            try {
                in = new GZIPInputStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ResultSet resultSet = ResultSetFactory.fromTSV(in);

        return resultSet;
    }

}
