package shaper.mapping.shex;

import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.*;
import shaper.mapping.model.shex.NodeConstraint;
import shaper.mapping.model.shex.ShExSchemaFactory;
import shaper.mapping.model.shex.Shape;
import shaper.mapping.r2rml.R2RMLParser;

import java.io.File;
import java.util.*;

public class R2RMLShExMapper extends ShExMapper {
    private String R2RMLPathname;

    public R2RMLShExMapper(String R2RMLPathname) { this.R2RMLPathname = R2RMLPathname; }

    private R2RMLParser getR2RMLParser() { return new R2RMLParser(R2RMLPathname, R2RMLParser.Lang.TTL); }

    private void writeDirectives() {
        // base
        writer.println(Symbols.BASE + Symbols.SPACE + Symbols.LT + shExSchema.getBaseIRI() + Symbols.GT);

        // prefix for newly created shape expressions
        writer.println(Symbols.PREFIX + Symbols.SPACE + shExSchema.getPrefix() + Symbols.COLON + Symbols.SPACE + Symbols.LT + shExSchema.getBaseIRI() + Symbols.HASH + Symbols.GT);

        // prefixes
        Set<Map.Entry<String, String>> entrySet = r2rmlModel.getPrefixMap().entrySet();
        for (Map.Entry<String, String> entry: entrySet)
            writer.println(Symbols.PREFIX + Symbols.SPACE + entry.getKey() + Symbols.COLON + Symbols.SPACE + Symbols.LT + entry.getValue() + Symbols.GT);
        writer.println();
    }

    private void writeShEx() {
        Set<TriplesMap> triplesMaps = r2rmlModel.getTriplesMaps();

        for (TriplesMap triplesMap : triplesMaps) {
            List<PredicateObjectMap> predicateObjectMaps = triplesMap.getPredicateObjectMaps();
            for (PredicateObjectMap predicateObjectMap: predicateObjectMaps) {
                Optional<ObjectMap> objectMap = predicateObjectMap.getObjectMap();
                if (objectMap.isPresent()) {
                    if (NodeConstraint.isPossibleToHaveXSFacet(objectMap.get())) {
                        String nodeConstraintID = shExSchema.getMappedNodeConstraintID(objectMap.get());
                        String nodeConstraint = shExSchema.getMappedNodeConstraint(objectMap.get());
                        if (nodeConstraintID != null && nodeConstraint != null) {
                            String id = shExSchema.getPrefix() + Symbols.COLON + nodeConstraintID;
                            writer.println(id + Symbols.SPACE + nodeConstraint);
                        }
                    }
                }
            }
            writer.println();

            writer.println(shExSchema.getMappedShape(triplesMap.getUri()));
            writer.println();
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Set<Shape> derivedShapes = shExSchema.getDerivedShapes();
        for (Shape derivedShape: derivedShapes) {
            writer.println(derivedShape);
            writer.println();
        }
    }

    private void postProcess() {
        try {
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public File generateShExFile() {
        r2rmlModel = R2RMLModelFactory.getR2RMLModel(getR2RMLParser());
        shExSchema = ShExSchemaFactory.getShExSchemaModel(r2rmlModel);

        preProcess();
        writeDirectives();
        writeShEx();
        postProcess();

        System.out.println("Translating the R2RML into ShEx has finished.");

        return output;
    }
}
