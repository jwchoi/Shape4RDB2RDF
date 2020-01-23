package shaper.mapping.shacl;

import shaper.Shaper;
import shaper.mapping.model.rdf.RDFMappingModelFactory;
import shaper.mapping.model.shacl.ShaclDocModelFactory;

import java.io.File;
import java.io.PrintWriter;

public class DirectMappingShaclMapper extends ShaclMapper {

    private void preProcess(String catalog) {
        output = new File(Shaper.DEFAULT_DIR_FOR_SHACL_FILE + catalog + "." + "ttl");

        try {
            writer = new PrintWriter(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeDirectives() {}

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
        rdfMappingModel = RDFMappingModelFactory.generateMappingModel();
        shaclDocModel = ShaclDocModelFactory.getSHACLDocModel(Shaper.dbSchema);

        preProcess(Shaper.dbSchema.getCatalog());
        writeDirectives();
        writeShacl();
        postProcess();

        System.out.println("Translating the schema into SHACL has finished.");

        return output;
    }
}
