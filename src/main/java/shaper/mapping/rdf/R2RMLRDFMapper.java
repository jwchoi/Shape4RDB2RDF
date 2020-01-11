package shaper.mapping.rdf;

import gr.seab.r2rml.beans.Database;
import gr.seab.r2rml.beans.Generator;
import gr.seab.r2rml.beans.Parser;
import gr.seab.r2rml.entities.MappingDocument;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class R2RMLRDFMapper extends RDFMapper {
    //-> for R2RML parser
    private String propertiesFile;
    //<- for R2RML parser

    public R2RMLRDFMapper(String propertiesFile) { this.propertiesFile = propertiesFile; }

    @Override
    public File generateRDFFile() {
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

        Generator generator = (Generator) context.getBean("generator");
        generator.setProperties(properties);
        generator.setResultModel(parser.getResultModel());

        generator.createTriples(mappingDocument);

        context.close();

        return new File(properties.getProperty("jena.destinationFileName"));
    }
}
