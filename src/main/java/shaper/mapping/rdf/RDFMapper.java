package shaper.mapping.rdf;

import shaper.Shaper;
import shaper.mapping.model.rdf.RDFMappingModel;

import java.io.File;
import java.io.PrintWriter;

public abstract class RDFMapper {

    enum Extension {
        Turtle("ttl");

        private final String extension;

        Extension(String extension) {
            this.extension = extension;
        }

        @Override
        public String toString() { return extension; }
    }

    public RDFMappingModel rdfMappingModel;

    File output;
    PrintWriter writer;

    public abstract File generateRDFFile();

    void preProcess(Extension extension) {
        String catalog = Shaper.dbSchema.getCatalog();

        switch (extension) {
            case Turtle:
                output = new File(Shaper.DEFAULT_DIR_FOR_RDF_FILE + catalog + "." + extension);
                break;
        }
        try {
            writer = new PrintWriter(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
