package shaper.mapping.model.shacl;

import janus.database.DBSchema;
import shaper.Shaper;
import shaper.mapping.PrefixMap;
import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ShaclDocModelFactory {
    private static R2RMLModel r2rmlModel;
    private static ShaclDocModel shaclDocModel;
    // R2RML
    public static ShaclDocModel getSHACLDocModel(R2RMLModel r2rmlModel) {
        ShaclDocModelFactory.r2rmlModel = r2rmlModel;
        shaclDocModel = new ShaclDocModel(URI.create(Shaper.shapeBaseURI), Shaper.prefixForShapeBaseURI);

        addPrefixes();

        Set<TriplesMap> triplesMaps = r2rmlModel.getTriplesMaps();

        // add PropertyShapes to NodeShape
        // add NodeShapes to ShaclDocModel
        for (TriplesMap triplesMap : triplesMaps) {
            SubjectMap subjectMap = triplesMap.getSubjectMap();

            //create a node shape
            NodeShape nodeShape = new NodeShape(createNodeShapeID(triplesMap), triplesMap.getUri(), subjectMap, shaclDocModel);

            List<PredicateObjectMap> predicateObjectMaps = triplesMap.getPredicateObjectMaps();
            for (PredicateObjectMap predicateObjectMap: predicateObjectMaps) {
                List<PredicateObjectMap.PredicateObjectPair> predicateObjectPairs = predicateObjectMap.getPredicateObjectPairs();
                for (PredicateObjectMap.PredicateObjectPair predicateObjectPair: predicateObjectPairs) {
                    PredicateMap predicateMap = predicateObjectPair.getPredicateMap();

                    // create property shape from a predicate-object pair
                    String predicate = predicateMap.getConstant().get();
                    int multiplicity = getMultiplicity(triplesMap, URI.create(predicate));
                    boolean hasQualifiedValueShape = multiplicity > 1 ? true : false;
                    IRI propertyShapeID = createPropertyShapeID(nodeShape, predicate, hasQualifiedValueShape);

                    PropertyShape propertyShape;

                    if (predicateObjectPair.getRefObjectMap().isPresent()) {
                        // when referencing object map
                        RefObjectMap refObjectMap = predicateObjectPair.getRefObjectMap().get();
                        propertyShape = new PropertyShape(propertyShapeID, predicateMap, refObjectMap, hasQualifiedValueShape, shaclDocModel);
                    } else {
                        // when object map
                        ObjectMap objectMap = predicateObjectPair.getObjectMap().get();
                        propertyShape = new PropertyShape(propertyShapeID, predicateMap, objectMap, hasQualifiedValueShape, shaclDocModel);
                    }
                    shaclDocModel.addShape(propertyShape);

                    // for reference from node shape to property shape
                    nodeShape.addPropertyShape(propertyShape.getID());
                }
            }

            shaclDocModel.addShape(nodeShape);
        }

        // add PropertyShapes additionally generated due to duplicated predicates to the NodeShape
        for (TriplesMap triplesMap : triplesMaps) {
            // find the mapped shape
            NodeShape nodeShape = shaclDocModel.getMappedNodeShape(triplesMap.getUri());

            Set<String> checkedPredicates = new TreeSet<>();

            List<PredicateObjectMap> predicateObjectMaps = triplesMap.getPredicateObjectMaps();
            for (PredicateObjectMap predicateObjectMap : predicateObjectMaps) {
                List<PredicateObjectMap.PredicateObjectPair> predicateObjectPairs = predicateObjectMap.getPredicateObjectPairs();
                for (PredicateObjectMap.PredicateObjectPair predicateObjectPair: predicateObjectPairs) {
                    PredicateMap predicateMap = predicateObjectPair.getPredicateMap();

                    String predicate = predicateMap.getConstant().get();
                    if (!checkedPredicates.contains(predicate)) {

                        int multiplicity = getMultiplicity(triplesMap, URI.create(predicate));

                        if (multiplicity < 2) continue;

                        boolean hasQualifiedValueShape = false;
                        IRI propertyShapeID = createPropertyShapeID(nodeShape, predicate, hasQualifiedValueShape);
                        PropertyShape propertyShape = new PropertyShape(propertyShapeID, predicateMap, multiplicity, shaclDocModel);
                        shaclDocModel.addShape(propertyShape);

                        // for reference from node shape to property shape
                        nodeShape.addPropertyShape(propertyShape.getID());
                    }

                    checkedPredicates.add(predicate);
                }
            }
        }

        return shaclDocModel;
    }

    private static String obtainFragmentOrLastPathSegmentOf(URI uri) {
        String postfix = uri.getFragment();
        if (postfix == null) {
            Path path = Path.of(uri.getPath());
            postfix = path.getName(path.getNameCount()-1).toString();
        }

        return postfix;
    }

    private static IRI createNodeShapeID(TriplesMap triplesMap) {
        String postfix = obtainFragmentOrLastPathSegmentOf(triplesMap.getUri());
        return IRI.create(shaclDocModel.getBaseIRI() + Symbols.HASH + postfix + "Shape");
    }

    private static int getMultiplicity(TriplesMap triplesMap, URI predicate) {
        int multiplicity = 0;

        List<PredicateObjectMap> predicateObjectMaps = triplesMap.getPredicateObjectMaps();
        for (PredicateObjectMap predicateObjectMap: predicateObjectMaps) {

            List<PredicateObjectMap.PredicateObjectPair> predicateObjectPairs = predicateObjectMap.getPredicateObjectPairs();
            for (PredicateObjectMap.PredicateObjectPair predicateObjectPair: predicateObjectPairs) {

                PredicateMap predicateMap = predicateObjectPair.getPredicateMap();
                if (predicateMap.getConstant().isPresent() && predicateMap.getConstant().get().equals(predicate.toString()))
                    multiplicity++;
            }
        }

        return multiplicity;
    }

    private static IRI createPropertyShapeID(NodeShape nodeShape, String predicateURIString, boolean hasQualifiedValueShape) {
        URI predicateURI = URI.create(predicateURIString);
        String prefixOfPredicateURI = shaclDocModel.getPrefixOf(predicateURI);

        String postfix = obtainFragmentOrLastPathSegmentOf(predicateURI);

        if (prefixOfPredicateURI != null)
            postfix = prefixOfPredicateURI + Symbols.DASH + postfix;

        IRI propertyShapeID = IRI.create(nodeShape.getID() + Symbols.DASH + postfix);

        if (hasQualifiedValueShape) {
            IRI tempPropertyShapeID;
            Set<IRI> propertyShapeIDs = nodeShape.getPropertyShapeIDs();
            int index = 0;
            do {
                index++;
                tempPropertyShapeID = IRI.create(propertyShapeID + Symbols.DASH + "q" + index);
            } while (propertyShapeIDs.contains(tempPropertyShapeID));
            propertyShapeID = tempPropertyShapeID;
        }

        return propertyShapeID;
    }

    private static void addPrefixes() {
        // PREFIX: from RDF graph generated by the R2RML document.
        Set<Map.Entry<String, String>> entrySet = r2rmlModel.getPrefixMap().entrySet();
        for (Map.Entry<String, String> entry: entrySet)
            shaclDocModel.addPrefixDecl(entry.getKey(), entry.getValue());

        shaclDocModel.addPrefixDecl("rdf", PrefixMap.getURI("rdf").toString());
    }

    //Direct Mapping
    public static ShaclDocModel getSHACLDocModel(DBSchema dbSchema) {
        return null;
    }
}
