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
package fr.inria.eventcloud.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.hp.hpl.jena.query.BIOInput;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingInputStream;
import com.hp.hpl.jena.sparql.engine.binding.BindingOutputStream;

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
        if (gzipped) {
            try {
                out = new GZIPOutputStream(out);

                if (binding == null) {
                    out.write(0);
                } else {
                    out.write(7);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BindingOutputStream bos = new BindingOutputStream(out);
        if (binding != null) {
            bos.write(binding);
            bos.flush();
        }

        if (gzipped) {
            try {
                ((GZIPOutputStream) out).finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

        ResultSetFormatter.outputAsBIO(out, resultSet);

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

        try {
            if (gzipped) {
                if (((GZIPInputStream) in).read() == 0) {
                    return null;
                }
            } else {
                if (in.available() == 0) {
                    return null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        BindingInputStream bis = new BindingInputStream(in);
        return bis.next();
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

        ResultSet resultSet = BIOInput.fromBIO(in);

        return resultSet;
    }

}
