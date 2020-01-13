package shaper.mapping.shex;

import shaper.Shaper;
import shaper.mapping.PrefixMap;
import shaper.mapping.Symbols;
import shaper.mapping.model.rdf.RDFMappingModelFactory;
import shaper.mapping.model.shex.ShExSchemaFactory;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Set;

public class DirectMappingShExMapper extends ShExMapper {

    private void writeDirectives() {
        // base
        writer.println(Symbols.BASE + Symbols.SPACE + Symbols.LT + shExSchema.getBaseIRI() + Symbols.GT); // for an RDF Graph by direct mapping

        // prefix for newly created shape expressions
        writer.println(Symbols.PREFIX + Symbols.SPACE + shExSchema.getPrefix() + Symbols.COLON + Symbols.SPACE + Symbols.LT + shExSchema.getBaseIRI() + Symbols.HASH + Symbols.GT);

        // prefixID
        writer.println(Symbols.PREFIX + Symbols.SPACE + PrefixMap.getPrefix(URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns")) + Symbols.COLON + Symbols.SPACE + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
        writer.println(Symbols.PREFIX + Symbols.SPACE + PrefixMap.getPrefix(URI.create("http://www.w3.org/2001/XMLSchema")) + Symbols.COLON + Symbols.SPACE + "<http://www.w3.org/2001/XMLSchema#>");
        writer.println(Symbols.PREFIX + Symbols.SPACE + PrefixMap.getPrefix(URI.create("http://www.w3.org/2000/01/rdf-schema")) + Symbols.COLON + Symbols.SPACE + "<http://www.w3.org/2000/01/rdf-schema#>");
        writer.println();
    }

    private void writeShEx() {
        Set<String> tables = Shaper.dbSchema.getTableNames();

        for(String table : tables) {
            List<String> columns = Shaper.dbSchema.getColumns(table);
            for (String column: columns) {
                String id = shExSchema.getPrefix() + Symbols.COLON + shExSchema.getMappedNodeConstraintID(table, column);
                writer.println(id + Symbols.SPACE + shExSchema.getMappedNodeConstraint(table, column));
            }
            writer.println();

            writer.println(shExSchema.getMappedShape(table));
            writer.println();
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
    public File generateShExFile() {
        rdfMappingModel = RDFMappingModelFactory.generateMappingModel();
        shExSchema = ShExSchemaFactory.getShExSchemaModel(Shaper.dbSchema);

        preProcess();
        writeDirectives();
        writeShEx();
        postProcess();

        System.out.println("Translating the schema into ShEx has finished.");

        return output;
    }
}
