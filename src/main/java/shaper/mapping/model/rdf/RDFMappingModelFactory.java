package shaper.mapping.model.rdf;

import janus.database.DBSchema;
import shaper.Shaper;
import shaper.mapping.Symbols;

import java.net.URI;
import java.util.List;
import java.util.Set;

public class RDFMappingModelFactory {
	public static RDFMappingModel generateMappingModel(DBSchema dbSchema) {
		RDFMappingModel mappingMD = new RDFMappingModel(URI.create(Shaper.rdfBaseURI), dbSchema.getCatalog());
		
		Set<String> tables = dbSchema.getTableNames();
		
		for(String table : tables) {
			TableIRI tableIRIMD = new TableIRI(mappingMD.getBaseIRI(), table);

            List<String> columns = dbSchema.getColumns(table);
			for(String column: columns) {
				LiteralProperty lpMD = new LiteralProperty(table, column);
				
				mappingMD.addLiteralPropertyMetaData(lpMD);
			} // END COLUMN

            Set<String> refConstraints = dbSchema.getRefConstraints(table);
			for(String refConstraint: refConstraints) {
                ReferenceProperty rpMD = new ReferenceProperty(table, refConstraint);

                mappingMD.addReferencePropertyMetaData(rpMD);
            } // END REFERENTIAL CONSTRAINT

			mappingMD.addTableIRIMetaData(tableIRIMD);
		} // END TABLE
		
		return mappingMD;
	}
}
