package fr.inria.eventcloud.reasoner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;

/**
 * The SPARQL decomposer is used to decompose a SPARQL query into subqueries if
 * several conjunctions or disjunctions are detected.
 * 
 * @author lpellegr
 */
public class SparqlDecomposer {

    public List<AtomicSparqlQuery> decompose(String sparqlQuery) {
        Query query = QueryFactory.create(sparqlQuery);
        return this.parseQueryTree((ElementGroup) query.getQueryPattern());
    }

    private ArrayList<AtomicSparqlQuery> parseQueryTree(ElementGroup group) {
        ArrayList<AtomicSparqlQuery> result =
                new ArrayList<AtomicSparqlQuery>();

        List<Element> elementList = group.getElements();
        for (int i = 0; i < elementList.size(); i++) {
            Element e = elementList.get(i);
            result.addAll(this.processElement(e));
        }

        return result;
    }

    private List<AtomicSparqlQuery> processElement(Element elt) {
        List<AtomicSparqlQuery> result = new ArrayList<AtomicSparqlQuery>();

        // Parses a Basic Graph Pattern
        if (elt instanceof ElementPathBlock) {
            result = parse((ElementPathBlock) elt);
        } else if (elt instanceof ElementUnion) {
            // Parses an UNION keyword which forms a disjunction
            // of two graph patterns
            ElementUnion unionBlock = ((ElementUnion) elt);
            for (Element unionElt : unionBlock.getElements()) {
                for (Element graphPattern : ((ElementGroup) unionElt).getElements()) {
                    result.addAll(this.parse((ElementPathBlock) graphPattern));
                }
            }
        }

        return result;
    }

    public List<AtomicSparqlQuery> parse(ElementPathBlock elt) {
        List<AtomicSparqlQuery> result = new ArrayList<AtomicSparqlQuery>();
        ElementPathBlock block = elt;
        Iterator<TriplePath> it = block.patternElts();

        TriplePath triple;
        int i = 0;
        while (it.hasNext()) {
            triple = it.next();

            // TODO adds support for FilterElement
            result.add(new AtomicSparqlQuery(
                    triple.getSubject().toString(), triple.getPredicate()
                            .toString(), triple.getObject().toString()));
            i++;
        }

        return result;
    }

    public static void main(String[] args) {
        // String query1 =
        // "CONSTRUCT { ?s ?p ?o } WHERE { ?s <http://predicate1.com> ?o . ?o <http://predicate2.com> \"hello\" } ";
        // String query2 =
        // "CONSTRUCT { ?s ?p ?o } WHERE { ?s <http://predicate1.com> ?o . } ";

        // disjunction
        String query3 =
                "PREFIX go: <http://purl.org/obo/owl/GO#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX obo: <http://www.obofoundry.org/ro/ro.owl#> SELECT DISTINCT ?label ?process WHERE { { ?process obo:part_of go:GO_0007165 }  UNION  { ?process rdfs:subClassOf go:GO_0007165 } }";

        SparqlDecomposer decomposer = new SparqlDecomposer();
        List<AtomicSparqlQuery> subqueries = decomposer.decompose(query3);

        for (AtomicSparqlQuery query : subqueries) {
            System.out.println(query.toConstruct());
        }
    }

}