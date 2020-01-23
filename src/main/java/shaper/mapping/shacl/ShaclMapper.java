package shaper.mapping.shacl;

import shaper.mapping.model.r2rml.R2RMLModel;
import shaper.mapping.model.rdf.RDFMappingModel;
import shaper.mapping.model.shacl.ShaclDocModel;

import java.io.File;
import java.io.PrintWriter;

public abstract class ShaclMapper {
    protected R2RMLModel r2rmlModel; // used only when mapping with R2RML
    protected RDFMappingModel rdfMappingModel; // used only when Direct Mapping

    protected ShaclDocModel shaclDocModel; // dependent on r2rmlModel or rdfMappingModel

    protected File output;
    protected PrintWriter writer;

    public abstract File generateShaclFile();
}
