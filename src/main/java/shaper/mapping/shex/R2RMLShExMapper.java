package shaper.mapping.shex;

import gr.seab.r2rml.beans.Database;
import gr.seab.r2rml.beans.Parser;
import gr.seab.r2rml.entities.MappingDocument;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.*;
import shaper.mapping.model.shex.NodeConstraint;
import shaper.mapping.model.shex.ShExSchemaFactory;
import shaper.mapping.model.shex.Shape;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class R2RMLShExMapper extends ShExMapper {
    //-> for R2RML parser
    private String propertiesFile;
    //<- for R2RML parser

    public R2RMLShExMapper(String propertiesFile) { this.propertiesFile = propertiesFile; }

    private MappingDocument generateMappingDocument() {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (Exception ex) {
            System.err.println("Error reading properties file (" + propertiesFile + ").");
            return null;
        }

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("app-context.xml");

        Database db = (Database) context.getBean("db");
        db.setProperties(properties);

        Parser parser = (Parser) context.getBean("parser");
        parser.setProperties(properties);

        MappingDocument mappingDocument = parser.parse();

        context.close();

        return mappingDocument;
    }

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
        r2rmlModel = R2RMLModelFactory.getR2RMLModel(generateMappingDocument());
        shExSchema = ShExSchemaFactory.getShExSchemaModel(r2rmlModel);

        preProcess();
        writeDirectives();
        writeShEx();
        postProcess();

        System.out.println("Translating the R2RML into ShEx has finished.");

        return output;
    }
}
