package shaper.mapping.shacl;

import shaper.Shaper;
import shaper.mapping.model.r2rml.R2RMLModel;
import shaper.mapping.model.rdf.RDFMappingModel;
import shaper.mapping.model.shacl.ShaclDocModel;

import java.io.File;
import java.io.PrintWriter;

public abstract class ShaclMapper {
    public R2RMLModel r2rmlModel; // used only when mapping with R2RML
    public RDFMappingModel rdfMappingModel; // used only when Direct Mapping

    public ShaclDocModel shaclDocModel; // dependent on r2rmlModel or rdfMappingModel

    File output;
    PrintWriter writer;

    public abstract File generateShaclFile();

    void preProcess() {
        String catalog = Shaper.dbSchema.getCatalog();

        output = new File(Shaper.DEFAULT_DIR_FOR_SHACL_FILE + catalog + "." + "shaclc");

        try {
            writer = new PrintWriter(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
