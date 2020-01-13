package shaper.mapping.shacl;

import shaper.Shaper;
import shaper.mapping.model.rdf.RDFMappingModelFactory;
import shaper.mapping.model.shacl.ShaclDocModelFactory;

import java.io.File;

public class DirectMappingShaclMapper extends ShaclMapper {

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

        preProcess();
        writeDirectives();
        writeShacl();
        postProcess();

        System.out.println("Translating the schema into SHACL has finished.");

        return output;
    }
}
