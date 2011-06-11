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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.datastore.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.rdf.model.Alt;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.RSIterator;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceF;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.ReificationStyle;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.utils.SparqlResultSerializer;

/**
 * ModelWrapper is used to make a Jena {@link Model} serializable.
 * 
 * @author lpellegr
 */
public class ModelWrapper extends SparqlResultWrapper<Model> implements Model {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a wrapper for the specified {@code model}.
     * 
     * @param model
     *            the model to wrap.
     */
    public ModelWrapper(Model model) {
        super(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalWriteObject(ObjectOutputStream out)
            throws IOException {
        SparqlResultSerializer.serialize(
                out, this, EventCloudProperties.COMPRESSION.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalReadObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        this.object =
                SparqlResultSerializer.deserializeModel(
                        in, EventCloudProperties.COMPRESSION.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement asStatement(Triple t) {
        return super.object.asStatement(t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri) {
        return super.object.setNsPrefix(prefix, uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Graph getGraph() {
        return super.object.getGraph();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryHandler queryHandler() {
        return super.object.queryHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RDFNode asRDFNode(Node n) {
        return super.object.asRDFNode(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource wrapAsResource(Node n) {
        return super.object.wrapAsResource(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrefixMapping removeNsPrefix(String prefix) {
        return super.object.removeNsPrefix(prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterCriticalSection(boolean readLockRequested) {
        super.object.enterCriticalSection(readLockRequested);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RDFWriter getWriter() {
        return super.object.getWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping other) {
        return super.object.setNsPrefixes(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leaveCriticalSection() {
        super.object.leaveCriticalSection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RDFWriter getWriter(String lang) {
        return super.object.getWriter(lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RDFReader getReader() {
        return super.object.getReader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long size() {
        return super.object.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> map) {
        return super.object.setNsPrefixes(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RDFReader getReader(String lang) {
        return super.object.getReader(lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String setWriterClassName(String lang, String className) {
        return super.object.setWriterClassName(lang, className);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping map) {
        return super.object.withDefaultMappings(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String setReaderClassName(String lang, String className) {
        return super.object.setReaderClassName(lang, className);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return super.object.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResource(String uri, ResourceF f) {
        return super.object.getResource(uri, f);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNsPrefixURI(String prefix) {
        return super.object.getNsPrefixURI(prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listSubjects() {
        return super.object.listSubjects();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNsURIPrefix(String uri) {
        return super.object.getNsURIPrefix(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property getProperty(String uri) {
        return super.object.getProperty(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NsIterator listNameSpaces() {
        return super.object.listNameSpaces();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getNsPrefixMap() {
        return super.object.getNsPrefixMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bag getBag(String uri) {
        return super.object.getBag(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String expandPrefix(String prefixed) {
        return super.object.expandPrefix(prefixed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bag getBag(Resource r) {
        return super.object.getBag(r);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String shortForm(String uri) {
        return super.object.shortForm(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResource(String uri) {
        return super.object.getResource(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Alt getAlt(String uri) {
        return super.object.getAlt(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property getProperty(String nameSpace, String localName) {
        return super.object.getProperty(nameSpace, localName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Alt getAlt(Resource r) {
        return super.object.getAlt(r);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String qnameFor(String uri) {
        return super.object.qnameFor(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrefixMapping lock() {
        return super.object.lock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Seq getSeq(String uri) {
        return super.object.getSeq(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource createResource() {
        return super.object.createResource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Seq getSeq(Resource r) {
        return super.object.getSeq(r);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource createResource(AnonId id) {
        return super.object.createResource(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource createResource(Resource type) {
        return super.object.createResource(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource createResource(String uri) {
        return super.object.createResource(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RDFNode getRDFNode(Node n) {
        return super.object.getRDFNode(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource createResource(String uri, Resource type) {
        return super.object.createResource(uri, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property createProperty(String nameSpace, String localName) {
        return super.object.createProperty(nameSpace, localName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean samePrefixMappingAs(PrefixMapping other) {
        return super.object.samePrefixMappingAs(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource createResource(ResourceF f) {
        return super.object.createResource(f);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createLiteral(String v, String language) {
        return super.object.createLiteral(v, language);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource createResource(String uri, ResourceF f) {
        return super.object.createResource(uri, f);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createLiteral(String v, boolean wellFormed) {
        return super.object.createLiteral(v, wellFormed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property createProperty(String uri) {
        return super.object.createProperty(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createLiteral(String v) {
        return super.object.createLiteral(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(boolean v) {
        return super.object.createTypedLiteral(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(String lex, RDFDatatype dtype) {
        return super.object.createTypedLiteral(lex, dtype);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(int v) {
        return super.object.createTypedLiteral(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(long v) {
        return super.object.createTypedLiteral(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(Calendar d) {
        return super.object.createTypedLiteral(d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(Object value, RDFDatatype dtype) {
        return super.object.createTypedLiteral(value, dtype);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(char v) {
        return super.object.createTypedLiteral(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(float v) {
        return super.object.createTypedLiteral(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(double v) {
        return super.object.createTypedLiteral(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(Object value) {
        return super.object.createTypedLiteral(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(String v) {
        return super.object.createTypedLiteral(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createStatement(Resource s, Property p, RDFNode o) {
        return super.object.createStatement(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(String lex, String typeURI) {
        return super.object.createTypedLiteral(lex, typeURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RDFList createList() {
        return super.object.createList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RDFList createList(Iterator<? extends RDFNode> members) {
        return super.object.createList(members);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Literal createTypedLiteral(Object value, String typeURI) {
        return super.object.createTypedLiteral(value, typeURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RDFList createList(RDFNode[] members) {
        return super.object.createList(members);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(Statement s) {
        return super.object.add(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(Statement[] statements) {
        return super.object.add(statements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createLiteralStatement(Resource s, Property p, boolean o) {
        return super.object.createLiteralStatement(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model remove(Statement[] statements) {
        return super.object.remove(statements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createLiteralStatement(Resource s, Property p, float o) {
        return super.object.createLiteralStatement(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(List<Statement> statements) {
        return super.object.add(statements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createLiteralStatement(Resource s, Property p, double o) {
        return super.object.createLiteralStatement(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createLiteralStatement(Resource s, Property p, long o) {
        return super.object.createLiteralStatement(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model remove(List<Statement> statements) {
        return super.object.remove(statements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createLiteralStatement(Resource s, Property p, int o) {
        return super.object.createLiteralStatement(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(StmtIterator iter) {
        return super.object.add(iter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createLiteralStatement(Resource s, Property p, char o) {
        return super.object.createLiteralStatement(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(Model m) {
        return super.object.add(m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createLiteralStatement(Resource s, Property p, Object o) {
        return super.object.createLiteralStatement(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(Model m, boolean suppressReifications) {
        return super.object.add(m, suppressReifications);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createStatement(Resource s, Property p, String o) {
        return super.object.createStatement(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model read(String url) {
        return super.object.read(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model read(InputStream in, String base) {
        return super.object.read(in, base);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createStatement(Resource s, Property p, String o, String l) {
        return super.object.createStatement(s, p, o, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createStatement(Resource s, Property p, String o,
                                     boolean wellFormed) {
        return super.object.createStatement(s, p, o, wellFormed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model read(InputStream in, String base, String lang) {
        return super.object.read(in, base, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement createStatement(Resource s, Property p, String o,
                                     String l, boolean wellFormed) {
        return super.object.createStatement(s, p, o, l, wellFormed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model read(Reader reader, String base) {
        return super.object.read(reader, base);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bag createBag() {
        return super.object.createBag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bag createBag(String uri) {
        return super.object.createBag(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Alt createAlt() {
        return super.object.createAlt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model read(String url, String lang) {
        return super.object.read(url, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Alt createAlt(String uri) {
        return super.object.createAlt(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model read(Reader reader, String base, String lang) {
        return super.object.read(reader, base, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Seq createSeq() {
        return super.object.createSeq();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Seq createSeq(String uri) {
        return super.object.createSeq(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(Resource s, Property p, RDFNode o) {
        return super.object.add(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model addLiteral(Resource s, Property p, boolean o) {
        return super.object.addLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model addLiteral(Resource s, Property p, long o) {
        return super.object.addLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model addLiteral(Resource s, Property p, int o) {
        return super.object.addLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model read(String url, String base, String lang) {
        return super.object.read(url, base, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model addLiteral(Resource s, Property p, char o) {
        return super.object.addLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model addLiteral(Resource s, Property p, float o) {
        return super.object.addLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model write(Writer writer) {
        return super.object.write(writer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model addLiteral(Resource s, Property p, double o) {
        return super.object.addLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model addLiteral(Resource s, Property p, Object o) {
        return super.object.addLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model write(Writer writer, String lang) {
        return super.object.write(writer, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model addLiteral(Resource s, Property p, Literal o) {
        return super.object.addLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model write(Writer writer, String lang, String base) {
        return super.object.write(writer, lang, base);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(Resource s, Property p, String o) {
        return super.object.add(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(Resource s, Property p, String lex, RDFDatatype datatype) {
        return super.object.add(s, p, lex, datatype);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(Resource s, Property p, String o, boolean wellFormed) {
        return super.object.add(s, p, o, wellFormed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model write(OutputStream out) {
        return super.object.write(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model add(Resource s, Property p, String o, String l) {
        return super.object.add(s, p, o, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model remove(Resource s, Property p, RDFNode o) {
        return super.object.remove(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model write(OutputStream out, String lang) {
        return super.object.write(out, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model remove(StmtIterator iter) {
        return super.object.remove(iter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model remove(Model m) {
        return super.object.remove(m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model write(OutputStream out, String lang, String base) {
        return super.object.write(out, lang, base);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model remove(Model m, boolean suppressReifications) {
        return super.object.remove(m, suppressReifications);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StmtIterator listLiteralStatements(Resource subject,
                                              Property predicate, boolean object) {
        return super.object.listLiteralStatements(subject, predicate, object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StmtIterator listLiteralStatements(Resource subject,
                                              Property predicate, char object) {
        return super.object.listLiteralStatements(subject, predicate, object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model remove(Statement s) {
        return super.object.remove(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement getRequiredProperty(Resource s, Property p) {
        return super.object.getRequiredProperty(s, p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StmtIterator listLiteralStatements(Resource subject,
                                              Property predicate, long object) {
        return super.object.listLiteralStatements(subject, predicate, object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StmtIterator listLiteralStatements(Resource subject,
                                              Property predicate, float object) {
        return super.object.listLiteralStatements(subject, predicate, object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement getProperty(Resource s, Property p) {
        return super.object.getProperty(s, p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StmtIterator listLiteralStatements(Resource subject,
                                              Property predicate, double object) {
        return super.object.listLiteralStatements(subject, predicate, object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listSubjectsWithProperty(Property p) {
        return super.object.listSubjectsWithProperty(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listResourcesWithProperty(Property p) {
        return super.object.listResourcesWithProperty(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StmtIterator listStatements(Resource subject, Property predicate,
                                       String object) {
        return super.object.listStatements(subject, predicate, object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listSubjectsWithProperty(Property p, RDFNode o) {
        return super.object.listSubjectsWithProperty(p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listResourcesWithProperty(Property p, RDFNode o) {
        return super.object.listResourcesWithProperty(p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StmtIterator listStatements(Resource subject, Property predicate,
                                       String object, String lang) {
        return super.object.listStatements(subject, predicate, object, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeIterator listObjects() {
        return super.object.listObjects();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeIterator listObjectsOfProperty(Property p) {
        return super.object.listObjectsOfProperty(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeIterator listObjectsOfProperty(Resource s, Property p) {
        return super.object.listObjectsOfProperty(s, p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Resource s, Property p) {
        return super.object.contains(s, p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listResourcesWithProperty(Property p, boolean o) {
        return super.object.listResourcesWithProperty(p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsResource(RDFNode r) {
        return super.object.containsResource(r);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listResourcesWithProperty(Property p, long o) {
        return super.object.listResourcesWithProperty(p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Resource s, Property p, RDFNode o) {
        return super.object.contains(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listResourcesWithProperty(Property p, char o) {
        return super.object.listResourcesWithProperty(p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listResourcesWithProperty(Property p, float o) {
        return super.object.listResourcesWithProperty(p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Statement s) {
        return super.object.contains(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listResourcesWithProperty(Property p, double o) {
        return super.object.listResourcesWithProperty(p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAny(StmtIterator iter) {
        return super.object.containsAny(iter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listResourcesWithProperty(Property p, Object o) {
        return super.object.listResourcesWithProperty(p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(StmtIterator iter) {
        return super.object.containsAll(iter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listSubjectsWithProperty(Property p, String o) {
        return super.object.listSubjectsWithProperty(p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAny(Model model) {
        return super.object.containsAny(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResIterator listSubjectsWithProperty(Property p, String o, String l) {
        return super.object.listSubjectsWithProperty(p, o, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Model model) {
        return super.object.containsAll(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsLiteral(Resource s, Property p, boolean o) {
        return super.object.containsLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReified(Statement s) {
        return super.object.isReified(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsLiteral(Resource s, Property p, long o) {
        return super.object.containsLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getAnyReifiedStatement(Statement s) {
        return super.object.getAnyReifiedStatement(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsLiteral(Resource s, Property p, int o) {
        return super.object.containsLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsLiteral(Resource s, Property p, char o) {
        return super.object.containsLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllReifications(Statement s) {
        super.object.removeAllReifications(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeReification(ReifiedStatement rs) {
        super.object.removeReification(rs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsLiteral(Resource s, Property p, float o) {
        return super.object.containsLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StmtIterator listStatements() {
        return super.object.listStatements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsLiteral(Resource s, Property p, double o) {
        return super.object.containsLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StmtIterator listStatements(Selector s) {
        return super.object.listStatements(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsLiteral(Resource s, Property p, Object o) {
        return super.object.containsLiteral(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StmtIterator listStatements(Resource s, Property p, RDFNode o) {
        return super.object.listStatements(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Resource s, Property p, String o) {
        return super.object.contains(s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Resource s, Property p, String o, String l) {
        return super.object.contains(s, p, o, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReifiedStatement createReifiedStatement(Statement s) {
        return super.object.createReifiedStatement(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReifiedStatement createReifiedStatement(String uri, Statement s) {
        return super.object.createReifiedStatement(uri, s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RSIterator listReifiedStatements() {
        return super.object.listReifiedStatements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RSIterator listReifiedStatements(Statement st) {
        return super.object.listReifiedStatements(st);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReificationStyle getReificationStyle() {
        return super.object.getReificationStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model query(Selector s) {
        return super.object.query(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model union(Model model) {
        return super.object.union(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model intersection(Model model) {
        return super.object.intersection(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model difference(Model model) {
        return super.object.difference(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object m) {
        return super.object.equals(m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model begin() {
        return super.object.begin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model abort() {
        return super.object.abort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model commit() {
        return super.object.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object executeInTransaction(Command cmd) {
        return super.object.executeInTransaction(cmd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean independent() {
        return super.object.independent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsTransactions() {
        return super.object.supportsTransactions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsSetOperations() {
        return super.object.supportsSetOperations();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIsomorphicWith(Model g) {
        return super.object.isIsomorphicWith(g);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        super.object.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lock getLock() {
        return super.object.getLock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model register(ModelChangedListener listener) {
        return super.object.register(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model unregister(ModelChangedListener listener) {
        return super.object.unregister(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model notifyEvent(Object e) {
        return super.object.notifyEvent(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model removeAll() {
        return super.object.removeAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model removeAll(Resource s, Property p, RDFNode r) {
        return super.object.removeAll(s, p, r);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return super.object.isClosed();
    }

}
