package shaper.mapping.shacl;

import gr.seab.r2rml.beans.Database;
import gr.seab.r2rml.beans.Parser;
import gr.seab.r2rml.entities.MappingDocument;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.R2RMLModelFactory;
import shaper.mapping.model.r2rml.TriplesMap;
import shaper.mapping.model.shacl.IRI;
import shaper.mapping.model.shacl.NodeShape;
import shaper.mapping.model.shacl.ShaclDocModelFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.*;

public class R2RMLShaclMapper extends ShaclMapper {
    //-> for R2RML parser
    private String propertiesFile;
    //<- for R2RML parser

    public R2RMLShaclMapper(String propertiesFile) { this.propertiesFile = propertiesFile; }

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
        writer.println(Symbols.AT + Symbols.base + Symbols.SPACE + Symbols.LT + shaclDocModel.getBaseIRI() + Symbols.GT + Symbols.SPACE + Symbols.DOT);

        // prefixes
        Set<Map.Entry<URI, String>> entrySet = shaclDocModel.getPrefixMap().entrySet();
        for (Map.Entry<URI, String> entry: entrySet)
            writer.println(Symbols.AT + Symbols.prefix + Symbols.SPACE + entry.getValue() + Symbols.COLON + Symbols.SPACE + Symbols.LT + entry.getKey() + Symbols.GT + Symbols.SPACE + Symbols.DOT);
        writer.println();
    }

    private void writeShacl() {
        Set<TriplesMap> triplesMaps = r2rmlModel.getTriplesMaps();

        for (TriplesMap triplesMap : triplesMaps) {
            NodeShape nodeShape = shaclDocModel.getMappedNodeShape(triplesMap.getUri());

            List<IRI> propertyShapeIDs = nodeShape.getPropertyShapeIDs();
            for (IRI propertyShapeID: propertyShapeIDs)
                writer.println(shaclDocModel.getSerializedPropertyShape(propertyShapeID));

            writer.println(nodeShape);
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
    public File generateShaclFile() {
        r2rmlModel = R2RMLModelFactory.getR2RMLModel(generateMappingDocument());
        shaclDocModel = ShaclDocModelFactory.getSHACLDocModel(r2rmlModel);

        preProcess();
        writeDirectives();
        writeShacl();
        postProcess();

        System.out.println("Translating the R2RML into SHACL has finished.");

        return output;
    }
}
