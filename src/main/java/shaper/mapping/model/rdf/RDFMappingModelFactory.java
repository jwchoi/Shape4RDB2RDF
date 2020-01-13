package shaper.mapping.model.rdf;

import shaper.Shaper;

import java.net.URI;
import java.util.List;
import java.util.Set;

public class RDFMappingModelFactory {
	public static RDFMappingModel generateMappingModel() {
		RDFMappingModel mappingMD = new RDFMappingModel(URI.create(Shaper.baseURI));
		
		Set<String> tables = Shaper.dbSchema.getTableNames();
		
		for(String table : tables) {
			TableIRI tableIRIMD = new TableIRI(table);

            List<String> columns = Shaper.dbSchema.getColumns(table);
			for(String column: columns) {
				LiteralProperty lpMD = new LiteralProperty(table, column);
				
				mappingMD.addLiteralPropertyMetaData(lpMD);
			} // END COLUMN

            Set<String> refConstraints = Shaper.dbSchema.getRefConstraints(table);
			for(String refConstraint: refConstraints) {
                ReferenceProperty rpMD = new ReferenceProperty(table, refConstraint);

                mappingMD.addReferencePropertyMetaData(rpMD);
            } // END REFERENTIAL CONSTRAINT

			mappingMD.addTableIRIMetaData(tableIRIMD);
		} // END TABLE
		
		return mappingMD;
	}
}
