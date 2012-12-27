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
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;

/**
 * Some utility methods to serialize Jena {@link Node}s.
 * 
 * @author lpellegr
 */
public class NodeSerializer {

    public static Node readLiteralOrUri(ObjectInput in) throws IOException {
        byte bitmap = in.readByte();

        boolean isLiteral = (1 & (bitmap >> 2)) == 1;
        boolean hasLanguageTag = (1 & (bitmap >> 1)) == 1;
        boolean hasDatatype = (1 & bitmap) == 1;

        if (isLiteral) {
            String literalValue = in.readUTF();
            String languageTag = null;
            String datatypeURI = null;

            if (hasLanguageTag) {
                languageTag = readString(in);
            }

            if (hasDatatype) {
                datatypeURI = readString(in);
            }

            return Node.createLiteral(literalValue, languageTag, hasDatatype
                    ? TypeMapper.getInstance().getSafeTypeByName(datatypeURI)
                    : null);
        } else {
            return readUri(in);
        }
    }

    public static Node readUri(ObjectInput in) throws IOException {
        return Node.createURI(readString(in));
    }

    public static String readString(ObjectInput in) throws IOException {
        int stringLength = in.readInt();
        byte[] data = new byte[stringLength];

        in.read(data);

        return new String(data);
    }

    public static void writeLiteralOrUri(ObjectOutput out, Node node)
            throws IOException {
        boolean isLiteral = node.isLiteral();
        boolean hasLanguageTag = false;
        boolean hasDatatype = false;

        if (isLiteral) {
            hasLanguageTag = !node.getLiteralLanguage().isEmpty();
            hasDatatype = node.getLiteralDatatypeURI() != null;
        }

        byte bitmap =
                createLiteralBitmap(isLiteral, hasLanguageTag, hasDatatype);

        // writes a bitmap to indicate whether the object value is a literal
        // and if it embeds a language tag and/or a datatype
        out.writeByte(bitmap);

        if (isLiteral) {
            // a literal may contain unicode characters
            out.writeUTF(node.getLiteralLexicalForm());

            if (hasLanguageTag) {
                writeString(out, node.getLiteralLanguage());
            }

            if (hasDatatype) {
                writeString(out, node.getLiteralDatatypeURI());
            }
        } else {
            writeString(out, node.toString());
        }
    }

    private static byte createLiteralBitmap(boolean hasLiteral,
                                            boolean hasLanguageTag,
                                            boolean hasDatatype) {
        byte result = 0;

        if (hasLiteral) {
            result |= 0x4;
        }

        if (hasLanguageTag) {
            result |= 0x2;
        }

        if (hasDatatype) {
            result |= 0x1;
        }

        return result;
    }

    public static void writeUri(ObjectOutput out, Node node) throws IOException {
        writeString(out, node.getURI());
    }

    public static void writeString(ObjectOutput out, String s)
            throws IOException {
        out.writeInt(s.length());
        out.writeBytes(s);
    }

}
