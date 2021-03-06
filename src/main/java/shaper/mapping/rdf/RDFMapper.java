package shaper.mapping.rdf;

import shaper.mapping.model.dm.DirectMappingModel;

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

    public DirectMappingModel directMappingModel; // used only when Direct Mapping

    File output;
    PrintWriter writer;

    public abstract File generateRDFFile();
}
