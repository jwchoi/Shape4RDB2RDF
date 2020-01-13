package shaper.mapping.shacl;

import gr.seab.r2rml.beans.Database;
import gr.seab.r2rml.beans.Parser;
import gr.seab.r2rml.entities.MappingDocument;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import shaper.mapping.Symbols;
import shaper.mapping.model.r2rml.R2RMLModelFactory;
import shaper.mapping.model.shacl.Directive;
import shaper.mapping.model.shacl.ShaclDocModelFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
        Set<Directive> directives = shaclDocModel.getDirectives();
        for (Directive directive: directives)
            writer.println(directive);

        writer.println();
    }

    private void writeShacl() {}

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
