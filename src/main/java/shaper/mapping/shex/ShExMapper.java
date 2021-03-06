package shaper.mapping.shex;

import shaper.mapping.model.r2rml.R2RMLModel;
import shaper.mapping.model.dm.DirectMappingModel;
import shaper.mapping.model.shex.ShExSchema;

import java.io.File;
import java.io.PrintWriter;

public abstract class ShExMapper {
    public R2RMLModel r2rmlModel; // used only when mapping with R2RML
    public DirectMappingModel directMappingModel; // used only when Direct Mapping

    public ShExSchema shExSchema; // dependent on r2rmlModel or rdfMappingModel

    File output;
    PrintWriter writer;

    public abstract File generateShExFile();
}
